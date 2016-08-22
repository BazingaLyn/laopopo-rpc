package org.laopopo.client.consumer;

import io.netty.channel.Channel;

import org.laopopo.common.exception.remoting.RemotingSendRequestException;
import org.laopopo.common.exception.remoting.RemotingTimeoutException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.SubcribeRequestCustomBody;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 消费者的注册处理功能
 * @time 2016年8月18日
 * @modifytime
 */
public class DefaultConsumerRegistry {

	private static final Logger logger = LoggerFactory.getLogger(DefaultConsumerRegistry.class);

	private DefaultConsumer defaultConsumer;

	private long timeout;

	public DefaultConsumerRegistry(DefaultConsumer defaultConsumer) {
		this.defaultConsumer = defaultConsumer;
		this.timeout = this.defaultConsumer.getConsumerConfig().getRegistryTimeout();
	}

	public void subcribeService(String... serviceName) {
		
		if(this.defaultConsumer.getRegistyChannel() == null){
			this.defaultConsumer.getOrUpdateHealthyChannel();
		}
		
		if(this.defaultConsumer.getRegistyChannel() != null){
			
			logger.info("registry center channel is [{}]",this.defaultConsumer.getRegistyChannel());
			for (String service : serviceName) {

				SubcribeRequestCustomBody body = new SubcribeRequestCustomBody();
				body.setServiceName(service);

				RemotingTransporter remotingTransporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.SUBSCRIBE_SERVICE, body);
				try {
				
					RemotingTransporter request = sendKernelSubscribeInfo(this.defaultConsumer.getRegistyChannel(),remotingTransporter,timeout);
					RemotingTransporter ackTransporter = this.defaultConsumer.getConsumerManager().handlerSubcribeResult(request, this.defaultConsumer.getRegistyChannel());
					this.defaultConsumer.getRegistyChannel().writeAndFlush(ackTransporter);
				} catch (Exception e) {
					logger.warn("registry failed [{}]", e.getMessage());
				}

			}
		}else{
			logger.warn("sorry can not connection to registry address [{}],please check your registry address",this.defaultConsumer.getRegistryClientConfig().getDefaultAddress());
		}

	}

	private RemotingTransporter sendKernelSubscribeInfo(Channel registyChannel, RemotingTransporter remotingTransporter, long timeout) throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException {
		return this.defaultConsumer.getRegistryNettyRemotingClient().invokeSyncImpl(this.defaultConsumer.getRegistyChannel(), remotingTransporter,timeout);
	}

}
