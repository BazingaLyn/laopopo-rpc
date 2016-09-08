package org.laopopo.client.consumer.proxy;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;

import java.lang.reflect.Method;
import java.util.Map;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import org.laopopo.client.annotation.RPConsumer;
import org.laopopo.client.consumer.Consumer;
import org.laopopo.common.exception.rpc.NoServiceException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.RequestCustomBody;
import org.laopopo.common.transport.body.ResponseCustomBody;
import org.laopopo.common.utils.ChannelGroup;
import org.laopopo.remoting.model.RemotingTransporter;

/**
 * 
 * @author BazingaLyn
 * @description 同步调用的类
 * @time 2016年8月27日
 * @modifytime
 */
public class SynInvoker {

	private Consumer consumer;
	
	private long timeoutMillis;
	
	private Map<String, Long> methodsSpecialTimeoutMillis;
	

	public SynInvoker(Consumer consumer, long timeoutMillis, Map<String, Long> methodsSpecialTimeoutMillis) {
		this.consumer = consumer;
		this.timeoutMillis = timeoutMillis;
		this.methodsSpecialTimeoutMillis = methodsSpecialTimeoutMillis;
	}

	@RuntimeType
	public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable {
		
		RPConsumer rpcConsumer = method.getAnnotation(RPConsumer.class);
		
		String serviceName = rpcConsumer.serviceName();
		
		ChannelGroup channelGroup = consumer.loadBalance(serviceName);
		if (channelGroup == null || channelGroup.size() == 0) {
			throw new NoServiceException("没有第三方提供该服务，请检查服务名");
		}

		RequestCustomBody body = new RequestCustomBody();
		body.setArgs(args);
		body.setServiceName(serviceName);
		
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
		RemotingTransporter response = consumer.sendRpcRequestToProvider(channelGroup.next(),request,time);
		ResponseCustomBody customBody = serializerImpl().readObject(response.bytes(), ResponseCustomBody.class);
		return customBody.getResultWrapper().getResult();
	}

}
