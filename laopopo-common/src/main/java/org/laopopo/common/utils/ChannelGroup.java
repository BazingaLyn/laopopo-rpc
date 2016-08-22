package org.laopopo.common.utils;

import io.netty.channel.Channel;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public interface ChannelGroup {

	Channel next();

	boolean add(Channel channel);

	boolean remove(Channel channel);

	void setWeight(int weight);

	int getWeight();

	int size();

	boolean isAvailable();

}
