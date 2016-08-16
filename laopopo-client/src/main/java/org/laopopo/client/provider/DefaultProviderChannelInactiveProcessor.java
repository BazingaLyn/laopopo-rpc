package org.laopopo.client.provider;

import io.netty.channel.ChannelHandlerContext;

import org.laopopo.remoting.model.NettyChannelInactiveProcessor;

public class DefaultProviderChannelInactiveProcessor implements NettyChannelInactiveProcessor {

	public DefaultProviderChannelInactiveProcessor(DefaultProvider defaultProvider) {
	}

	@Override
	public void processChannelInactive(ChannelHandlerContext ctx) throws Exception {
		
	}

}
