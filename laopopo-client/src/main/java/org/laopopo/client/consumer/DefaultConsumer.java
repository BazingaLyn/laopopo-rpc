package org.laopopo.client.consumer;

import io.netty.channel.Channel;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyRemotingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 默认的消费类
 * @time 2016年8月18日
 * @modifytime
 */
public abstract class DefaultConsumer implements Consumer, ConsumerRegistry {

	private static final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);

	private NettyClientConfig registryClientConfig;

	private NettyClientConfig providerClientConfig;

	private ConsumerConfig consumerConfig;

	private NettyRemotingClient registryNettyRemotingClient;

	private NettyRemotingClient providerNettyRemotingClient;

	private DefaultConsumerRegistry defaultConsumerRegistry;
	
	private ConsumerManager consumerManager;

	private Channel registyChannel;

	public DefaultConsumer(NettyClientConfig registryClientConfig, NettyClientConfig providerClientConfig, ConsumerConfig consumerConfig) {
		this.registryClientConfig = registryClientConfig;
		this.providerClientConfig = providerClientConfig;
		this.consumerConfig = consumerConfig;
		defaultConsumerRegistry = new DefaultConsumerRegistry(this);
		consumerManager = new ConsumerManager(this);
		initialize();
	}

	private void initialize() {
		this.registryNettyRemotingClient = new NettyRemotingClient(this.registryClientConfig);
		this.providerNettyRemotingClient = new NettyRemotingClient(this.providerClientConfig);

		// 注册处理器
		this.registerProcessor();

	}

	private void registerProcessor() {
		this.registryNettyRemotingClient.registerProcessor(LaopopoProtocol.SUBCRIBE_RESULT, new DefaultConsumerRegistryProcessor(this), null);
		this.registryNettyRemotingClient.registerProcessor(LaopopoProtocol.SUBCRIBE_SERVICE_CANCEL, new DefaultConsumerRegistryProcessor(this), null);
	}

	 @Override
	public void subcribeService(SubcribeService... subcribeServices) {
		 if(subcribeServices.length > 0){
			 this.defaultConsumerRegistry.subcribeService(subcribeServices);
		 }
		
	}
	
	@Override
	public void start() {
		this.registryNettyRemotingClient.start();
		getOrUpdateHealthyChannel();
	}

	@Override
	public void getOrUpdateHealthyChannel() {
		
		String addresses = this.registryClientConfig.getDefaultAddress();

		if(registyChannel != null && registyChannel.isActive()&& registyChannel.isWritable())
			return;
		
		if (addresses == null || "".equals(addresses)) {
			logger.error("registry address is empty");
			return;
		}
		int retryConnectionTimes = this.consumerConfig.getRetryConnectionRegistryTimes();
		long maxTimeout = this.consumerConfig.getMaxRetryConnectionRegsitryTime();

		String[] adds = addresses.split(",");
		
		for(int i = 0;i < adds.length;i++){
			
			if(registyChannel != null && registyChannel.isActive()&& registyChannel.isWritable())
				return;
			
			String currentAddress = adds[i];
			final long beginTimestamp = System.currentTimeMillis();
			long endTimestamp = beginTimestamp;
			
			int times = 0;

			 for (; times < retryConnectionTimes && (endTimestamp - beginTimestamp) < maxTimeout; times++) {
				 try {
					Channel channel = registryNettyRemotingClient.createChannel(currentAddress);
					if(channel != null && channel.isActive()&& channel.isWritable()){
						this.registyChannel = channel;
						break;
					}else{
						continue;
					}
				} catch (InterruptedException e) {
					logger.warn("connection registry center [{}] fail",currentAddress);
					endTimestamp = System.currentTimeMillis();
					continue;
				}
			 }
		}

	}

	public NettyRemotingClient getRegistryNettyRemotingClient() {
		return registryNettyRemotingClient;
	}

	public void setRegistryNettyRemotingClient(NettyRemotingClient registryNettyRemotingClient) {
		this.registryNettyRemotingClient = registryNettyRemotingClient;
	}

	public Channel getRegistyChannel() {
		return registyChannel;
	}

	public void setRegistyChannel(Channel registyChannel) {
		this.registyChannel = registyChannel;
	}

	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
	}

	public NettyClientConfig getRegistryClientConfig() {
		return registryClientConfig;
	}

	public void setRegistryClientConfig(NettyClientConfig registryClientConfig) {
		this.registryClientConfig = registryClientConfig;
	}

	public ConsumerManager getConsumerManager() {
		return consumerManager;
	}

	public void setConsumerManager(ConsumerManager consumerManager) {
		this.consumerManager = consumerManager;
	}

	public NettyRemotingClient getProviderNettyRemotingClient() {
		return providerNettyRemotingClient;
	}

	public void setProviderNettyRemotingClient(NettyRemotingClient providerNettyRemotingClient) {
		this.providerNettyRemotingClient = providerNettyRemotingClient;
	}


}
