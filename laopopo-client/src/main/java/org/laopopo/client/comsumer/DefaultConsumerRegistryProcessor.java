package org.laopopo.client.comsumer;

import io.netty.channel.ChannelHandlerContext;

import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;

public class DefaultConsumerRegistryProcessor implements NettyRequestProcessor {

	public DefaultConsumerRegistryProcessor(DefaultConsumer defaultConsumer) {
	}

	@Override
	public RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request) throws Exception {
		return null;
	}

}
