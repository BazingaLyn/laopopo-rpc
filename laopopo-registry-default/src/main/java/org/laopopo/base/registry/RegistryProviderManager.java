package org.laopopo.base.registry;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.base.registry.model.RegistryPersistRecord;
import org.laopopo.base.registry.model.RegistryPersistRecord.PersistProviderInfo;
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
import org.laopopo.common.transport.body.MetricsCustomBody;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody;
import org.laopopo.common.transport.body.SubscribeRequestCustomBody;
import org.laopopo.common.utils.PersistUtils;
import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

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
	//每个服务的历史记录
	private final ConcurrentMap<String, RegistryPersistRecord> historyRecords = new ConcurrentHashMap<String, RegistryPersistRecord>();
    //每个服务对应的负载策略
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

		RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody,channel);

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
			//历史记录中的所有服务的持久化的信息记录
			ConcurrentMap<String, RegistryPersistRecord> concurrentMap = historyRecords; 

			// 获取到这个地址可能以前注册过的注册信息
			RegisterMeta existRegiserMeta = maps.get(meta.getAddress());

			// 如果等于空，则说明以前没有注册过 这就需要从历史记录中将某些服务以前注册审核的信息恢复一下记录
			if (null == existRegiserMeta) {
				
				RegistryPersistRecord persistRecord = concurrentMap.get(serviceName);
				//如果历史记录中没有记录该信息，也就说持久化中没有记录到该信息的时候，就需要构造默认的持久化信息
				if(null == persistRecord ||!isContainChildrenInfo(persistRecord,meta.getAddress())){
					
					persistRecord = new RegistryPersistRecord();
					persistRecord.setServiceName(serviceName);															 //持久化的服务名
					persistRecord.setBalanceStrategy(LoadBalanceStrategy.WEIGHTINGRANDOM);  							 //默认的负载均衡的策略
					
					PersistProviderInfo providerInfo = new PersistProviderInfo();
					providerInfo.setAddress(meta.getAddress());															 //服务提供者的地址
					providerInfo.setIsReviewed(defaultRegistryServer.getRegistryServerConfig().getDefaultReviewState()); //服务默认是未审核
					persistRecord.getProviderInfos().add(providerInfo);
					
					concurrentMap.put(serviceName, persistRecord);
				}
				
				//循环该服务的所有服务提供者实例的信息，获取到当前实例的审核状态，设置好meta的审核信息
				for(PersistProviderInfo providerInfo:persistRecord.getProviderInfos()){
					
					if(providerInfo.getAddress().equals(meta.getAddress())){
						meta.setIsReviewed(providerInfo.getIsReviewed());
					}
				}
					
				existRegiserMeta = meta;
				maps.put(meta.getAddress(), existRegiserMeta);
			}

			this.getServiceMeta(meta.getAddress()).add(serviceName);
			
			//默认的负载均衡的策略
			LoadBalanceStrategy defaultBalanceStrategy = defaultRegistryServer.getRegistryServerConfig().getDefaultLoadBalanceStrategy();
			
			if(null != concurrentMap.get(serviceName)){
				
				RegistryPersistRecord persistRecord = concurrentMap.get(serviceName);
				if(null != persistRecord.getBalanceStrategy()){
					defaultBalanceStrategy = persistRecord.getBalanceStrategy();
				}
			} 
			
			// 设置该服务默认的负载均衡的策略
			globalServiceLoadBalance.put(serviceName, defaultBalanceStrategy);
			

			// 判断provider发送的信息已经被成功的存储的情况下，则告之服务注册成功
			ackCustomBody.setSuccess(true);

			// 如果审核通过，则通知相关服务的订阅者
			if (meta.getIsReviewed() == ServiceReviewState.PASS_REVIEW) {
				this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriber(meta, globalServiceLoadBalance.get(serviceName));
			}
		}

		//将地址与该channel绑定好，方便其他地方使用
		globalProviderChannelMetaMap.put(meta.getAddress(), channel);

		return responseTransporter;
	}

	private boolean isContainChildrenInfo(RegistryPersistRecord persistRecord, Address address) {
		List<PersistProviderInfo> infos = persistRecord.getProviderInfos();
		if(null != infos && !infos.isEmpty()){
			for(PersistProviderInfo info : infos){
				if(info.getAddress().equals(address)){
					return true;
				}
			}
		}
		return false;
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

		RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody,channel);

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
		

		//将订阅的channel上打上tag标记，表示该channel订阅的服务
		attachSubscribeEventOnChannel(serviceName, channel);

		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceName);
		// 如果订阅的暂时还没有服务提供者，则返回空列表给订阅者
		if (maps.isEmpty()) {
			return responseTransporter;
		}

		//构建返回的订阅信息的对象
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

		//将其channel上打上的标记移除掉
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

				if (data.getIsReviewed() == ServiceReviewState.PASS_REVIEW )
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
	 * @throws InterruptedException 
	 * @throws RemotingTimeoutException 
	 * @throws RemotingSendRequestException 
	 */
	private RemotingTransporter handleReview(String serviceName, Address address, long requestId, ServiceReviewState reviewState) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {

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
					ServiceReviewState serviceReviewState = data.getIsReviewed();
					data.setIsReviewed(reviewState);
					
					notifyConsumer(serviceReviewState,reviewState,data,serviceName);
					
				}
				
			} else { // 如果传递的地址是null，说明是审核该服务的所有地址
				if (null != maps.values() && maps.values().size() > 0) {
					
					ackCustomBody.setSuccess(true);
					for (RegisterMeta meta : maps.values()) {
						ServiceReviewState serviceReviewState = meta.getIsReviewed();
						meta.setIsReviewed(reviewState);
						notifyConsumer(serviceReviewState,reviewState,meta,serviceName);
					}
				}
			}
		}
		return remotingTransporter;
	}

	private void notifyConsumer(ServiceReviewState serviceReviewState, ServiceReviewState reviewState, RegisterMeta data, String serviceName) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
		//当现状态和原状态不一样的情况下，需要通知消费者
		if(serviceReviewState != reviewState){
			
			switch (reviewState) {
			case PASS_REVIEW: //如果审核通过
				this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriber(data, globalServiceLoadBalance.get(serviceName));
				break;
			case FORBIDDEN:   //如果修改成禁用
				this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriberCancel(data);
				break;
			default:
				break;
			}
		}
		
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
		if(address == null){
			return remotingTransporter;
		}else{
			Channel matchedProviderChannel = globalProviderChannelMetaMap.get(address);
			if(matchedProviderChannel != null){
				request.setCode(LaopopoProtocol.DEGRADE_SERVICE);
				request.setCustomHeader(body);
				return defaultRegistryServer.getRemotingServer().invokeSync(matchedProviderChannel, request, 3000l);
			}else{
				return remotingTransporter;
			}
		}
	}
	
	/**
	 * 持久化操作
	 * 原则：1)首先优先从globalRegisterInfoMap中持久化到库中
	 *      2)如果globalRegisterInfoMap中没有信息，则从老版本中的historyRecords中的信息重新保存到硬盘中去,这样做的好处就是不需要多维护一个historyRecords这个全局变量的信息有效性
	 *      
	 * 这样做的原因是因为，只要有服务注册到注册中心，在注册的处理的时候，已经从历史中获取到以前审核和负载的情况，所以globalRegisterInfoMap中的信息是最新的
	 * 如果有些服务以前注册过，但这次重启之后没有注册，所以就需要重新将其更新一下合并记录
	 * @throws IOException
	 */
	public void persistServiceInfo() throws IOException {
		
		Map<String,RegistryPersistRecord> persistMap = new HashMap<String, RegistryPersistRecord>();
		ConcurrentMap<String, ConcurrentMap<Address, RegisterMeta>> _globalRegisterInfoMap = this.globalRegisterInfoMap; //_stack copy
		ConcurrentMap<String, LoadBalanceStrategy> _globalServiceLoadBalance = this.globalServiceLoadBalance; //_stack copy
		ConcurrentMap<String, RegistryPersistRecord> _historyRecords = this.historyRecords;
		
		//globalRegisterInfoMap 中保存
		if(_globalRegisterInfoMap.keySet() != null){
			
			for(String serviceName : _globalRegisterInfoMap.keySet()){
				
				RegistryPersistRecord persistRecord = new RegistryPersistRecord();
				persistRecord.setServiceName(serviceName);
				persistRecord.setBalanceStrategy(_globalServiceLoadBalance.get(serviceName));
				
				List<PersistProviderInfo> providerInfos = new ArrayList<PersistProviderInfo>();
				ConcurrentMap<Address, RegisterMeta> serviceMap = _globalRegisterInfoMap.get(serviceName);
				for(Address address : serviceMap.keySet()){
					PersistProviderInfo info = new PersistProviderInfo();
					info.setAddress(address);
					info.setIsReviewed(serviceMap.get(address).getIsReviewed());
					providerInfos.add(info);
				}
				persistRecord.setProviderInfos(providerInfos);
				persistMap.put(serviceName, persistRecord);
			}
		}
		
		
		if(null != _historyRecords.keySet()){
			
			for(String serviceName :_historyRecords.keySet()){
				
				//不包含的时候
				if(!persistMap.keySet().contains(serviceName)){
					persistMap.put(serviceName, _historyRecords.get(serviceName));
				}else{
					
					//负载策略不需要合并更新，需要更新的是existRecord中没有的provider的信息
					List<PersistProviderInfo> providerInfos = new ArrayList<PersistProviderInfo>();
					RegistryPersistRecord existRecord = persistMap.get(serviceName);
					providerInfos.addAll(existRecord.getProviderInfos());
					
					//可能需要合并的信息，合并原则，如果同地址的审核策略以globalRegisterInfoMap为准，如果不同地址，则合并信息
					RegistryPersistRecord possibleMergeRecord = _historyRecords.get(serviceName);
					List<PersistProviderInfo> possibleProviderInfos = possibleMergeRecord.getProviderInfos();
					
					for(PersistProviderInfo eachPossibleInfo : possibleProviderInfos){
						
						Address address = eachPossibleInfo.getAddress();
						
						boolean exist = false;
						for(PersistProviderInfo existProviderInfo : providerInfos){
							if(existProviderInfo.getAddress().equals(address)){
								exist = true;
								break;
							}
						}
						if(!exist){
							providerInfos.add(eachPossibleInfo);
						}
					}
					existRecord.setProviderInfos(providerInfos);
					persistMap.put(serviceName, existRecord);
				}
			}
			
			if(null != persistMap.values() && !persistMap.values().isEmpty()){
				
				String jsonString = JSON.toJSONString(persistMap.values());
				
				if(jsonString != null){
					PersistUtils.string2File(jsonString, this.defaultRegistryServer.getRegistryServerConfig().getStorePathRootDir());
				}
			}
		}
	}

	public ConcurrentMap<String, RegistryPersistRecord> getHistoryRecords() {
		return historyRecords;
	}

	
	

}
