package org.laopopo.client.comsumer;

import java.util.List;

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
public abstract class DefaultConsumer implements Consumer,ConsumerRegistry {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);
	
	private NettyClientConfig registryClientConfig;
	
	private NettyClientConfig providerClientConfig;
	
	private NettyRemotingClient registryNettyRemotingClient;
	
	private NettyRemotingClient providerNettyRemotingClient;
	
	private DefaultConsumerRegistry defaultConsumerRegistry;
	
	public DefaultConsumer(NettyClientConfig registryClientConfig, NettyClientConfig providerClientConfig) {
		this.registryClientConfig = registryClientConfig;
		this.providerClientConfig = providerClientConfig;
		defaultConsumerRegistry = new DefaultConsumerRegistry(this);
		initialize();
	}

	private void initialize() {
		this.registryNettyRemotingClient = new NettyRemotingClient(this.registryClientConfig);
		this.providerNettyRemotingClient = new NettyRemotingClient(this.providerClientConfig);
		
		//注册处理器
	    this.registerProcessor();
		
	}

	private void registerProcessor() {
		this.registryNettyRemotingClient.registerProcessor(LaopopoProtocol.SUBCRIBE_RESULT, new DefaultConsumerRegistryProcessor(this), null);
	    this.registryNettyRemotingClient.registerProcessor(LaopopoProtocol.SUBCRIBE_SERVICE_CANCEL, new DefaultConsumerRegistryProcessor(this), null);
//	    this.registryNettyRemotingClient.registerProcessor(LaopopoProtocol.SUBCRIBE_SERVICE_CANCEL, new DefaultConsumerRegistryProcessor(this), null);
	}


	@Override
	public void subcribeService(List<SubcribeService> subcribeServices) {
		if(null != subcribeServices && !subcribeServices.isEmpty()){
			this.defaultConsumerRegistry.subcribeService(subcribeServices);
		}
	}
	
	
	
	
	

}
