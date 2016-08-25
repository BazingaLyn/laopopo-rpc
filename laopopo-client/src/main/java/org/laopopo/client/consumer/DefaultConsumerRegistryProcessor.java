package org.laopopo.client.consumer;

import static org.laopopo.common.protocal.LaopopoProtocol.SUBCRIBE_RESULT;
import static org.laopopo.common.protocal.LaopopoProtocol.SUBCRIBE_SERVICE_CANCEL;
import static org.laopopo.common.protocal.LaopopoProtocol.OFFLINE_ADDRESS;
import io.netty.channel.ChannelHandlerContext;

import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 消费者端注册功能的主要处理逻辑
 * @time 2016年8月22日
 * @modifytime
 */
public class DefaultConsumerRegistryProcessor implements NettyRequestProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DefaultConsumerRegistryProcessor.class);

	private DefaultConsumer defaultConsumer;

	public DefaultConsumerRegistryProcessor(DefaultConsumer defaultConsumer) {
		this.defaultConsumer = defaultConsumer;
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
		case SUBCRIBE_RESULT:
			// 回复ack信息 这个也要保持幂等性，因为有可能在consumer消费成功之后发送ack信息到registry信息丢失，registry回重新发送订阅结果信息
			return this.defaultConsumer.getConsumerManager().handlerSubcribeResult(request, ctx.channel());
		case SUBCRIBE_SERVICE_CANCEL:
			// 回复ack信息
			return this.defaultConsumer.getConsumerManager().handlerSubscribeResultCancel(request, ctx.channel());
		case OFFLINE_ADDRESS:
			// 回复ack信息
			return this.defaultConsumer.getConsumerManager().handlerOffline(request, ctx.channel());
		}

		return null;
	}

}
