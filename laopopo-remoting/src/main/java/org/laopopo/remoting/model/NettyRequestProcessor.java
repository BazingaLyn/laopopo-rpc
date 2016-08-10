package org.laopopo.remoting.model;

import io.netty.channel.ChannelHandlerContext;

public interface NettyRequestProcessor {
	
	RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter transporter)
            throws Exception;

}
