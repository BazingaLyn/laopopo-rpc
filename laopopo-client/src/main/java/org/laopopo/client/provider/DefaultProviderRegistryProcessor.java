package org.laopopo.client.provider;

import static org.laopopo.common.protocal.LaopopoProtocol.DEGRADE_SERVICE;
import static org.laopopo.common.protocal.LaopopoProtocol.METRICS_SERVICE;
import io.netty.channel.ChannelHandlerContext;

import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultProviderRegistryProcessor implements NettyRequestProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultProviderRegistryProcessor.class);
	
	private DefaultProvider defaultProvider;

	public DefaultProviderRegistryProcessor(DefaultProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
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
		   case DEGRADE_SERVICE:
			    return this.defaultProvider.handlerDegradeService(request,ctx.channel());
		   case METRICS_SERVICE:
			   return this.defaultProvider.handlerMetricsService(request,ctx.channel());
		}
		return null;
	}

}
