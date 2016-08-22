package org.laopopo.common.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class NettyChannelGroup implements ChannelGroup {
	
	
	private final CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<>();
	
	private AtomicInteger index = new AtomicInteger(0);
	
	private final UnresolvedAddress address;
	
	private volatile int weight = 50;
	
    public NettyChannelGroup(UnresolvedAddress address) {
        this.address = address;
    }

	@Override
	public Channel next() {
		for(;;){
			int length = channels.size();
			if(length == 0){
                throw new IllegalStateException("no channel");
			}
			if (length == 1) {
                return channels.get(0);
            }
			int offset = Math.abs(index.incrementAndGet() % length);

            return channels.get(offset);
		}
	}

	@Override
	public boolean add(Channel channel) {
		boolean added = channels.add(channel);
        if (added) {
        	channel.closeFuture().addListener(remover);
        }
		return false;
	}


	@Override
	public boolean remove(Channel channel) {
		return channels.remove(channel);
	}
	
	private final ChannelFutureListener remover = new ChannelFutureListener() {

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            remove(future.channel());
        }
    };

	@Override
	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public UnresolvedAddress getAddress() {
		return address;
	}

	@Override
	public int size() {
		return channels.size();
	}

	@Override
	public boolean isAvailable() {
		return channels.size() > 0 ? true :  false;
	}

}
