package org.laopopo.monitor;

import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.laopopo.common.rpc.MetricsReporter;
import org.laopopo.common.rpc.RegisterMeta;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.common.utils.PersistUtils;
import org.laopopo.remoting.netty.NettyRemotingServer;
import org.laopopo.remoting.netty.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author BazingaLyn
 * @description 监控中心节点监控中心有以下几个操作 
 * 1)监控中心在启动服务的时候，需要将历史的统计记录恢复一下，这样防止历史统计记录丢失
 * 2)监控中心需要接受各个服务提供者发送过来的最新的统计记录，替换旧版的统计记录，这边需要注意的是：
 * 		每个provider实例，只要不宕机重启的情况下，他发送给监控中心的数据都是最新的统计数据
 *      举例来说，加入某个服务提供者从2016年9月7日14:55:46这个事件开始计数，到2016年9月7日14:56:01某个服务被调用了14次，此时它会发送14次给monitor端
 *      加入接下来的10s内，它有被调用了40次，那么他会发送调用了54次的信息给monitor，也就说不是增量统计，所以这边我们需要注意
 * 3)当管理着发送统计的命令给monitor端的时候，需要做统计的操作给管理端
 * 4)防止monitor实例宕机，需要每隔一段时间
 * @time 2016年8月15日
 * @modifytime
 */
public class DefaultMonitor implements MonitorNode {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMonitor.class);

	private MonitorConfig monitorConfig;
	private NettyServerConfig nettyServerConfig;
	private NettyRemotingServer nettyRemotingServer;
	private ExecutorService remotingExecutor;
	private ExecutorService remotingChannelInactiveExecutor;

	// 当前实例统计的上下文信息 key是服务名，value 是每个服务提供者在其端口提供的信息
	private ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> globalMetricsReporter = new ConcurrentHashMap<String, ConcurrentMap<Address, MetricsReporter>>();
	// 历史统计的信息
	private ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> historyGlobalMetricsReporter = new ConcurrentHashMap<String, ConcurrentMap<Address, MetricsReporter>>();
	// key为服务提供者实例与监控中心之间的channel，val是一个集合，因为有VIP的服务的原因，可能一个实例有2个端口，服务提供者的实例 它提供服务的监听端口
	private ConcurrentMap<Channel, ConcurrentSet<Address>> globalProviderReporter = new ConcurrentHashMap<Channel, ConcurrentSet<Address>>();
	// //定时任务
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("monitor-timer"));

	public DefaultMonitor(NettyServerConfig nettyServerConfig, MonitorConfig monitorConfig) {
		this.nettyServerConfig = nettyServerConfig;
		this.monitorConfig = monitorConfig;
		initialize();
	}

	private void initialize() {

		this.nettyRemotingServer = new NettyRemotingServer(nettyServerConfig);

		this.remotingExecutor = Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new NamedThreadFactory("MonitorExecutorThread_"));

		this.remotingChannelInactiveExecutor = Executors.newFixedThreadPool(nettyServerConfig.getChannelInactiveHandlerThreads(), new NamedThreadFactory(
				"MonitorChannelInActiveExecutorThread_"));

		// 从硬盘上恢复历史统计数据
		this.recoverFromDisk();

		// 注册处理器
		this.registerProcessor();

		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					DefaultMonitor.this.persistMetricsToDisk();
				} catch (IOException e) {
					logger.warn("persistMetricsToDisk failed [{}]", e.getMessage());
				}
			}
		}, 30, monitorConfig.getPersistTime(), TimeUnit.SECONDS);

	}

	/**
	 * 从硬盘上恢复以前的统计数据
	 */
	private void recoverFromDisk() {
		
		String persistString = PersistUtils.file2String(monitorConfig.getStorePathRootDir());

		if (null != persistString) {
			List<MetricsReporter> metricsReporters = JSON.parseArray(persistString.trim(), MetricsReporter.class);
			if (null != metricsReporters) {
				
				for (MetricsReporter metricsReporter : metricsReporters) {
					
					Address address = new Address(metricsReporter.getHost(), metricsReporter.getPort());
					String serviceName = metricsReporter.getServiceName();
					ConcurrentMap<Address, MetricsReporter> concurrentMap = new ConcurrentHashMap<RegisterMeta.Address, MetricsReporter>();
					concurrentMap.put(address, metricsReporter);
					historyGlobalMetricsReporter.put(serviceName, concurrentMap);
				}
			}
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	protected void persistMetricsToDisk() throws IOException {

		ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> historyGlobalMetricsReporter = this.historyGlobalMetricsReporter;

		if (historyGlobalMetricsReporter.keySet().size() > 0) {

			List<MetricsReporter> metricsReporters = new ArrayList<MetricsReporter>();
			if (historyGlobalMetricsReporter.values() != null) {

				for (ConcurrentMap<Address, MetricsReporter> concurrentMap : historyGlobalMetricsReporter.values()) {
					metricsReporters.addAll(concurrentMap.values());
				}
			}

			if (!metricsReporters.isEmpty()) {

				String persistStr = JSON.toJSONString(metricsReporters, false);
				PersistUtils.string2File(persistStr, monitorConfig.getStorePathRootDir());
			}

		}

	}

	private void registerProcessor() {
		this.nettyRemotingServer.registerDefaultProcessor(new DefaultMonitorProcessor(this), this.remotingExecutor);
		this.nettyRemotingServer.registerChannelInactiveProcessor(new DefaultMonitorChannelInactiveProcessor(this), remotingChannelInactiveExecutor);
	}

	@Override
	public void start() {
		this.nettyRemotingServer.start();
		logger.info("monitor start...");
	}

	public ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> getGlobalMetricsReporter() {
		return globalMetricsReporter;
	}

	public void setGlobalMetricsReporter(ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> globalMetricsReporter) {
		this.globalMetricsReporter = globalMetricsReporter;
	}

	public ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> getHistoryGlobalMetricsReporter() {
		return historyGlobalMetricsReporter;
	}

	public void setHistoryGlobalMetricsReporter(ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> historyGlobalMetricsReporter) {
		this.historyGlobalMetricsReporter = historyGlobalMetricsReporter;
	}

	public ConcurrentMap<Channel, ConcurrentSet<Address>> getGlobalProviderReporter() {
		return globalProviderReporter;
	}

	public void setGlobalProviderReporter(ConcurrentMap<Channel, ConcurrentSet<Address>> globalProviderReporter) {
		this.globalProviderReporter = globalProviderReporter;
	}

	public MonitorConfig getMonitorConfig() {
		return monitorConfig;
	}

	public void setMonitorConfig(MonitorConfig monitorConfig) {
		this.monitorConfig = monitorConfig;
	}

}
