package org.laopopo.base.registry;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.laopopo.base.registry.model.RegistryPersistRecord;
import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.common.utils.PersistUtils;
import org.laopopo.registry.RegistryServer;
import org.laopopo.remoting.netty.NettyRemotingServer;
import org.laopopo.remoting.netty.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * #######注册中心######
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
 * 5)接收管理者的一些信息请求，比如 服务统计 | 某个实例的服务降级 | 通知消费者的访问策略  | 改变某个服务实例的比重
 * 6)将管理者对服务的一些信息 例如审核结果，负载算法等信息持久化到硬盘
 * @time 2016年8月15日
 * @modifytime
 */
public class DefaultRegistryServer implements RegistryServer {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultRegistryServer.class);
	
	
    private final NettyServerConfig nettyServerConfig;       //netty Server的一些配置文件
    private RegistryServerConfig registryServerConfig;       //注册中心的配置文件
	private NettyRemotingServer remotingServer;  	         //注册中心的netty server端
	private RegistryConsumerManager consumerManager;         //注册中心消费侧的管理逻辑控制类
	private RegistryProviderManager providerManager;         //注册中心服务提供者的管理逻辑控制类
	private ExecutorService remotingExecutor;                //执行器
	private ExecutorService remotingChannelInactiveExecutor; //channel inactive的线程执行器
	
	//定时任务
    private final ScheduledExecutorService scheduledExecutorService = Executors
    		.newSingleThreadScheduledExecutor(new NamedThreadFactory("registry-timer"));
	
    /**
     * 
     * @param nettyServerConfig 注册中心的netty的配置文件 至少需要配置listenPort
     * @param nettyClientConfig 注册中心连接Monitor端的netty配置文件，至少需要配置defaultAddress值 这边monitor是单实例，所以address一个就好
     */
    public DefaultRegistryServer(NettyServerConfig nettyServerConfig,RegistryServerConfig registryServerConfig) {
    	this.nettyServerConfig = nettyServerConfig;
    	this.registryServerConfig = registryServerConfig;
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
		 
		 //从硬盘上恢复一些服务的信息
		 this.recoverServiceInfoFromDisk();
		 
		 this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					// 延迟60秒，每隔60秒开始 定时向consumer发送消费者消费失败的信息
					try {
						DefaultRegistryServer.this.getConsumerManager().checkSendFailedMessage();
					} catch (Exception e) {
						logger.warn("schedule publish failed [{}]",e.getMessage());
					} 
				}
		}, 60, 60, TimeUnit.SECONDS);
		 
		 this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					// 延迟60秒，每隔一段时间将一些服务信息持久化到硬盘上
					try {
						DefaultRegistryServer.this.getProviderManager().persistServiceInfo();
					} catch (Exception e) {
						logger.warn("schedule persist failed [{}]",e.getMessage());
					} 
				}
		}, 60, this.registryServerConfig.getPersistTime(), TimeUnit.SECONDS);
	}

	/**
	 * 从硬盘上恢复一些服务的审核负载算法的信息
	 */
	private void recoverServiceInfoFromDisk() {
		
		String persistString = PersistUtils.file2String(this.registryServerConfig.getStorePathRootDir());
		
		if (null != persistString) {
			List<RegistryPersistRecord> registryPersistRecords = JSON.parseArray(persistString.trim(), RegistryPersistRecord.class);
			
			if (null != registryPersistRecords) {
				for (RegistryPersistRecord metricsReporter : registryPersistRecords) {
					
				     String serviceName = metricsReporter.getServiceName();
				     this.getProviderManager().getHistoryRecords().put(serviceName, metricsReporter);
				     
				}
			}
		}
		
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

	public RegistryServerConfig getRegistryServerConfig() {
		return registryServerConfig;
	}

	public void setRegistryServerConfig(RegistryServerConfig registryServerConfig) {
		this.registryServerConfig = registryServerConfig;
	}

}
