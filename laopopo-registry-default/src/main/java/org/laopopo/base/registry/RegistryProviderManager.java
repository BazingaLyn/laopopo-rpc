package org.laopopo.base.registry;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.exception.remoting.RemotingSendRequestException;
import org.laopopo.common.exception.remoting.RemotingTimeoutException;
import org.laopopo.common.loadbalance.LoadBalanceStrategy;
import org.laopopo.common.metrics.ServiceMetrics;
import org.laopopo.common.metrics.ServiceMetrics.ConsumerInfo;
import org.laopopo.common.metrics.ServiceMetrics.ProviderInfo;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.rpc.RegisterMeta;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.common.rpc.ServiceReviewState;
import org.laopopo.common.transport.body.AckCustomBody;
import org.laopopo.common.transport.body.ManagerServiceCustomBody;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.common.transport.body.MetricsCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody;
import org.laopopo.common.transport.body.SubscribeRequestCustomBody;
import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 注册服务中心端的provider侧的管理
 * 
 * @time 2016年8月15日
 * @modifytime
 */
public class RegistryProviderManager implements RegistryProviderServer {

	private static final Logger logger = LoggerFactory.getLogger(RegistryProviderManager.class);

	private static final AttributeKey<ConcurrentSet<String>> S_SUBSCRIBE_KEY = AttributeKey.valueOf("server.subscribed");
	private static final AttributeKey<ConcurrentSet<RegisterMeta>> S_PUBLISH_KEY = AttributeKey.valueOf("server.published");

	private DefaultRegistryServer defaultRegistryServer;

	// 某个服务
	private final ConcurrentMap<String, ConcurrentMap<Address, RegisterMeta>> globalRegisterInfoMap = new ConcurrentHashMap<String, ConcurrentMap<Address, RegisterMeta>>();
	// 指定节点都注册了哪些服务
	private final ConcurrentMap<Address, ConcurrentSet<String>> globalServiceMetaMap = new ConcurrentHashMap<RegisterMeta.Address, ConcurrentSet<String>>();
	// 某个服务 订阅它的消费者的channel集合
	private final ConcurrentMap<String, ConcurrentSet<Channel>> globalConsumerMetaMap = new ConcurrentHashMap<String, ConcurrentSet<Channel>>();
	// 提供者某个地址对应的channel
	private final ConcurrentMap<Address, Channel> globalProviderChannelMetaMap = new ConcurrentHashMap<RegisterMeta.Address, Channel>();

	private final ConcurrentMap<String, LoadBalanceStrategy> globalServiceLoadBalance = new ConcurrentHashMap<String, LoadBalanceStrategy>();

	public RegistryProviderManager(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
	}

	public RemotingTransporter handleManager(RemotingTransporter request, Channel channel) throws RemotingSendRequestException, RemotingTimeoutException,
			InterruptedException {

		ManagerServiceCustomBody managerServiceCustomBody = serializerImpl().readObject(request.bytes(), ManagerServiceCustomBody.class);

		switch (managerServiceCustomBody.getManagerServiceRequestType()) {
			case REVIEW:
				return handleReview(managerServiceCustomBody.getSerivceName(), managerServiceCustomBody.getAddress(), request.getOpaque(),
						managerServiceCustomBody.getServiceReviewState());
			case DEGRADE:
				return handleDegradeService(request, channel);
			case MODIFY_WEIGHT:
				return handleModifyWeight(request.getOpaque(), managerServiceCustomBody);
			case MODIFY_LOADBALANCE:
				return handleModifyLoadBalance(request.getOpaque(), managerServiceCustomBody);
			case METRICS:
				return handleMetricsService(managerServiceCustomBody.getSerivceName(), request.getOpaque());
			default:
				break;
		}
		return null;
	}

	/**
	 * 处理provider服务注册
	 * 
	 * @throws InterruptedException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	@Override
	public RemotingTransporter handlerRegister(RemotingTransporter remotingTransporter, Channel channel) throws RemotingSendRequestException,
			RemotingTimeoutException, InterruptedException {

		// 准备好ack信息返回个provider，悲观主义，默认返回失败ack，要求provider重新发送请求
		AckCustomBody ackCustomBody = new AckCustomBody(remotingTransporter.getOpaque(), false);
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody,
				remotingTransporter.getOpaque());

		// 接收到主体信息
		PublishServiceCustomBody publishServiceCustomBody = serializerImpl().readObject(remotingTransporter.bytes(), PublishServiceCustomBody.class);

		RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody);

		if (logger.isDebugEnabled()) {
			logger.info("Publish [{}] on channel[{}].", meta, channel);
		}

		// channel上打上该服务的标记 方便当channel inactive的时候，直接从channel上拿到标记的属性，通知
		attachPublishEventOnChannel(meta, channel);

		// 一个服务的最小单元，也是确定一个服务的最小单位
		final String serviceName = meta.getServiceName();
		// 找出提供此服务的全部地址和该服务在该地址下的审核情况
		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceName);

		synchronized (globalRegisterInfoMap) {

			// 获取到这个地址可能以前注册过的注册信息
			RegisterMeta existRegiserMeta = maps.get(meta.getAddress());

			// 如果等于空，则说明以前没有注册过
			if (null == existRegiserMeta) {
				existRegiserMeta = meta;
				maps.put(meta.getAddress(), existRegiserMeta);
			}

			this.getServiceMeta(meta.getAddress()).add(serviceName);

			// 设置该服务默认的负载均衡的策略
			globalServiceLoadBalance.put(serviceName, LoadBalanceStrategy.WEIGHTINGRANDOM);

			// 判断provider发送的信息已经被成功的存储的情况下，则告之服务注册成功
			ackCustomBody.setSuccess(true);

			// 如果审核通过，则通知相关服务的订阅者
			if (meta.getIsReviewed() == ServiceReviewState.PASS_REVIEW) {
				this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriber(meta, globalServiceLoadBalance.get(serviceName));
			}
		}

		globalProviderChannelMetaMap.put(meta.getAddress(), channel);

		return responseTransporter;
	}

	public RemotingTransporter handleMetricsService(String metricsServiceName, long requestId) {

		MetricsCustomBody responseBody = new MetricsCustomBody();
		RemotingTransporter remotingTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.MANAGER_SERVICE, responseBody, requestId);
		List<ServiceMetrics> serviceMetricses = new ArrayList<ServiceMetrics>();
		// 统计全部
		if (metricsServiceName == null) {

			if (globalServiceMetaMap.keySet() != null) {

				for (String serviceName : globalRegisterInfoMap.keySet()) {
					ServiceMetrics serviceMetrics = assemblyServiceMetricsByServiceName(serviceName);
					serviceMetricses.add(serviceMetrics);
				}
			}
		} else { // 更新的服务
			String serviceName = metricsServiceName;
			ServiceMetrics serviceMetrics = assemblyServiceMetricsByServiceName(serviceName);
			serviceMetricses.add(serviceMetrics);

		}
		responseBody.setServiceMetricses(serviceMetricses);
		return remotingTransporter;
	}

	/**
	 * 修改某个服务的负载均衡的策略
	 * 
	 * @param opaque
	 * @param managerServiceCustomBody
	 * @return
	 */
	private RemotingTransporter handleModifyLoadBalance(long opaque, ManagerServiceCustomBody managerServiceCustomBody) {

		AckCustomBody ackCustomBody = new AckCustomBody(opaque, false);
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, opaque);

		String serviceName = managerServiceCustomBody.getSerivceName();
		LoadBalanceStrategy balanceStrategy = managerServiceCustomBody.getLoadBalanceStrategy();

		synchronized (globalServiceLoadBalance) {
			LoadBalanceStrategy currentLoadBalanceStrategy = globalServiceLoadBalance.get(serviceName);

			if (null == currentLoadBalanceStrategy) {
				return responseTransporter;
			}

			ackCustomBody.setSuccess(true);

			if (currentLoadBalanceStrategy != balanceStrategy) {
				currentLoadBalanceStrategy = balanceStrategy;

			}
		}

		return responseTransporter;
	}

	/**
	 * 修改某个服务实例上的权重
	 * 
	 * @param opaque
	 * @param managerServiceCustomBody
	 * @param channel
	 * @return
	 * @throws InterruptedException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	private RemotingTransporter handleModifyWeight(long opaque, ManagerServiceCustomBody managerServiceCustomBody) throws RemotingSendRequestException,
			RemotingTimeoutException, InterruptedException {

		// 准备好ack信息返回个provider，悲观主义，默认返回失败ack，要求provider重新发送请求
		AckCustomBody ackCustomBody = new AckCustomBody(opaque, false);
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, opaque);

		String serviceName = managerServiceCustomBody.getSerivceName(); // 服务名
		Address address = managerServiceCustomBody.getAddress(); // 地址
		Integer weight = managerServiceCustomBody.getWeightVal(); // 权重

		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceName);

		synchronized (globalRegisterInfoMap) {

			if (maps == null) {
				return responseTransporter;
			}
			RegisterMeta meta = maps.get(address);
			meta.setWeight(weight);

			ackCustomBody.setSuccess(true);

			// 如果审核通过，则通知相关服务的订阅者
			if (meta.getIsReviewed() == ServiceReviewState.PASS_REVIEW) {
				this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriber(meta, globalServiceLoadBalance.get(serviceName));
			}
		}

		return responseTransporter;
	}

	/**
	 * provider端发送的请求，取消对某个服务的提供
	 * 
	 * @param request
	 * @param channel
	 * @return
	 * @throws InterruptedException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	public RemotingTransporter handlerRegisterCancel(RemotingTransporter request, Channel channel) throws RemotingSendRequestException,
			RemotingTimeoutException, InterruptedException {

		// 准备好ack信息返回个provider，悲观主义，默认返回失败ack，要求provider重新发送请求
		AckCustomBody ackCustomBody = new AckCustomBody(request.getOpaque(), false);
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, request.getOpaque());

		// 接收到主体信息
		PublishServiceCustomBody publishServiceCustomBody = serializerImpl().readObject(request.bytes(), PublishServiceCustomBody.class);

		RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody);

		handlePublishCancel(meta, channel);

		ackCustomBody.setSuccess(true);

		globalProviderChannelMetaMap.remove(meta.getAddress());

		return responseTransporter;
	}

	/**
	 * 处理consumer的消息订阅，并返回结果
	 * 
	 * @param request
	 * @param channel
	 * @return
	 */
	public RemotingTransporter handleSubscribe(RemotingTransporter request, Channel channel) {

		SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.SUBCRIBE_RESULT, subcribeResultCustomBody,
				request.getOpaque());
		// 接收到主体信息
		SubscribeRequestCustomBody requestCustomBody = serializerImpl().readObject(request.bytes(), SubscribeRequestCustomBody.class);
		String serviceName = requestCustomBody.getServiceName();
		// 将其降入到channel的group中去
		this.defaultRegistryServer.getConsumerManager().getSubscriberChannels().add(channel);

		// 存储消费者信息
		ConcurrentSet<Channel> channels = globalConsumerMetaMap.get(serviceName);
		if (null == channels) {
			channels = new ConcurrentSet<Channel>();
		}
		channels.add(channel);
		globalConsumerMetaMap.put(serviceName, channels);
		

		attachSubscribeEventOnChannel(serviceName, channel);

		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceName);
		// 如果订阅的暂时还没有服务提供者，则返回空列表给订阅者
		if (maps.isEmpty()) {
			return responseTransporter;
		}

		buildSubcribeResultCustomBody(maps, subcribeResultCustomBody);

		return responseTransporter;
	}

	/***
	 * 服务下线的接口
	 * 
	 * @param meta
	 * @param channel
	 * @throws InterruptedException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	public void handlePublishCancel(RegisterMeta meta, Channel channel) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {

		if (logger.isDebugEnabled()) {
			logger.info("Cancel publish {} on channel{}.", meta, channel);
		}

		attachPublishCancelEventOnChannel(meta, channel);

		final String serviceMeta = meta.getServiceName();
		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceMeta);
		if (maps.isEmpty()) {
			return;
		}

		synchronized (globalRegisterInfoMap) {

			Address address = meta.getAddress();
			RegisterMeta data = maps.remove(address);

			if (data != null) {
				this.getServiceMeta(address).remove(serviceMeta);

				if (data.getIsReviewed() == ServiceReviewState.PASS_REVIEW)
					this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriberCancel(meta);
			}
		}
	}

	/*
	 * ======================================分隔符，以上为核心方法，下面为内部方法==================
	 * ============
	 */

	private void attachPublishCancelEventOnChannel(RegisterMeta meta, Channel channel) {
		Attribute<ConcurrentSet<RegisterMeta>> attr = channel.attr(S_PUBLISH_KEY);
		ConcurrentSet<RegisterMeta> registerMetaSet = attr.get();
		if (registerMetaSet == null) {
			ConcurrentSet<RegisterMeta> newRegisterMetaSet = new ConcurrentSet<>();
			registerMetaSet = attr.setIfAbsent(newRegisterMetaSet);
			if (registerMetaSet == null) {
				registerMetaSet = newRegisterMetaSet;
			}
		}

		registerMetaSet.remove(meta);
	}

	private void attachPublishEventOnChannel(RegisterMeta meta, Channel channel) {

		Attribute<ConcurrentSet<RegisterMeta>> attr = channel.attr(S_PUBLISH_KEY);
		ConcurrentSet<RegisterMeta> registerMetaSet = attr.get();
		if (registerMetaSet == null) {
			ConcurrentSet<RegisterMeta> newRegisterMetaSet = new ConcurrentSet<>();
			registerMetaSet = attr.setIfAbsent(newRegisterMetaSet);
			if (registerMetaSet == null) {
				registerMetaSet = newRegisterMetaSet;
			}
		}

		registerMetaSet.add(meta);
	}

	private ConcurrentSet<String> getServiceMeta(Address address) {
		ConcurrentSet<String> serviceMetaSet = globalServiceMetaMap.get(address);
		if (serviceMetaSet == null) {
			ConcurrentSet<String> newServiceMetaSet = new ConcurrentSet<>();
			serviceMetaSet = globalServiceMetaMap.putIfAbsent(address, newServiceMetaSet);
			if (serviceMetaSet == null) {
				serviceMetaSet = newServiceMetaSet;
			}
		}
		return serviceMetaSet;
	}

	private ConcurrentMap<Address, RegisterMeta> getRegisterMeta(String serviceMeta) {
		ConcurrentMap<Address, RegisterMeta> maps = globalRegisterInfoMap.get(serviceMeta);
		if (maps == null) {
			ConcurrentMap<Address, RegisterMeta> newMaps = new ConcurrentHashMap<RegisterMeta.Address, RegisterMeta>();
			maps = globalRegisterInfoMap.putIfAbsent(serviceMeta, newMaps);
			if (maps == null) {
				maps = newMaps;
			}
		}
		return maps;
	}

	private void buildSubcribeResultCustomBody(ConcurrentMap<Address, RegisterMeta> maps, SubcribeResultCustomBody subcribeResultCustomBody) {

		Collection<RegisterMeta> values = maps.values();

		if (values != null && values.size() > 0) {
			List<RegisterMeta> registerMetas = new ArrayList<RegisterMeta>();
			for (RegisterMeta meta : values) {
				// 判断是否人工审核过，审核过的情况下，组装给consumer的响应主体，返回个consumer
				if (meta.getIsReviewed() == ServiceReviewState.PASS_REVIEW) {
					registerMetas.add(meta);
				}
			}
			subcribeResultCustomBody.setRegisterMeta(registerMetas);
		}
	}

	private void attachSubscribeEventOnChannel(String serviceMeta, Channel channel) {
		Attribute<ConcurrentSet<String>> attr = channel.attr(S_SUBSCRIBE_KEY);
		ConcurrentSet<String> serviceMetaSet = attr.get();
		if (serviceMetaSet == null) {
			ConcurrentSet<String> newServiceMetaSet = new ConcurrentSet<String>();
			serviceMetaSet = attr.setIfAbsent(newServiceMetaSet);
			if (serviceMetaSet == null) {
				serviceMetaSet = newServiceMetaSet;
			}
		}
		serviceMetaSet.add(serviceMeta);
	}

	public ConcurrentMap<String, ConcurrentMap<Address, RegisterMeta>> getGlobalRegisterInfoMap() {
		return globalRegisterInfoMap;
	}

	public ConcurrentMap<Address, ConcurrentSet<String>> getGlobalServiceMetaMap() {
		return globalServiceMetaMap;
	}

	/**
	 * 组装服务信息反馈给管理页面
	 * 
	 * @param serviceName
	 * @return
	 */
	private ServiceMetrics assemblyServiceMetricsByServiceName(String serviceName) {
		ServiceMetrics serviceMetrics = new ServiceMetrics();
		serviceMetrics.setServiceName(serviceName);
		serviceMetrics.setLoadBalanceStrategy(globalServiceLoadBalance.get(serviceName));
		ConcurrentMap<Address, RegisterMeta> concurrentMap = globalRegisterInfoMap.get(serviceName);
		if (null != concurrentMap && concurrentMap.keySet() != null) {
			ConcurrentMap<Address,ProviderInfo> providerInfos = new ConcurrentHashMap<Address,ProviderInfo>();
			for (Address address : concurrentMap.keySet()) {

				ProviderInfo providerInfo = new ProviderInfo();
				providerInfo.setPort(address.getPort());
				providerInfo.setHost(address.getHost());
				RegisterMeta meta = concurrentMap.get(address);
				providerInfo.setServiceReviewState(meta.getIsReviewed());
				providerInfo.setIsDegradeService(meta.isHasDegradeService());
				providerInfo.setIsVipService(meta.isVIPService());
				providerInfo.setIsSupportDegrade(meta.isSupportDegradeService());

				providerInfos.put(address, providerInfo);
			}
			serviceMetrics.setProviderMaps(providerInfos);
		}
		ConcurrentSet<Channel> channels = globalConsumerMetaMap.get(serviceName);
		if (null != channels && channels.size() > 0) {
			Set<ConsumerInfo> consumerInfos = new HashSet<ServiceMetrics.ConsumerInfo>();
			for (Channel consumerChannel : channels) {
				ConsumerInfo consumerInfo = new ConsumerInfo();
				String consumerAddress = ConnectionUtils.parseChannelRemoteAddr(consumerChannel);
				if (!"".equals(consumerAddress) && null != consumerAddress) {
					String[] s = consumerAddress.split(":");
					consumerInfo.setHost(s[0]);
					consumerInfo.setPort(Integer.parseInt(s[1]));
					consumerInfos.add(consumerInfo);
				}
			}
			serviceMetrics.setConsumerInfos(consumerInfos);
		}
		return serviceMetrics;
	}

	/**
	 * 审核服务
	 * 
	 * @param request
	 * @param channel
	 * @return
	 */
	private RemotingTransporter handleReview(String serviceName, Address address, long requestId, ServiceReviewState reviewState) {

		AckCustomBody ackCustomBody = new AckCustomBody(requestId, false);
		RemotingTransporter remotingTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, requestId);

		// 获取到这个服务的所有
		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceName);

		if (maps.isEmpty()) {
			return remotingTransporter;
		}

		synchronized (globalRegisterInfoMap) {

			// 只修改该地址提供的信息
			if (null != address) {
				RegisterMeta data = maps.get(address);

				if (data != null) {
					ackCustomBody.setSuccess(true);
					data.setIsReviewed(reviewState);
				}
			} else { // 如果传递的地址是null，说明是审核该服务的所有地址
				if (null != maps.values() && maps.values().size() > 0) {
					ackCustomBody.setSuccess(true);
					for (RegisterMeta meta : maps.values()) {
						meta.setIsReviewed(reviewState);
					}
				}
			}
		}
		return remotingTransporter;
	}

	private RemotingTransporter handleDegradeService(RemotingTransporter request, Channel channel) throws RemotingSendRequestException,
			RemotingTimeoutException, InterruptedException {

		AckCustomBody ackCustomBody = new AckCustomBody(request.getOpaque(), false);
		RemotingTransporter remotingTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, request.getOpaque());

		ManagerServiceCustomBody body = serializerImpl().readObject(request.bytes(), ManagerServiceCustomBody.class);

		String serviceName = body.getSerivceName();
		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceName);

		Address address = null;

		synchronized (globalRegisterInfoMap) {

			if (null != body.getAddress()) {

				RegisterMeta existRegiserMeta = maps.get(body.getAddress());
				if (null == existRegiserMeta) {
					return remotingTransporter;
				}
				if (existRegiserMeta.getIsReviewed() != ServiceReviewState.PASS_REVIEW) {
					return remotingTransporter;
				}

				address = existRegiserMeta.getAddress();
			}

		}
		Channel matchedProviderChannel = globalProviderChannelMetaMap.get(address);
		return defaultRegistryServer.getRemotingServer().invokeSync(matchedProviderChannel, request, 3000l);
	}

}
