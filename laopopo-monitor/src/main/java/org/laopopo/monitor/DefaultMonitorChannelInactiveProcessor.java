package org.laopopo.monitor;

import io.netty.channel.ChannelHandlerContext;

import org.laopopo.remoting.model.NettyChannelInactiveProcessor;

public class DefaultMonitorChannelInactiveProcessor implements NettyChannelInactiveProcessor {

	public DefaultMonitorChannelInactiveProcessor(DefaultMonitor defaultMonitor) {
	}

	@Override
	public void processChannelInactive(ChannelHandlerContext ctx) {
		
	}

}
