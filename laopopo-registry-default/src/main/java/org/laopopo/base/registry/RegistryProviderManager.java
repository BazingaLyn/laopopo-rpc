package org.laopopo.base.registry;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import static org.laopopo.common.utils.Constants.ACK_PUBLISH_CANCEL_FAILURE;
import static org.laopopo.common.utils.Constants.ACK_PUBLISH_CANCEL_SUCCESS;
import static org.laopopo.common.utils.Constants.ACK_PUBLISH_FAILURE;
import static org.laopopo.common.utils.Constants.ACK_PUBLISH_SUCCESS;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.AckCustomBody;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.common.transport.body.SubcribeRequestCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody.ServiceInfo;
import org.laopopo.registry.model.RegisterMeta;
import org.laopopo.registry.model.RegisterMeta.Address;
import org.laopopo.registry.model.ServiceReviewState;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 注册服务中心端的provider侧的管理
 * 
 * @notice 如果用户在monitor端配置某个服务的权重，某个服务的审核，某个服务的手动降级 等操作，默认1分钟后生效
 * @time 2016年8月15日
 * @modifytime
 */
public class RegistryProviderManager implements RegistryProviderServer {

	private static final Logger logger = LoggerFactory.getLogger(RegistryProviderManager.class);

	private static final AttributeKey<ConcurrentSet<String>> S_SUBSCRIBE_KEY = AttributeKey.valueOf("server.subscribed");
	private static final AttributeKey<ConcurrentSet<RegisterMeta>> S_PUBLISH_KEY = AttributeKey.valueOf("server.published");

	private DefaultRegistryServer defaultRegistryServer;
	
	private final ConcurrentMap<String, ConcurrentMap<Address, RegisterMeta>> globalRegisterInfoMap = new ConcurrentHashMap<String, ConcurrentMap<Address,RegisterMeta>>();

	// 指定节点都注册了哪些服务
    private final ConcurrentMap<Address, ConcurrentSet<String>> globalServiceMetaMap = new ConcurrentHashMap<RegisterMeta.Address, ConcurrentSet<String>>();
	
    private final ConcurrentMap<String, ConcurrentSet<Channel>> globalConsumerMetaMap = new ConcurrentHashMap<String, ConcurrentSet<Channel>>();
    
    
    public RegistryProviderManager(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
	}

	/**
	 * 处理provider服务注册
	 */
	@Override
	public RemotingTransporter handlerRegister(RemotingTransporter remotingTransporter, Channel channel) {

		//准备好ack信息返回个provider，悲观主义，默认返回失败ack，要求provider重新发送请求
		AckCustomBody ackCustomBody = new AckCustomBody(remotingTransporter.getOpaque(), false, ACK_PUBLISH_FAILURE);
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody,
				remotingTransporter.getOpaque());

		//接收到主体信息
		PublishServiceCustomBody publishServiceCustomBody = serializerImpl().readObject(remotingTransporter.bytes(), PublishServiceCustomBody.class);

		RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody);

		if (logger.isDebugEnabled()) {
			logger.info("Publish [{}] on channel[{}].", meta, channel);
		}

		// channel上打上该服务的标记 方便当channel inactive的时候，直接从channel上拿到标记的属性，通知
		attachPublishEventOnChannel(meta, channel);

		//一个服务的最小单元，也是确定一个服务的最小单位
		final String serviceMeta = meta.getServiceName();
		//找出提供此服务的全部地址和该服务在该地址下的审核情况
		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceMeta);
		
		synchronized (globalRegisterInfoMap) {
			//将当前的地址也put进去，如果返回是null，说明是第一次注册，则将其该地址的服务列表中加入该服务
			if (maps.putIfAbsent(meta.getAddress(), meta) == null) {
                this.getServiceMeta(meta.getAddress()).add(serviceMeta);
			}
			
			//判断provider发送的信息已经被成功的存储的情况下，则告之服务注册成功
			ackCustomBody.setDesc(ACK_PUBLISH_SUCCESS);
			ackCustomBody.setSuccess(true);
			
			//如果审核通过，则通知相关服务的订阅者
			if(meta.getIsReviewed() == ServiceReviewState.PASS_REVIEW){
				this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriber(meta);
			}
		}
		
		return responseTransporter;
	}
	
	/**
	 * provider端发送的请求，取消对某个服务的提供
	 * @param request
	 * @param channel
	 * @return
	 */
	public RemotingTransporter handlerRegisterCancel(RemotingTransporter request, Channel channel) {
		
		//准备好ack信息返回个provider，悲观主义，默认返回失败ack，要求provider重新发送请求
		AckCustomBody ackCustomBody = new AckCustomBody(request.getOpaque(), false, ACK_PUBLISH_CANCEL_FAILURE);
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody,
				request.getOpaque());
		
		//接收到主体信息
		PublishServiceCustomBody publishServiceCustomBody = serializerImpl().readObject(request.bytes(), PublishServiceCustomBody.class);

		RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody);
		
		handlePublishCancel(meta, channel);
		
		ackCustomBody.setDesc(ACK_PUBLISH_CANCEL_SUCCESS);
		ackCustomBody.setSuccess(true);
		
		return responseTransporter;
	}
	
	/**
	 * 处理consumer的消息订阅，并返回结果
	 * @param request
	 * @param channel
	 * @return
	 */
	public RemotingTransporter handleSubscribe(RemotingTransporter request, Channel channel) {
		
		SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.SUBCRIBE_RESULT, subcribeResultCustomBody, request.getOpaque());
		//接收到主体信息
		SubcribeRequestCustomBody requestCustomBody = serializerImpl().readObject(request.bytes(), SubcribeRequestCustomBody.class);
		String serviceMeta = requestCustomBody.getServiceName();
		//将其降入到channel的group中去
		this.defaultRegistryServer.getConsumerManager().getSubscriberChannels().add(channel);
		
		//存入到消费者中全局变量中去 TODO is need?
		ConcurrentSet<Channel> channels = globalConsumerMetaMap.get(serviceMeta);
		if(null == channels){
			channels = new ConcurrentSet<Channel>();
		}
		channels.add(channel);
		
		
		attachSubscribeEventOnChannel(serviceMeta, channel);
		
		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceMeta);
		//如果订阅的暂时还没有服务提供者，则返回空列表给订阅者
        if (maps.isEmpty()) {
        	return responseTransporter;
        }
        
        buildSubcribeResultCustomBody(maps,subcribeResultCustomBody);
        
		return responseTransporter;
	}
	
	/***
	 * 服务下线的接口
	 * @param meta
	 * @param channel
	 */
	public void handlePublishCancel(RegisterMeta meta, Channel channel) {

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
                
                this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriberCancel(meta);
            }
		}
	}
	
	/**
	 * 审核服务
	 * @param request
	 * @param channel
	 * @return
	 */
	public RemotingTransporter handleReview(RemotingTransporter request, Channel channel) {
		
		if (logger.isDebugEnabled()) {
			logger.info("review service {} on channel{}.", request, channel);
		}
		
		return null;
	}

	

	/*
	 * ======================================分隔符，以上为核心方法，下面为内部方法==============================
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


	private boolean isChannelSubscribeOnServiceMeta(String serviceMeta, Channel channel) {
//		ConcurrentSet<ServiceMeta> serviceMetaSet = channel.attr(S_SUBSCRIBE_KEY).get();
//
//		return serviceMetaSet != null && serviceMetaSet.contains(serviceMeta);
		return true;
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
		
		if(values != null && values.size() > 0){
			List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();
			for(RegisterMeta meta : values){
				//判断是否人工审核过，审核过的情况下，组装给consumer的响应主体，返回个consumer
				if(meta.getIsReviewed() == ServiceReviewState.PASS_REVIEW){
					
					ServiceInfo serviceInfo = new ServiceInfo(meta.getAddress().getHost(), meta.getAddress().getPort(), meta.getServiceName(), meta.isVIPService(),meta.getWeight(),meta.getConnCount());
					serviceInfos.add(serviceInfo);
				}
			}
			subcribeResultCustomBody.setServiceInfos(serviceInfos);
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


}
