package org.laopopo.example.generic.test_3;

import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.rpc.MetricsReporter;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.monitor.DefaultMonitor;
import org.laopopo.monitor.MonitorConfig;
import org.laopopo.remoting.netty.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorTest {

	private static final Logger logger = LoggerFactory.getLogger(MonitorTest.class);

	private static DefaultMonitor defaultMonitor = null;

	public static void main(String[] args) {

		Thread t = new Thread(new MonitorTest1Scanner(), "timeout.scanner");
		t.setDaemon(true);
		t.start();

		NettyServerConfig config = new NettyServerConfig();
		MonitorConfig monitorConfig = new MonitorConfig();
		// 注册中心的端口号
		config.setListenPort(19010);

		defaultMonitor = new DefaultMonitor(config,monitorConfig);
		defaultMonitor.start();

	}

	private static class MonitorTest1Scanner implements Runnable {

		@Override
		public void run() {

			for (;;) {
				logger.info("统计中");
				try {
					Thread.sleep(10000);
					ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> maps = defaultMonitor.getGlobalMetricsReporter();
					if(null != maps){
						for(String serviceName : maps.keySet()){
							if(maps.get(serviceName) != null){
								for(Address address : maps.get(serviceName).keySet()){
									logger.info("serviceName [{}] address [{}] and metricsInfo [{}]",serviceName,address,maps.get(serviceName).get(address));
								}
							}
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

	}

}
