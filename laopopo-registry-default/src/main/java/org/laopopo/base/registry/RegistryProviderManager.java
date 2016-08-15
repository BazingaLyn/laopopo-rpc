package org.laopopo.base.registry;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.CommonCustomBody;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody;
import org.laopopo.registry.model.RegisterMeta;
import org.laopopo.registry.model.RegisterMeta.Address;
import org.laopopo.registry.model.RegisterMeta.ServiceMeta;
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

	private final ConcurrentMap<ServiceMeta, ConcurrentMap<Address, RegisterMeta>> globalRegisterInfoMap = new ConcurrentHashMap<RegisterMeta.ServiceMeta, ConcurrentMap<Address, RegisterMeta>>();
	// 指定节点都注册了哪些服务
	private final ConcurrentMap<Address, ConcurrentSet<ServiceMeta>> globalServiceMetaMap = new ConcurrentHashMap<RegisterMeta.Address, ConcurrentSet<ServiceMeta>>();

	public RegistryProviderManager(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
	}

	@Override
	public RemotingTransporter handlerRegister(RemotingTransporter remotingTransporter, Channel channel) {

		CommonCustomBody comonCustomBody = remotingTransporter.getCustomHeader();
		if (comonCustomBody instanceof PublishServiceCustomBody) {

			PublishServiceCustomBody publishServiceCustomBody = (PublishServiceCustomBody) comonCustomBody;
			RegisterMeta meta = RegisterMeta.createRegiserMeta(publishServiceCustomBody);

			if (logger.isDebugEnabled()) {
				logger.info("Publish [{}] on channel[{}].", meta, channel);
			}

			// channel上打上该服务的标记
			attachPublishEventOnChannel(meta, channel);

			final ServiceMeta serviceMeta = meta.getServiceMeta();
			ConcurrentMap<Address, RegisterMeta> config = this.getRegisterMeta(serviceMeta);
			synchronized (globalRegisterInfoMap) {

				if (config.putIfAbsent(meta.getAddress(), meta) == null) {
					this.getServiceMeta(meta.getAddress()).add(serviceMeta);
				}

				SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody(meta.getAddress().getHost(), meta.getAddress().getPort(), meta
						.getServiceMeta().getGroup(), meta.getServiceMeta().getVersion(), meta.getServiceMeta().getServiceProviderName(), meta.getServiceMeta()
						.isVIPService());

				RemotingTransporter sendConsumerRemotingTrasnporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.SUBCRIBE_RESULT,
						subcribeResultCustomBody, LaopopoProtocol.REQUEST_REMOTING);

				 //该接口管理员是否审核通过
				 if(this.defaultRegistryServer.getMonitorConfigManager().isReviewed(meta)){
					 
					 this.defaultRegistryServer.getConsumerManager().getSubscriberChannels().writeAndFlush(sendConsumerRemotingTrasnporter, new ChannelMatcher() {

						@Override
						public boolean matches(Channel channel) {
							boolean doSend = isChannelSubscribeOnServiceMeta(serviceMeta, channel);
							//TODO
//	                        if (doSend) {
//	                            MessageNonAck msgNonAck = new MessageNonAck(serviceMeta, msg, channel);
//	                            // 收到ack后会移除当前key(参见handleAcknowledge), 否则超时超时重发
//	                            messagesNonAck.put(msgNonAck.id, msgNonAck);
//	                        }
	                        return doSend;
						}

					 });
				 }

			}

		}
		//TODO return ack
		return null;
	}

	public void handlePublishCancel(RegisterMeta meta, Channel channel) {
		
         if(logger.isDebugEnabled()){
        	 logger.info("Cancel publish {} on channel{}.", meta, channel);
         }
         
         attachPublishCancelEventOnChannel(meta, channel);
         
         final ServiceMeta serviceMeta = meta.getServiceMeta();
         ConcurrentMap<Address, RegisterMeta> config = this.getRegisterMeta(serviceMeta);
         if (config.isEmpty()) {
             return;
         }
         
         synchronized (globalRegisterInfoMap) {
        	 
         }
         
	}
	
	
	
	/*======================================分隔符，以上为核心方法，下面为内部方法=======================*/

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

	public ConcurrentMap<Address, RegisterMeta> getRegisterMeta(ServiceMeta serviceMeta) {
		ConcurrentMap<Address, RegisterMeta> config = globalRegisterInfoMap.get(serviceMeta);
		if (config == null) {
			ConcurrentMap<Address, RegisterMeta> newConfig = new ConcurrentHashMap<RegisterMeta.Address, RegisterMeta>();
			config = globalRegisterInfoMap.putIfAbsent(serviceMeta, newConfig);
			if (config == null) {
				config = newConfig;
			}
		}
		return config;
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
