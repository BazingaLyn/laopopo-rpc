package org.laopopo.client.provider;

import static org.laopopo.common.protocal.LaopopoProtocol.RPC_REQUEST;
import io.netty.channel.ChannelHandlerContext;

import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description provider端
 * @time
 * @modifytime
 */
public class DefaultProviderRPCProcessor implements NettyRequestProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultProviderRPCProcessor.class);
	
	private DefaultProvider defaultProvider;

	public DefaultProviderRPCProcessor(DefaultProvider defaultProvider) {
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
		   case RPC_REQUEST:
			   //这边稍微特殊处理一下，可以返回null,我们不需要叫外层代码帮我们writeAndFlush 发出请求，因为我们持有channel，这样做rpc可以更加灵活一点
			    this.defaultProvider.handlerRPCRequest(request,ctx.channel());
			    break;
		}
		return null;
	}

}
