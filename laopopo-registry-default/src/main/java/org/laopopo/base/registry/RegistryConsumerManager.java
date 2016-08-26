package org.laopopo.base.registry;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.List;

import org.laopopo.common.exception.remoting.RemotingSendRequestException;
import org.laopopo.common.exception.remoting.RemotingTimeoutException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.rpc.RegisterMeta;
import org.laopopo.common.transport.body.AckCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody;
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

	private volatile ChannelGroup subscriberChannels = new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);

	private final ConcurrentSet<MessageNonAck> messagesNonAcks = new ConcurrentSet<MessageNonAck>();

	public RegistryConsumerManager(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
	}

	public ChannelGroup getSubscriberChannels() {
		return subscriberChannels;
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
		buildSubcribeResultCustomBody(meta, subcribeResultCustomBody);

		// 传送给consumer对象的RemotingTransporter
		RemotingTransporter sendConsumerRemotingTrasnporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.SUBCRIBE_RESULT,
				subcribeResultCustomBody);

		pushMessageToConsumer(sendConsumerRemotingTrasnporter, meta.getServiceName());

	}

	public void notifyMacthedSubscriberCancel(final RegisterMeta meta) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {

		// 构建订阅通知的主体传输对象
		SubcribeResultCustomBody subcribeResultCustomBody = new SubcribeResultCustomBody();
		buildSubcribeResultCustomBody(meta, subcribeResultCustomBody);

		RemotingTransporter sendConsumerRemotingTrasnporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.SUBCRIBE_SERVICE_CANCEL,
				subcribeResultCustomBody);
		
		pushMessageToConsumer(sendConsumerRemotingTrasnporter, meta.getServiceName());

	}
	
	
	
	/**
	 * 检查messagesNonAcks中是否有发送失败的信息，然后再次发送
	 */
	public void checkSendFailedMessage(){
		ConcurrentSet<MessageNonAck> nonAcks = messagesNonAcks;
		messagesNonAcks.clear();
		if(nonAcks != null){
			for(MessageNonAck messageNonAck:nonAcks){
				try {
					pushMessageToConsumer(messageNonAck.getMsg(), messageNonAck.getServiceName());
				} catch (Exception e) {
					logger.error("send message failed");
				} 
			}
		}
		nonAcks = null; //help GC
	}
	
	/***************************分隔符，上面为对外方法*****************************************************/

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
	 * 
	 * @param meta
	 * @param subcribeResultCustomBody
	 */
	private void buildSubcribeResultCustomBody(RegisterMeta meta, SubcribeResultCustomBody subcribeResultCustomBody) {
		List<RegisterMeta> registerMetas = new ArrayList<RegisterMeta>();

		registerMetas.add(meta);
		subcribeResultCustomBody.setRegisterMeta(registerMetas);
	}

	private void pushMessageToConsumer(RemotingTransporter sendConsumerRemotingTrasnporter, String serviceName) throws RemotingSendRequestException,
			RemotingTimeoutException, InterruptedException {
		// 所有的订阅者的channel集合
		if (!subscriberChannels.isEmpty()) {
			for (Channel channel : subscriberChannels) {
				if (isChannelSubscribeOnServiceMeta(serviceName, channel)) {
					RemotingTransporter remotingTransporter = this.defaultRegistryServer.getRemotingServer().invokeSync(channel,
							sendConsumerRemotingTrasnporter, 3000l);
					// 如果是ack返回是null说明是超时了，需要重新发送
					if (remotingTransporter == null) {
						logger.warn("push consumer message time out,need send again");
						MessageNonAck msgNonAck = new MessageNonAck(remotingTransporter, channel,serviceName);
						messagesNonAcks.add(msgNonAck);
					}
					// 如果消费者端消费者消费失败
					AckCustomBody ackCustomBody = (AckCustomBody) remotingTransporter.getCustomHeader();
					if (!ackCustomBody.isSuccess()) {
						logger.warn("consumer fail handler this message");
						MessageNonAck msgNonAck = new MessageNonAck(remotingTransporter, channel,serviceName);
						messagesNonAcks.add(msgNonAck);
					}
				}
			}
		}
	}
	
	
	
	static class MessageNonAck {

		private final long id;

		private final String serviceName;
		private final RemotingTransporter msg;
		private final Channel channel;

		public MessageNonAck(RemotingTransporter msg, Channel channel,String serviceName) {
			this.msg = msg;
			this.channel = channel;
			this.serviceName = serviceName;

			id = msg.getOpaque();
		}

		public long getId() {
			return id;
		}

		public RemotingTransporter getMsg() {
			return msg;
		}

		public Channel getChannel() {
			return channel;
		}

		public String getServiceName() {
			return serviceName;
		}
		
		

	}

}
