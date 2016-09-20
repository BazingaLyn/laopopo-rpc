package org.laopopo.monitor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ConcurrentSet;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.rpc.MetricsReporter;
import org.laopopo.common.rpc.RegisterMeta;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.remoting.model.NettyChannelInactiveProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 
 *              当某个服务提供者宕机的时候，会与monitor端之间的链接inactive，此时需要将这个服务器提供的所有的服务调用的次数的信息全部持久化到硬盘
 *              中，防止数据丢失
 * @time 2016年9月7日
 * @modifytime
 */
public class DefaultMonitorChannelInactiveProcessor implements NettyChannelInactiveProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMonitorChannelInactiveProcessor.class);

	private DefaultMonitor defaultMonitor;

	public DefaultMonitorChannelInactiveProcessor(DefaultMonitor defaultMonitor) {
		this.defaultMonitor = defaultMonitor;
	}

	@Override
	public void processChannelInactive(ChannelHandlerContext ctx) {

		ConcurrentSet<Address> addresses = defaultMonitor.getGlobalProviderReporter().get(ctx.channel());
		if (null == addresses || addresses.isEmpty()) {
			logger.warn("channel [{}] provider no service", ctx.channel());
			return;
		}
		Collection<ConcurrentMap<Address, MetricsReporter>> value = defaultMonitor.getGlobalMetricsReporter().values();

		if (value != null && !value.isEmpty()) {

			for (ConcurrentMap<Address, MetricsReporter> eachMap : value) {
				
				for(Address address : addresses){
					
					MetricsReporter metricsReporter = eachMap.get(address);
					
					if(null != metricsReporter){ //将其更新到history map中去
						
						ConcurrentMap<Address, MetricsReporter>  historyMetrics = defaultMonitor.getHistoryGlobalMetricsReporter().get(metricsReporter.getServiceName());
						
						if(null == historyMetrics){
							
							historyMetrics = new ConcurrentHashMap<RegisterMeta.Address, MetricsReporter>();
							historyMetrics.put(address, metricsReporter);
							defaultMonitor.getHistoryGlobalMetricsReporter().put(metricsReporter.getServiceName(), historyMetrics);
						}else{
							MetricsReporter historyMetricsReporter= historyMetrics.get(address);
							
							if(null == historyMetricsReporter){
								
								historyMetricsReporter = metricsReporter;
								
							}else{
								historyMetricsReporter.setCallCount(historyMetricsReporter.getCallCount() + metricsReporter.getCallCount());
								historyMetricsReporter.setFailCount(historyMetricsReporter.getFailCount() + metricsReporter.getFailCount());
								historyMetricsReporter.setTotalReuqestTime(historyMetricsReporter.getTotalReuqestTime() + metricsReporter.getTotalReuqestTime());
							}
						}
						
						
						if(this.defaultMonitor.getMonitorConfig().isChangedPersistRightnow()){
							try {
								this.defaultMonitor.persistMetricsToDisk();
							} catch (IOException e) {
								logger.error("persist disk error exception [{}]",e.getMessage());
							}
						}
						metricsReporter.setCallCount(0l);
						metricsReporter.setFailCount(0l);
						metricsReporter.setTotalReuqestTime(0l);
					}
				}
			}
		}
	}

}
