package org.laopopo.monitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.remoting.netty.NettyRemotingServer;
import org.laopopo.remoting.netty.NettyServerConfig;

/**
 * 
 * @author BazingaLyn
 * @description 监控中心节点
 * @time 2016年8月15日
 * @modifytime
 */
public class DefaultMonitor implements MonitorNode {
	
	private NettyServerConfig nettyServerConfig;
	
	private NettyRemotingServer nettyRemotingServer;
	
	private ExecutorService remotingExecutor;
	
	private ExecutorService remotingChannelInactiveExecutor;
	
	private MonitorController monitorController;
	
	//定时任务
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("monitor-timer"));
	
	public DefaultMonitor(NettyServerConfig nettyServerConfig) {
		this.nettyServerConfig = nettyServerConfig;
		monitorController = new MonitorController(this);
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
	}

	public MonitorController getMonitorController() {
		return monitorController;
	}

	public void setMonitorController(MonitorController monitorController) {
		this.monitorController = monitorController;
	}
	

}
