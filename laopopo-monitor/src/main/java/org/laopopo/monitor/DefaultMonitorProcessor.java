package org.laopopo.monitor;

import static org.laopopo.common.protocal.LaopopoProtocol.MANAGER_SERVICE;
import static org.laopopo.common.protocal.LaopopoProtocol.MERTRICS_SERVICE;
import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ConcurrentSet;

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
			//返回给管理页面的对象
			ServiceMetrics metrics = new ServiceMetrics();
			metrics.setServiceName(serviceName);
			//从当前的统计信息中，构建返回体
			buildMetrics(maps,metrics);
			//从持久化信息中返回
			buildMetrics(historyMaps,metrics);
			List<ServiceMetrics> serviceMetricses = new ArrayList<ServiceMetrics>();
			serviceMetricses.add(metrics);
			metricsCustomBody.setServiceMetricses(serviceMetricses);
		}
		return remotingTransporter;
	}

	private void buildMetrics(ConcurrentMap<Address, MetricsReporter> maps, ServiceMetrics metrics) {
		
		if(null != maps){
			
			Long totalCallCount = 0l;
			Long totalFailCount = 0l;
			Long totalTime = 0l;
			Long totalRequestSize = 0l;
			
			for(Address address : maps.keySet()){
				
				MetricsReporter metricsReporter = maps.get(address);            
				Long callCount = metricsReporter.getCallCount();
				Long failCount = metricsReporter.getFailCount();
				Long requestSize = metricsReporter.getRequestSize();
				Long handlerTime = metricsReporter.getTotalReuqestTime();
				
				totalCallCount += callCount;
				totalFailCount += failCount;
				totalRequestSize += requestSize;
				totalTime += handlerTime;
				
				ConcurrentMap<Address, ProviderInfo> providerConcurrentMap = metrics.getProviderMaps();
				
				ProviderInfo info = providerConcurrentMap.get(address);
				if(info == null){
					info = new ProviderInfo();
					info.setHost(address.getHost());
					info.setPort(address.getPort());
					providerConcurrentMap.put(address, info);
				}
				
				Long eachHandlerAvgTime = 0l;

				if((info.getCallCount() + metricsReporter.getCallCount())!=0){
					
					eachHandlerAvgTime = ((info.getCallCount() - info.getFailCount()) * info.getHandlerAvgTime() + metricsReporter.getTotalReuqestTime()) / (info.getCallCount() - info.getFailCount() + metricsReporter.getCallCount() - metricsReporter.getFailCount());
					
				}
				
				info.setHandlerAvgTime(eachHandlerAvgTime);
				info.setCallCount(info.getCallCount() + callCount);
				info.setFailCount(info.getFailCount() + failCount);
				info.setRequestSize(info.getRequestSize() + requestSize);
				
				
			}
			metrics.setTotalCallCount(metrics.getTotalCallCount() + totalCallCount);
			metrics.setTotalFailCount(metrics.getTotalFailCount() + totalFailCount);
			Long existTotalTime = (metrics.getTotalCallCount() - metrics.getTotalFailCount()) * metrics.getHandlerAvgTime();
			
			metrics.setHandlerAvgTime((existTotalTime+totalTime) / (metrics.getTotalCallCount() - metrics.getTotalFailCount()));
			metrics.setRequestSize(metrics.getRequestSize() + totalRequestSize);
		}
		
	}

	/**
	 * 处理服务提供者发送过来的统计信息
	 * 这边需要注意的是，因为服务提供端不是按照时间段去统计的，而是从服务启动的时候就开始统计，也就说服务提供端发送过来的信息是全量信息，不是增量信息
	 * 所以我们这边要做的是将provider发送过来最新的信息替换monitor本地的信息，而不是将本地的信息与provider发送的信息做累加操作
	 * 
	 * @param request
	 * @param channel
	 * @return
	 */
	private RemotingTransporter handlerMetricsService(RemotingTransporter request, Channel channel) {
		
		//反序列化内容
		ProviderMetricsCustomBody body = serializerImpl().readObject(request.bytes(),ProviderMetricsCustomBody.class);
		
		if(body.getMetricsReporter() != null && !body.getMetricsReporter().isEmpty()){
			
			for(MetricsReporter metricsReporter : body.getMetricsReporter()){
				
				
				String host = ConnectionUtils.parseChannelRemoteAddress(channel).getHost();
				
				Address address = new Address(host, metricsReporter.getPort());
				
				metricsReporter.setHost(host);
				
				ConcurrentSet<Address> addresses = defaultMonitor.getGlobalProviderReporter().get(channel);
				if(addresses == null){
					addresses = new ConcurrentSet<Address>();
					defaultMonitor.getGlobalProviderReporter().put(channel, addresses);
				}
				addresses.add(address);
				
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
