package org.laopopo.monitor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.laopopo.common.rpc.MetricsReporter;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.remoting.netty.NettyRemotingServer;
import org.laopopo.remoting.netty.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 监控中心节点
 * @time 2016年8月15日
 * @modifytime
 */
public class DefaultMonitor implements MonitorNode {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultMonitor.class);
	
	private NettyServerConfig nettyServerConfig;
	
	private NettyRemotingServer nettyRemotingServer;
	
	private ExecutorService remotingExecutor;
	
	private ExecutorService remotingChannelInactiveExecutor;
	
	private ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> globalMetricsReporter = new ConcurrentHashMap<String, ConcurrentMap<Address,MetricsReporter>>();
	
	private ConcurrentMap<String, ConcurrentMap<Address, MetricsReporter>> historyGlobalMetricsReporter = new ConcurrentHashMap<String, ConcurrentMap<Address,MetricsReporter>>();
//	//定时任务
//    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("monitor-timer"));
	
	public DefaultMonitor(NettyServerConfig nettyServerConfig) {
		this.nettyServerConfig = nettyServerConfig;
		initialize();
	}

	private void initialize() {
		
		this.nettyRemotingServer = new NettyRemotingServer(nettyServerConfig);
		
		this.remotingExecutor =
                Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new NamedThreadFactory("MonitorExecutorThread_"));
	 
	 this.remotingChannelInactiveExecutor =
                Executors.newFixedThreadPool(nettyServerConfig.getChannelInactiveHandlerThreads(), new NamedThreadFactory("MonitorChannelInActiveExecutorThread_"));
	 
	 //注册处理器
	 this.registerProcessor();
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

}
