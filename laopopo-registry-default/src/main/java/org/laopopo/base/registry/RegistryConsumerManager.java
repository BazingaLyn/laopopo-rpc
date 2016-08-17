package org.laopopo.base.registry;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.SubcribeResultCustomBody;
import org.laopopo.registry.model.RegisterMeta;
import org.laopopo.registry.model.RegisterMeta.Address;
import org.laopopo.registry.model.ServiceMeta;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 注册中心模块消费端的管理
 * @time 2016年8月15日
 * @modifytime
 */
public class RegistryConsumerManager {
	
	private static final Logger logger = LoggerFactory.getLogger(RegistryConsumerManager.class);
	
	private DefaultRegistryServer defaultRegistryServer;
	
	private static final AttributeKey<ConcurrentSet<ServiceMeta>> S_SUBSCRIBE_KEY = AttributeKey.valueOf("server.subscribed");
	
	private final ChannelGroup subscriberChannels = new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);

	public RegistryConsumerManager(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
	}

	public ChannelGroup getSubscriberChannels() {
		return subscriberChannels;
	}

	public void handleOfflineNotice(Address address) {
		
	}

	/**
	 * 通知相关的订阅者服务的信息
	 * @param meta
	 */
	public void notifyMacthedSubscriber(final RegisterMeta meta) {
		
		SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody(meta.getAddress().getHost(), meta.getAddress().getPort(), meta
				.getServiceMeta().getGroup(), meta.getServiceMeta().getVersion(), meta.getServiceMeta().getServiceProviderName(), meta.getServiceMeta()
				.isVIPService());

		RemotingTransporter sendConsumerRemotingTrasnporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.SUBCRIBE_RESULT,
				subcribeResultCustomBody);
		
		subscriberChannels.writeAndFlush(sendConsumerRemotingTrasnporter, new ChannelMatcher() {

			@Override
			public boolean matches(Channel channel) {
				boolean doSend = isChannelSubscribeOnServiceMeta(meta.getServiceMeta(), channel);
				// TODO
				// if (doSend) {
				// MessageNonAck msgNonAck = new
				// MessageNonAck(serviceMeta, msg, channel);
				// // 收到ack后会移除当前key(参见handleAcknowledge), 否则超时超时重发
				// messagesNonAck.put(msgNonAck.id, msgNonAck);
				// }
				return doSend;
			}

		});
	}
	
	private boolean isChannelSubscribeOnServiceMeta(ServiceMeta serviceMeta, Channel channel) {
		ConcurrentSet<ServiceMeta> serviceMetaSet = channel.attr(S_SUBSCRIBE_KEY).get();

		return serviceMetaSet != null && serviceMetaSet.contains(serviceMeta);
	}
	

}
