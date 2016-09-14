package org.laopopo.common.utils;

import io.netty.channel.Channel;

/**
 * 
 * @author BazingaLyn
 * @description 服务消费者实例和服务提供者实例之间的Netty channel组
 * 因为一个服务消费者的实例和一个服务提供者之间的连接可以有多个
 * @time 2016年8月14日
 * @modifytime
 */
public interface ChannelGroup {

	/**
	 * 轮询获取某个服务消费者和服务提供者之间的某个channel
	 * @return
	 */
	Channel next();

	/**
	 * 向group组中增加一个active的channel
	 * @param channel
	 * @return
	 */
	boolean add(Channel channel);

	/**
	 * 将group组中的某个channel移除掉
	 * @param channel
	 * @return
	 */
	boolean remove(Channel channel);

	/**
	 * 设置整个group的权重
	 * @param weight
	 */
	void setWeight(int weight);

	/**
	 * 获取到整个channelGroup的权重
	 * @return
	 */
	int getWeight();

	/**
	 * 
	 * @return
	 */
	int size();

	/**
	 * 
	 * @return
	 */
	boolean isAvailable();
	
	/**
	 * 
	 * @return
	 */
	UnresolvedAddress getAddress();

}
