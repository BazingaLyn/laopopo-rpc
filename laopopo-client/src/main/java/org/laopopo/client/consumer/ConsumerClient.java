package org.laopopo.client.consumer;

import org.laopopo.remoting.netty.NettyClientConfig;

public class ConsumerClient extends DefaultConsumer {

	 

	public ConsumerClient(NettyClientConfig registryClientConfig, NettyClientConfig providerClientConfig, ConsumerConfig consumerConfig) {
		super(registryClientConfig, providerClientConfig, consumerConfig);
	}

	@Override
	public Object call(String serviceName, Object... args) throws Throwable {
		return null;
	}

	@Override
	public Object call(String serviceName, long timeout, Object... args) throws Throwable {
		return null;
	}

}
