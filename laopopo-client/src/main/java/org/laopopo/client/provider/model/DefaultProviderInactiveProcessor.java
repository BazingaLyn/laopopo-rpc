package org.laopopo.client.provider.model;

import io.netty.channel.ChannelHandlerContext;

import org.laopopo.client.provider.DefaultProvider;
import org.laopopo.remoting.model.NettyChannelInactiveProcessor;

public class DefaultProviderInactiveProcessor implements NettyChannelInactiveProcessor {

	private DefaultProvider defaultProvider;

	public DefaultProviderInactiveProcessor(DefaultProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

	@Override
	public void processChannelInactive(ChannelHandlerContext ctx) {
		defaultProvider.setProviderStateIsHealthy(false);
	}

}
