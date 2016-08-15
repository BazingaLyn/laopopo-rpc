package org.laopopo.base.registry;

import io.netty.channel.ChannelHandlerContext;
import static org.laopopo.common.protocal.LaopopoProtocol.*;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRegistryProcessor implements NettyRequestProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultRegistryProcessor.class);
	
	private DefaultRegistryServer defaultRegistryServer;

	public DefaultRegistryProcessor(DefaultRegistryServer defaultRegistryServer) {
		this.defaultRegistryServer = defaultRegistryServer;
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
		   case PUBLISH_SERVICE:
			   this.defaultRegistryServer.getProviderManager().handlerRegister(request,ctx.channel());
		}
		
		
		return null;
	}

}
