package org.laopopo.remoting.watcher;

import io.netty.channel.ChannelHandler;

/**
 * 
 * @author BazingaLyn
 *
 * @time
 */
public interface ChannelHandlerHolder {

	ChannelHandler[] handlers();
	
}
