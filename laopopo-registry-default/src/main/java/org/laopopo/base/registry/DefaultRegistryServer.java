package org.laopopo.base.registry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.registry.RegistryServer;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyRemotingServer;
import org.laopopo.remoting.netty.NettyServerConfig;

/**
 * 
 * 可以有多个注册中心，所有的注册中心之前不进行通讯，都是无状态的
 * 1)provider端与每一个注册中心之间保持长连接，保持重连
 * 2)consumer随机选择一个注册中心保持长连接，如果断了，不去主动重连，选择其他可用的注册中心
 * 
 * @author BazingaLyn
 * @description 默认的注册中心，处理注册端的所有事宜：
 * 1)处理consumer端发送过来的注册信息
 * 2)处理provider端发送过来的订阅信息
 * 3)当服务下线需要通知对应的consumer变更后的注册信息
 * 4)所有的注册订阅信息的储存和健康检查
 * @time 2016年8月15日
 * @modifytime
 */
public class DefaultRegistryServer implements RegistryServer {
	
	//netty Server的一些配置文件
    private final NettyServerConfig nettyServerConfig;
    
    //注册中心的netty server端
	private NettyRemotingServer remotingServer;
	
	private RegistryConsumerManager consumerManager;
	
	private RegistryProviderManager providerManager;
	
	private ExecutorService remotingExecutor;
	
	private ExecutorService remotingChannelInactiveExecutor;
	
	//定时任务
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("registry-timer"));
	
    /**
     * 
     * @param nettyServerConfig 注册中心的netty的配置文件 至少需要配置listenPort
     * @param nettyClientConfig 注册中心连接Monitor端的netty配置文件，至少需要配置defaultAddress值 这边monitor是单实例，所以address一个就好
     */
    public DefaultRegistryServer(NettyServerConfig nettyServerConfig) {
    	this.nettyServerConfig = nettyServerConfig;
    	consumerManager = new RegistryConsumerManager(this);
    	providerManager = new RegistryProviderManager(this);
    	initialize();
	}

	private void initialize() {
		
		 this.remotingServer = new NettyRemotingServer(this.nettyServerConfig);
		 
		 
		 this.remotingExecutor =
	                Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new NamedThreadFactory("RegistryCenterExecutorThread_"));
		 
		 this.remotingChannelInactiveExecutor =
	                Executors.newFixedThreadPool(nettyServerConfig.getChannelInactiveHandlerThreads(), new NamedThreadFactory("RegistryCenterChannelInActiveExecutorThread_"));
		 
		 //注册处理器
		 this.registerProcessor();
	}

	private void registerProcessor() {
		this.remotingServer.registerDefaultProcessor(new DefaultRegistryProcessor(this), this.remotingExecutor);
		this.remotingServer.registerChannelInactiveProcessor(new DefaultRegistryChannelInactiveProcessor(this), remotingChannelInactiveExecutor);
	}
	

	@Override
	public void start() {
		this.remotingServer.start();
	}

	public RegistryConsumerManager getConsumerManager() {
		return consumerManager;
	}

	public RegistryProviderManager getProviderManager() {
		return providerManager;
	}

	public NettyRemotingServer getRemotingServer() {
		return remotingServer;
	}

	public void setRemotingServer(NettyRemotingServer remotingServer) {
		this.remotingServer = remotingServer;
	}
	


}
