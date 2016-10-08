package org.laopopo.client.consumer.proxy;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;

import java.lang.reflect.Method;
import java.util.Map;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import org.laopopo.client.annotation.RPConsumer;
import org.laopopo.client.consumer.Consumer;
import org.laopopo.common.exception.remoting.RemotingSendRequestException;
import org.laopopo.common.exception.remoting.RemotingTimeoutException;
import org.laopopo.common.exception.rpc.NoServiceException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.RequestCustomBody;
import org.laopopo.common.transport.body.ResponseCustomBody;
import org.laopopo.common.utils.ChannelGroup;
import org.laopopo.common.utils.SystemClock;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 同步调用的类
 * @time 2016年8月27日
 * @modifytime
 */
public class SynInvoker {
	
	private static final Logger logger = LoggerFactory.getLogger(SynInvoker.class);

	private Consumer consumer;
	
	private long timeoutMillis;
	
	private Map<String, Long> methodsSpecialTimeoutMillis;
	

	public SynInvoker(Consumer consumer, long timeoutMillis, Map<String, Long> methodsSpecialTimeoutMillis) {
		this.consumer = consumer;
		this.timeoutMillis = timeoutMillis;
		this.methodsSpecialTimeoutMillis = methodsSpecialTimeoutMillis;
	}

	@RuntimeType
	public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) {
		
		RPConsumer rpcConsumer = method.getAnnotation(RPConsumer.class);
		
		String serviceName = rpcConsumer.serviceName();
		
		ChannelGroup channelGroup = consumer.loadBalance(serviceName);
		
		if (channelGroup == null || channelGroup.size() == 0) {
			//如果有channelGroup但是channel中却没有active的Channel的有可能是用户通过直连的方式去调用，我们需要去根据远程的地址去初始化channel
			if(channelGroup != null && channelGroup.getAddress() != null){
				
				logger.warn("direct connect provider");
				Channel channel = null;
				try {
					channel = consumer.directGetProviderByChannel(channelGroup.getAddress());
					channelGroup.add(channel);
					
				} catch (InterruptedException e) {
					logger.warn("direction get channel occor exception [{}]",e.getMessage());
				}
			}else{
				throw new NoServiceException("没有第三方提供该服务，请检查服务名");
			}
		}

		RequestCustomBody body = new RequestCustomBody();
		body.setArgs(args);                                   //调用参数
		body.setServiceName(serviceName);                     //调用的服务名
		body.setTimestamp(SystemClock.millisClock().now());   //调用的时间
		
		Long time = null;
		if(methodsSpecialTimeoutMillis != null){
			
			Long methodTime = methodsSpecialTimeoutMillis.get(serviceName);
			if(null != methodTime){
				time = methodTime;
			}
		}else{
			time = timeoutMillis == 0l ? 3000l :timeoutMillis;
		}
		
		RemotingTransporter request = RemotingTransporter.createRequestTransporter(LaopopoProtocol.RPC_REQUEST, body);
		RemotingTransporter response;
		try {
			
			response = consumer.sendRpcRequestToProvider(channelGroup.next(),request,time);
			ResponseCustomBody customBody = serializerImpl().readObject(response.bytes(), ResponseCustomBody.class);
			return customBody.getResult();
			
		} catch (RemotingTimeoutException e) {
			logger.warn("call remoting timeout [{}]",e.getMessage());
			return null;
		} catch (RemotingSendRequestException e) {
			logger.warn("send request orror exception [{}]",e.getMessage());
			return null;
		} catch (InterruptedException e) {
			logger.error("interrupted exception [{}]",e.getMessage());
			return null;
		}
	}

}
