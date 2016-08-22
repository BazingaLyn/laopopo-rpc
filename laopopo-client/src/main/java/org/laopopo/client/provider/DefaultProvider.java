package org.laopopo.client.provider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.laopopo.client.provider.flow.control.FlowController;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.remoting.model.RemotingTransporter;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyRemotingClient;
import org.laopopo.remoting.netty.NettyRemotingServer;
import org.laopopo.remoting.netty.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 服务提供者端
 * @time 2016年8月16日
 * @modifytime
 */
public class DefaultProvider implements Provider {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultProvider.class);

	private NettyClientConfig clientConfig;

	private NettyServerConfig serverConfig;

	// 连接monitor和注册中心
	private NettyRemotingClient nettyRemotingClient;

	// 等待被Consumer连接
	private NettyRemotingServer nettyRemotingServer;

	private ProviderController providerController;
	
	private ExecutorService remotingExecutor;

	private ExecutorService remotingChannelInactiveExecutor;
	
	private List<RemotingTransporter> publishRemotingTransporters;

	// 定时任务
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("provider-timer"));

	public DefaultProvider(NettyClientConfig clientConfig, NettyServerConfig serverConfig) {
		this.clientConfig = clientConfig;
		this.serverConfig = serverConfig;
		providerController = new ProviderController(this);
		initialize();
	}

	private void initialize() {
		
		this.nettyRemotingServer = new NettyRemotingServer(this.serverConfig);
		this.nettyRemotingClient = new NettyRemotingClient(this.clientConfig);

		this.remotingExecutor = Executors.newFixedThreadPool(serverConfig.getServerWorkerThreads(), new NamedThreadFactory("providerExecutorThread_"));

		this.remotingChannelInactiveExecutor = Executors.newFixedThreadPool(serverConfig.getChannelInactiveHandlerThreads(), new NamedThreadFactory(
				"providerChannelInActiveExecutorThread_"));
		
		//注册处理器
		 this.registerProcessor();

	}

	private void registerProcessor() {
		this.nettyRemotingServer.registerDefaultProcessor(new DefaultProviderProcessor(this), this.remotingExecutor);
		this.nettyRemotingServer.registerChannelInactiveProcessor(new DefaultProviderChannelInactiveProcessor(this), remotingChannelInactiveExecutor);
	}


	@Override
	public void publishedAndStartProvider(String address) throws InterruptedException, RemotingException {
		providerController.getRegistryController().publishedAndStartProvider(address);
	}

	@Override
	public void publishServiceAndListening(String listeningAddress,FlowController controller, Object... obj) {
		if(logger.isDebugEnabled()){
			logger.debug("[{}] accept consumer request",listeningAddress);
		}
		this.publishRemotingTransporters =  providerController.getLocalServerWrapperManager().wrapperRegisterInfo(listeningAddress,controller,obj);
	}

	public List<RemotingTransporter> getPublishRemotingTransporters() {
		return publishRemotingTransporters;
	}

	public void setPublishRemotingTransporters(List<RemotingTransporter> publishRemotingTransporters) {
		this.publishRemotingTransporters = publishRemotingTransporters;
	}

	@Override
	public void start() {
		nettyRemotingClient.start();
	}

	public NettyRemotingClient getNettyRemotingClient() {
		return nettyRemotingClient;
	}

	public void setNettyRemotingClient(NettyRemotingClient nettyRemotingClient) {
		this.nettyRemotingClient = nettyRemotingClient;
	}

	public ProviderController getProviderController() {
		return providerController;
	}

	public void setProviderController(ProviderController providerController) {
		this.providerController = providerController;
	}

	public ScheduledExecutorService getScheduledExecutorService() {
		return scheduledExecutorService;
	}
	
	
}
