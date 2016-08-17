package org.laopopo.monitor;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import org.laopopo.remoting.model.RemotingTransporter;

/**
 * 
 * @author BazingaLyn
 * @description monitor端的核心控制器
 * @time
 * @modifytime
 */
public class MonitorController {
	
	private DefaultMonitor defaultMonitor;
	
	private ChannelGroup registryChannels = new DefaultChannelGroup("regisrters", GlobalEventExecutor.INSTANCE);
	private ChannelGroup providerChannels = new DefaultChannelGroup("providers", GlobalEventExecutor.INSTANCE);

	public MonitorController(DefaultMonitor defaultMonitor) {
		this.defaultMonitor = defaultMonitor;
	}

	/**
	 * registry
	 * @param request
	 * @param channel
	 * @return
	 */
	public RemotingTransporter handlerCheckIsReview(RemotingTransporter request, Channel channel) {
		
		registryChannels.add(channel);
		
		return null;
	}

}
