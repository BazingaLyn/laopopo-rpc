package org.laopopo.client.provider;

import io.netty.channel.ChannelHandlerContext;

import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;

public class DefaultProviderRegistryProcessor implements NettyRequestProcessor {

	public DefaultProviderRegistryProcessor(DefaultProvider defaultProvider) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request) throws Exception {
		return null;
	}

}
