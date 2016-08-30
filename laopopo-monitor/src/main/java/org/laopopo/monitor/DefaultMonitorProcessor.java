package org.laopopo.monitor;

import io.netty.channel.ChannelHandlerContext;

import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description monitor的处理器
 * @time 2016年8月17日
 * @modifytime
 */
public class DefaultMonitorProcessor implements NettyRequestProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultMonitorProcessor.class);
	
	private DefaultMonitor defaultMonitor;

	public DefaultMonitorProcessor(DefaultMonitor defaultMonitor) {
		this.defaultMonitor = defaultMonitor;
	}

	@Override
	public RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug("receive request, {} {} {}",//
                request.getCode(), //
                ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), //
                request);
        }
		
		switch (request.getCode()) {
		  
		
		}
		return null;
	}

}
