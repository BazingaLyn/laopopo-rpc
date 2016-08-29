package org.laopopo.base.registry;

import static org.laopopo.common.protocal.LaopopoProtocol.DEGRADE_SERVICE;
import static org.laopopo.common.protocal.LaopopoProtocol.PUBLISH_CANCEL_SERVICE;
import static org.laopopo.common.protocal.LaopopoProtocol.PUBLISH_SERVICE;
import static org.laopopo.common.protocal.LaopopoProtocol.REVIEW_SERVICE;
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
		
		   case PUBLISH_SERVICE:         //处理服务提供者provider推送的服务信息
			   return this.defaultRegistryServer.getProviderManager().handlerRegister(request,ctx.channel());      //要保持幂等性，同一个实例重复发布同一个服务的时候对于注册中心来说是无影响的
		   case PUBLISH_CANCEL_SERVICE:  //处理服务提供者provider推送的服务取消的信息
			   return this.defaultRegistryServer.getProviderManager().handlerRegisterCancel(request,ctx.channel());
		   case SUBSCRIBE_SERVICE:       //处理服务消费者consumer订阅服务的请求
			   return this.defaultRegistryServer.getProviderManager().handleSubscribe(request, ctx.channel());
		   case REVIEW_SERVICE:          //处理管理者发送过来的服务审核服务
			   return this.defaultRegistryServer.getProviderManager().handleReview(request, ctx.channel());
		   case DEGRADE_SERVICE:          //处理管理员发送过来的手动降级请求
			   return this.defaultRegistryServer.getProviderManager().handleDegradeService(request, ctx.channel());
		}
		
		
		return null;
	}

}
