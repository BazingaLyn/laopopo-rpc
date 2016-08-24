package org.laopopo.base.registry;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.List;

import org.laopopo.common.exception.remoting.RemotingSendRequestException;
import org.laopopo.common.exception.remoting.RemotingTimeoutException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.SubcribeResultCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody.ServiceInfo;
import org.laopopo.registry.model.RegisterMeta;
import org.laopopo.registry.model.RegisterMeta.Address;
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

	private static final AttributeKey<ConcurrentSet<String>> S_SUBSCRIBE_KEY = AttributeKey.valueOf("server.subscribed");

	private final ChannelGroup subscriberChannels = new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);

	public RegistryConsumerManager(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
	}

	public ChannelGroup getSubscriberChannels() {
		return subscriberChannels;
	}

	/**
	 * 通知consumer该地址的所有服务都下线
	 * 
	 * @param address
	 *            
	 */
	public void handleOfflineNotice(Address address) {

	}

	/**
	 * 通知相关的订阅者服务的信息
	 * 
	 * @param meta
	 * @throws InterruptedException 
	 * @throws RemotingTimeoutException 
	 * @throws RemotingSendRequestException 
	 */
	public void notifyMacthedSubscriber(final RegisterMeta meta) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
		
		// 构建订阅通知的主体传输对象
		SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
		buildSubcribeResultCustomBody(meta,subcribeResultCustomBody);

		// 传送给consumer对象的RemotingTransporter
		RemotingTransporter sendConsumerRemotingTrasnporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.SUBCRIBE_RESULT,
				subcribeResultCustomBody);

		// 所有的订阅者的channel集合
		if(!subscriberChannels.isEmpty()){
			for(Channel channel :subscriberChannels){
				if(isChannelSubscribeOnServiceMeta(meta.getServiceName(), channel)){
					RemotingTransporter remotingTransporter = this.defaultRegistryServer.getRemotingServer().invokeSync(channel, sendConsumerRemotingTrasnporter, 3000l);
				    if(remotingTransporter == null){
				    	//超时重发
				    }
				}
			}
		}
	}


	public void notifyMacthedSubscriberCancel(final RegisterMeta meta) {
		
		// 构建订阅通知的主体传输对象
		SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
		buildSubcribeResultCustomBody(meta,subcribeResultCustomBody);
		
		RemotingTransporter sendConsumerRemotingTrasnporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.SUBCRIBE_SERVICE_CANCEL,
				subcribeResultCustomBody);
		
		// 所有的订阅者的channel集合
		if(!subscriberChannels.isEmpty()){
			
		}
		subscriberChannels.writeAndFlush(sendConsumerRemotingTrasnporter, new ChannelMatcher() {

			@Override
			public boolean matches(Channel channel) {
				//
				boolean doSend = isChannelSubscribeOnServiceMeta(meta.getServiceName(), channel);
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

	/**
	 * 因为在consumer订阅服务的时候，就会在其channel上绑定其订阅的信息
	 * 
	 * @param serviceMeta
	 * @param channel
	 * @return
	 */
	private boolean isChannelSubscribeOnServiceMeta(String serviceName, Channel channel) {
		ConcurrentSet<String> serviceMetaSet = channel.attr(S_SUBSCRIBE_KEY).get();

		return serviceMetaSet != null && serviceMetaSet.contains(serviceName);
	}
	
	/**
	 * 构建返回给consumer的返回主体对象
	 * @param meta
	 * @param subcribeResultCustomBody
	 */
	private void buildSubcribeResultCustomBody(RegisterMeta meta, SubcribeResultCustomBody subcribeResultCustomBody) {
		List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();	
		
		ServiceInfo  info = new ServiceInfo(meta.getAddress().getHost(), // 服务的提供地址
				meta.getAddress().getPort(), // 服务提供端口
				meta.getServiceName(),
				meta.isVIPService(),
				meta.getWeight(),
				meta.getConnCount()); // 是否为VIP服务 如果是，consumer调用的时候就会port-2 连接调用
		serviceInfos.add(info);
		subcribeResultCustomBody.setServiceInfos(serviceInfos);
	}

	

}
