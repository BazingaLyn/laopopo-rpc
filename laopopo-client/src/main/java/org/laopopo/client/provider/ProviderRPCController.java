package org.laopopo.client.provider;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import static org.laopopo.common.utils.Reflects.fastInvoke;
import static org.laopopo.common.utils.Reflects.findMatchingParameterTypes;
import static org.laopopo.common.utils.Status.APP_FLOW_CONTROL;
import static org.laopopo.common.utils.Status.BAD_REQUEST;
import static org.laopopo.common.utils.Status.SERVICE_NOT_FOUND;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.List;

import javax.management.ServiceNotFoundException;

import org.laopopo.client.metrics.Metrics;
import org.laopopo.client.provider.DefaultServiceProviderContainer.CurrentServiceState;
import org.laopopo.client.provider.flow.control.ServiceFlowControllerManager;
import org.laopopo.client.provider.model.ServiceWrapper;
import org.laopopo.common.exception.rpc.BadRequestException;
import org.laopopo.common.exception.rpc.FlowControlException;
import org.laopopo.common.exception.rpc.ServerBusyException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.RequestCustomBody;
import org.laopopo.common.transport.body.ResponseCustomBody;
import org.laopopo.common.transport.body.ResponseCustomBody.ResultWrapper;
import org.laopopo.common.utils.Pair;
import org.laopopo.common.utils.Status;
import org.laopopo.common.utils.SystemClock;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

/**
 * 
 * @author BazingaLyn
 * @description 处理consumer rpc请求的核心控制器，并统计处理的次数
 * @time 2016年8月30日
 * @modifytime
 */
public class ProviderRPCController {

	private static final Logger logger = LoggerFactory.getLogger(ProviderRPCController.class);

	private DefaultProvider defaultProvider;

	public ProviderRPCController(DefaultProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

	public void handlerRPCRequest(RemotingTransporter request, Channel channel) {
		
		Meter rejectionMeter = null;  			// 请求被拒绝次数统计
		Timer processingTimer = null;           // 统计请求的时间
		
		String serviceName = null;

		RequestCustomBody body = null;

		try {
			byte[] bytes = request.bytes();
			request.bytes(null);

			body = serializerImpl().readObject(bytes, RequestCustomBody.class);
			request.setCustomHeader(body);
			serviceName = body.getServiceName();
			
			rejectionMeter = Metrics.meter(serviceName+"::rejection");
			processingTimer = Metrics.timer(serviceName+"::processing");
			
		} catch (Exception e) {
			rejected(BAD_REQUEST, channel, request,rejectionMeter);
			return;
		}
		
		final Pair<CurrentServiceState, ServiceWrapper> pair = defaultProvider.getProviderController().getProviderContainer().lookupService(serviceName);
		if (pair == null || pair.getValue() == null) {
            rejected(SERVICE_NOT_FOUND, channel, request,rejectionMeter);
            return;
        }
		
		// app flow control
        ServiceFlowControllerManager serviceFlowControllerManager = defaultProvider.getServiceFlowControllerManager();
        if (!serviceFlowControllerManager.isAllow(serviceName)) {
            rejected(APP_FLOW_CONTROL,channel, request,rejectionMeter);
            return;
        }
        
        process(pair,request,channel,processingTimer);
	}



	private void process(Pair<CurrentServiceState, ServiceWrapper> pair, final RemotingTransporter request, Channel channel,final Timer processingTimer) {
		
		Object invokeResult = null;
		
		CurrentServiceState currentServiceState = pair.getKey();
		ServiceWrapper serviceWrapper = pair.getValue();
		
		Object targetCallObj = serviceWrapper.getServiceProvider();
		
		Object[] args = ((RequestCustomBody)request.getCustomHeader()).getArgs();
		
		if(currentServiceState.getHasDegrade().get() && serviceWrapper.getMockDegradeServiceProvider() != null){
			targetCallObj = serviceWrapper.getMockDegradeServiceProvider();
		}
		
		String methodName = serviceWrapper.getMethodName();
		List<Class<?>[]> parameterTypesList = serviceWrapper.getParamters();
		
		
		Class<?>[] parameterTypes = findMatchingParameterTypes(parameterTypesList, args);
		invokeResult = fastInvoke(targetCallObj, methodName, parameterTypes, args);
		
		ResultWrapper result = new ResultWrapper();
		result.setResult(invokeResult);
		ResponseCustomBody body = new ResponseCustomBody(Status.OK.value(), result);
		
		final RemotingTransporter response = RemotingTransporter.createResponseTransporter(LaopopoProtocol.RPC_RESPONSE, body, request.getOpaque());
		
		channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future) throws Exception {
				
				long elapsed = SystemClock.millisClock().now() - request.timestamp();
				if (future.isSuccess()) {
					
					processingTimer.update(elapsed, MILLISECONDS);
				} else {
					logger.info("request {} get failed response {}", request, response);
				}
			}
		});
		
	}

	private void rejected(Status status, Channel channel, final RemotingTransporter request,Meter rejectionMeter) {

		rejectionMeter.mark();
		ResultWrapper result = new ResultWrapper();
		switch (status) {
		case SERVER_BUSY:
			result.setError(new ServerBusyException());
			break;
		case BAD_REQUEST:
			result.setError(new BadRequestException());
		case SERVICE_NOT_FOUND:
			result.setError(new ServiceNotFoundException(((RequestCustomBody) request.getCustomHeader()).getServiceName()));
			break;
		case APP_FLOW_CONTROL:
		case PROVIDER_FLOW_CONTROL:
			result.setError(new FlowControlException());
			break;
		default:
			logger.warn("Unexpected status.", status.description());
			return;
		}
		logger.warn("Service rejected: {}.", result.getError());

		ResponseCustomBody responseCustomBody = new ResponseCustomBody(status.value(), result);
		final RemotingTransporter response = RemotingTransporter.createResponseTransporter(LaopopoProtocol.RPC_RESPONSE, responseCustomBody,
				request.getOpaque());

		channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					logger.info("request error {} get success response {}", request, response);
				} else {
					logger.info("request error {} get failed response {}", request, response);
				}
			}
		});
	}

}
