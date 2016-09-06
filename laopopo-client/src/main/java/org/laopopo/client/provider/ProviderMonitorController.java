package org.laopopo.client.provider;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.client.metrics.Metrics;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.rpc.MetricsReporter;
import org.laopopo.common.transport.body.ProviderMetricsCustomBody;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description provider 端连接monitor端的控制端
 * @time 提供者连接监控中心的全局管控器
 * @modifytime
 */
public class ProviderMonitorController {

	private static final Logger logger = LoggerFactory.getLogger(ProviderMonitorController.class);

	private DefaultProvider defaultProvider;

	public ProviderMonitorController(DefaultProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

	/**
	 * 定时发送信息到
	 */
	public void sendMetricsInfo() {

		logger.info("scheduled sendMetricsInfos");

		if (defaultProvider.getMonitorAddress() == null) {
			logger.warn("monitor address is empty");
			return;
		}
		
		if(defaultProvider.getGlobalPublishService() == null){
			logger.warn("publish info is empty");
			return;
		}
		
		

		ConcurrentMap<String, MetricsReporter> metricsMap = Metrics.getGlobalMetricsReporter();
		if (metricsMap != null && metricsMap.keySet() != null && metricsMap.values() != null) {

			List<MetricsReporter> reporters = new ArrayList<MetricsReporter>();
			
			reporters.addAll(metricsMap.values());
			
			for(int i = 0;i<reporters.size();i++){
				String serviceName = reporters.get(i).getServiceName();
				PublishServiceCustomBody body = defaultProvider.getGlobalPublishService().get(serviceName);
				
				if(body == null){
					logger.warn("servicename [{}] has no publishInfo ",serviceName);
					continue;
				}
				reporters.get(i).setHost(body.getHost());
				reporters.get(i).setPort(body.isVIPService() ? (body.getPort() -2):body.getPort());
			}
			ProviderMetricsCustomBody body = new ProviderMetricsCustomBody();
			body.setMetricsReporter(reporters);
			RemotingTransporter remotingTransporter = RemotingTransporter.createRequestTransporter(LaopopoProtocol.MERTRICS_SERVICE, body);
			Channel channel = defaultProvider.getMonitorChannel();

			if (null != channel && channel.isActive() && channel.isWritable()) {
				channel.writeAndFlush(remotingTransporter);
			}
		}

	}

	public void checkMonitorChannel() throws InterruptedException {
		Channel channel = defaultProvider.getMonitorChannel();

		if ((null == channel || !channel.isActive()) && defaultProvider.getMonitorAddress() != null) {
			defaultProvider.initMonitorChannel();
		}
	}

}
