package org.laopopo.base.registry;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import static org.laopopo.common.utils.Constants.ACK_PUBLISH_FAILURE;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.AckCustomBody;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.registry.model.RegisterMeta;
import org.laopopo.registry.model.RegisterMeta.Address;
import org.laopopo.registry.model.ServiceMeta;
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

	private static final AttributeKey<ConcurrentSet<ServiceMeta>> S_SUBSCRIBE_KEY = AttributeKey.valueOf("server.subscribed");
	private static final AttributeKey<ConcurrentSet<RegisterMeta>> S_PUBLISH_KEY = AttributeKey.valueOf("server.published");

	private DefaultRegistryServer defaultRegistryServer;
	
	private final ConcurrentMap<ServiceMeta, ConcurrentMap<Address, RegisterMeta>> globalRegisterInfoMap = new ConcurrentHashMap<ServiceMeta, ConcurrentMap<Address,RegisterMeta>>();

	// 指定节点都注册了哪些服务
    private final ConcurrentMap<Address, ConcurrentSet<ServiceMeta>> globalServiceMetaMap = new ConcurrentHashMap<RegisterMeta.Address, ConcurrentSet<ServiceMeta>>();
	public RegistryProviderManager(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
	}

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
		final ServiceMeta serviceMeta = meta.getServiceMeta();
		//找出提供此服务的全部地址和该服务在该地址下的审核情况
		ConcurrentMap<Address, RegisterMeta> maps = this.getRegisterMeta(serviceMeta);
		
		synchronized (globalRegisterInfoMap) {
			//将当前的地址也put进去，如果返回是null，说明是第一次注册，则将其该地址的服务列表中加入该服务
			if (maps.putIfAbsent(meta.getAddress(), meta) == null) {
                this.getServiceMeta(meta.getAddress()).add(serviceMeta);
			}
			
			//如果审核通过，则通知相关服务的订阅者
			if(meta.getIsReviewed() == ServiceReviewState.PASS_REVIEW){
				
				this.defaultRegistryServer.getConsumerManager().notifyMacthedSubscriber(meta);
				
			}
		}
		
		return responseTransporter;
	}

	private ConcurrentSet<ServiceMeta> getServiceMeta(Address address) {
		ConcurrentSet<ServiceMeta> serviceMetaSet = globalServiceMetaMap.get(address);
        if (serviceMetaSet == null) {
            ConcurrentSet<ServiceMeta> newServiceMetaSet = new ConcurrentSet<>();
            serviceMetaSet = globalServiceMetaMap.putIfAbsent(address, newServiceMetaSet);
            if (serviceMetaSet == null) {
                serviceMetaSet = newServiceMetaSet;
            }
        }
        return serviceMetaSet;
	}

	private ConcurrentMap<Address, RegisterMeta> getRegisterMeta(ServiceMeta serviceMeta) {
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

	public void handlePublishCancel(RegisterMeta meta, Channel channel) {

		if (logger.isDebugEnabled()) {
			logger.info("Cancel publish {} on channel{}.", meta, channel);
		}

//		attachPublishCancelEventOnChannel(meta, channel);
//
//		final ServiceMeta serviceMeta = meta.getServiceMeta();
//		ConcurrentMap<Address, RegisterMeta> config = this.getRegisterMeta(serviceMeta);
//		if (config.isEmpty()) {
//			return;
//		}


	}

	/*
	 * ======================================分隔符，以上为核心方法，下面为内部方法=======================
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


	private boolean isChannelSubscribeOnServiceMeta(ServiceMeta serviceMeta, Channel channel) {
		ConcurrentSet<ServiceMeta> serviceMetaSet = channel.attr(S_SUBSCRIBE_KEY).get();

		return serviceMetaSet != null && serviceMetaSet.contains(serviceMeta);
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

}
