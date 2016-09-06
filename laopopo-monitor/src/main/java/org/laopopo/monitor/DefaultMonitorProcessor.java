package org.laopopo.monitor;

import static org.laopopo.common.protocal.LaopopoProtocol.MANAGER_SERVICE;
import static org.laopopo.common.protocal.LaopopoProtocol.MERTRICS_SERVICE;
import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.metrics.ServiceMetrics;
import org.laopopo.common.metrics.ServiceMetrics.ProviderInfo;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.rpc.ManagerServiceRequestType;
import org.laopopo.common.rpc.MetricsReporter;
import org.laopopo.common.rpc.RegisterMeta;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.common.transport.body.ManagerServiceCustomBody;
import org.laopopo.common.transport.body.MetricsCustomBody;
import org.laopopo.common.transport.body.ProviderMetricsCustomBody;
import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description monitor的处理器
 * @time 2016年8月17日
 * @modifytime
 */
public class DefaultMonitorProcessor implements NettyRequestProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultMonitorProcessor.class);
	
	
	private  DefaultMonitor defaultMonitor;

	public DefaultMonitorProcessor(DefaultMonitor defaultMonitor) {
		this.defaultMonitor = defaultMonitor;
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
		  case MERTRICS_SERVICE: //因为服务提供者端是定时发送统计信息的，如果没有收到或者消费失败了，则丢弃，不做强制消费的ack判断要求provider重发
			  return handlerMetricsService(request,ctx.channel());
		  case MANAGER_SERVICE: 
			  return handlerManagerService(request,ctx.channel());
			  
		
		}
		return null;
	}

	private RemotingTransporter handlerManagerService(RemotingTransporter request, Channel channel) {
		
		MetricsCustomBody metricsCustomBody = new MetricsCustomBody();
		RemotingTransporter remotingTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.MERTRICS_SERVICE, metricsCustomBody, request.getOpaque());
		
		ManagerServiceCustomBody body = serializerImpl().readObject(request.bytes(), ManagerServiceCustomBody.class);
		
		if(body.getManagerServiceRequestType() == ManagerServiceRequestType.METRICS){
			
			String serviceName = body.getSerivceName();
			ConcurrentMap<Address, MetricsReporter> maps = defaultMonitor.getGlobalMetricsReporter().get(serviceName);
			ConcurrentMap<Address, MetricsReporter> historyMaps = defaultMonitor.getHistoryGlobalMetricsReporter().get(serviceName);
			
			ServiceMetrics metrics = new ServiceMetrics();
			metrics.setServiceName(serviceName);
			buildMetrics(maps,metrics);
			buildMetrics(historyMaps,metrics);
			List<ServiceMetrics> serviceMetricses = new ArrayList<ServiceMetrics>();
			serviceMetricses.add(metrics);
			metricsCustomBody.setServiceMetricses(serviceMetricses);
		}
		return remotingTransporter;
	}

	private void buildMetrics(ConcurrentMap<Address, MetricsReporter> maps, ServiceMetrics metrics) {
		
		if(null != maps){
			
			for(Address address : maps.keySet()){
				
				MetricsReporter metricsReporter = maps.get(address);            
				Long callCount = metricsReporter.getCallCount();
				Long failCount = metricsReporter.getFailCount();
				Double handlerAvgTime = metricsReporter.getHandlerAvgTime();
				Double handlerDataAvgSize = metricsReporter.getHandlerAvgTime();
				
				ConcurrentMap<Address, ProviderInfo> providerConcurrentMap = metrics.getProviderMaps();
				
				ProviderInfo info = providerConcurrentMap.get(address);
				if(info == null){
					info = new ProviderInfo();
					info.setHost(address.getHost());
					info.setPort(address.getPort());
					providerConcurrentMap.put(address, info);
				}
				
				info.setHandlerAvgTime(info.getCallCount() + callCount == 0 ? 0d :new BigDecimal((metrics.getHandlerAvgTime() * metrics.getTotalCallCount() + handlerAvgTime * callCount) / (info.getCallCount() + callCount)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
				info.setCallCount(info.getCallCount() + callCount);
				info.setFailCount(info.getFailCount() + failCount);
				info.setHandlerDataAvgSize(info.getHandlerDataAvgSize() + handlerDataAvgSize);
				
				
				Double avgHandlerTime = metrics.getTotalCallCount() + info.getCallCount() == 0 ? 0 :new BigDecimal((metrics.getHandlerAvgTime() * metrics.getTotalCallCount() + handlerAvgTime * info.getCallCount()) / (metrics.getTotalCallCount() + info.getCallCount())).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
				
				metrics.setHandlerAvgTime(avgHandlerTime);
				metrics.setTotalCallCount(metrics.getTotalCallCount() + info.getCallCount());
				metrics.setTotalFailCount(metrics.getTotalFailCount() + info.getFailCount());
				metrics.setTotalHandlerRequestBodySize(metrics.getTotalHandlerRequestBodySize() + info.getHandlerDataAvgSize());
			}
		}
		
	}

	private RemotingTransporter handlerMetricsService(RemotingTransporter request, Channel channel) {
		
		ProviderMetricsCustomBody body = serializerImpl().readObject(request.bytes(),ProviderMetricsCustomBody.class);
		
		if(body.getMetricsReporter() != null && !body.getMetricsReporter().isEmpty()){
			
			
			for(MetricsReporter metricsReporter : body.getMetricsReporter()){
				
				Address address = new Address(metricsReporter.getHost(), metricsReporter.getPort());
				
				String serviceName = metricsReporter.getServiceName();
				ConcurrentMap<Address, MetricsReporter> maps = defaultMonitor.getGlobalMetricsReporter().get(serviceName);
				if(maps == null){
					maps = new ConcurrentHashMap<RegisterMeta.Address, MetricsReporter>();
					defaultMonitor.getGlobalMetricsReporter().put(serviceName, maps);
				}
				maps.put(address, metricsReporter);
			}
		}
		
		return null;
	}
}
