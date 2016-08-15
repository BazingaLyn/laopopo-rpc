package org.laopopo.base.registry;

import org.laopopo.registry.model.RegisterMeta.Address;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 
 * @author BazingaLyn
 * @description 注册中心模块消费端的管理
 * @time 2016年8月15日
 * @modifytime
 */
public class RegistryConsumerManager {
	
	private DefaultRegistryServer defaultRegistryServer;
	
	private final ChannelGroup subscriberChannels = new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);

	public RegistryConsumerManager(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
	}

	public ChannelGroup getSubscriberChannels() {
		return subscriberChannels;
	}

	public void handleOfflineNotice(Address address) {
		
	}
	

}
