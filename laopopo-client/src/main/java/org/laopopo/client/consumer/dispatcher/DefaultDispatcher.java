package org.laopopo.client.consumer.dispatcher;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.laopopo.client.consumer.promise.DefaultResultPromise;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDispatcher {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultDispatcher.class);
	
	public DefaultResultPromise dispatcher(Channel channel,RemotingTransporter request,long timeout){
		
		final Channel _channel = channel;
		DefaultResultPromise defaultResultGather = new DefaultResultPromise(channel, request,timeout);
		
		_channel.writeAndFlush(request).addListener(new ChannelFutureListener() {  
            public void operationComplete(ChannelFuture future) throws Exception {  
                if(!future.isSuccess()) {  
                    logger.info("send fail,reason is {}",future.cause().getMessage());  
                }  
                  
            }  
        });
		return defaultResultGather;
		
	}

}
