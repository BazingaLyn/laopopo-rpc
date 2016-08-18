package org.laopopo.base.registry;

import static org.laopopo.common.protocal.LaopopoProtocol.PUBLISH_CANCEL_SERVICE;
import static org.laopopo.common.protocal.LaopopoProtocol.PUBLISH_SERVICE;
import static org.laopopo.common.protocal.LaopopoProtocol.SUBSCRIBE_SERVICE;
import io.netty.channel.ChannelHandlerContext;

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
			   return this.defaultRegistryServer.getProviderManager().handlerRegister(request,ctx.channel());
		   case PUBLISH_CANCEL_SERVICE:
			   return this.defaultRegistryServer.getProviderManager().handlerRegisterCancel(request,ctx.channel());
		   case SUBSCRIBE_SERVICE:
			   return this.defaultRegistryServer.getProviderManager().handleSubscribe(request, ctx.channel());
		}
		
		
		return null;
	}

}
