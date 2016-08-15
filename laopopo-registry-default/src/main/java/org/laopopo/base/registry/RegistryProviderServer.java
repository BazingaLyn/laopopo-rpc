package org.laopopo.base.registry;

import io.netty.channel.Channel;

import org.laopopo.remoting.model.RemotingTransporter;

/**
 * 
 * @author BazingaLyn
 * @description 注册中心处理provider的服务接口
 * @time 2016年8月15日
 * @modifytime
 */
public interface RegistryProviderServer {
	
	
	/**
	 * 处理provider发送过来的注册信息
	 * @param remotingTransporter 里面的CommonCustomBody 是#PublishServiceCustomBody
	 * @param channel
	 * @return
	 */
	RemotingTransporter handlerRegister(RemotingTransporter remotingTransporter,Channel channel);
}
