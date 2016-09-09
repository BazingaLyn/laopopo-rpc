package org.laopopo.client.provider;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.laopopo.client.metrics.Metrics;
import org.laopopo.client.provider.DefaultServiceProviderContainer.CurrentServiceState;
import org.laopopo.client.provider.model.DefaultProviderInactiveProcessor;
import org.laopopo.client.provider.model.ServiceWrapper;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.AckCustomBody;
import org.laopopo.common.transport.body.ManagerServiceCustomBody;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.common.utils.Pair;
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
 * @description 服务提供者端的具体实现
 * @time 2016年8月16日
 * @modifytime 2016年8月23日
 */
public class DefaultProvider implements Provider {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProvider.class);

	private NettyClientConfig clientConfig; // 向注册中心连接的netty client配置
	private NettyServerConfig serverConfig; // 等待服务提供者连接的netty server的配置
	private NettyRemotingClient nettyRemotingClient; // 连接monitor和注册中心
	private NettyRemotingServer nettyRemotingServer; // 等待被Consumer连接
	private NettyRemotingServer nettyRemotingVipServer; // 等待被Consumer VIP连接
	private ProviderRegistryController providerController; // provider端向注册中心连接的业务逻辑的控制器
	private ProviderRPCController providerRPCController; // consumer端远程调用的核心控制器
	private ExecutorService remotingExecutor; // RPC调用的核心线程执行器
	private ExecutorService remotingVipExecutor; // RPC调用的核心线程执行器
	// 定时检查 TODO
	private Channel monitorChannel; // 连接monitor端的channel

	/********* 要发布的服务的信息 ***********/
	private List<RemotingTransporter> publishRemotingTransporters;
	/************ 全局发布的信息 ************/
	private ConcurrentMap<String, PublishServiceCustomBody> globalPublishService = new ConcurrentHashMap<String, PublishServiceCustomBody>();
	/***** 注册中心的地址 ******/
	private String registryAddress;
	/******* 服务暴露给consumer的地址 ********/
	private int exposePort;
	/************* 监控中心的monitor的地址 *****************/
	private String monitorAddress;
	/*********** 要提供的服务 ***************/
	private Object[] obj;

	// 当前provider端状态是否健康，也就是说如果注册宕机后，该provider端的实例信息是失效，这是需要重新发送注册信息,因为默认状态下start就是发送，只有channel
	// inactive的时候说明短线了，需要重新发布信息
	private boolean ProviderStateIsHealthy = true;

	// 定时任务执行器
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("provider-timer"));

	public DefaultProvider(NettyClientConfig clientConfig, NettyServerConfig serverConfig) {
		this.clientConfig = clientConfig;
		this.serverConfig = serverConfig;
		providerController = new ProviderRegistryController(this);
		providerRPCController = new ProviderRPCController(this);
		initialize();
	}

	private void initialize() {

		this.nettyRemotingServer = new NettyRemotingServer(this.serverConfig);
		this.nettyRemotingClient = new NettyRemotingClient(this.clientConfig);
		this.nettyRemotingVipServer = new NettyRemotingServer(this.serverConfig);

		this.remotingExecutor = Executors.newFixedThreadPool(serverConfig.getServerWorkerThreads(), new NamedThreadFactory("providerExecutorThread_"));
		this.remotingVipExecutor = Executors.newFixedThreadPool(serverConfig.getServerWorkerThreads() / 2, new NamedThreadFactory("providerExecutorThread_"));
		// 注册处理器
		this.registerProcessor();

		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// 延迟5秒，每隔60秒开始 像其发送注册服务信息
				try {
					logger.info("schedule check publish service");
					if (!ProviderStateIsHealthy) {
						logger.info("channel which connected to registry,has been inactived,need to republish service");
						DefaultProvider.this.publishedAndStartProvider();
					}
				} catch (Exception e) {
					logger.warn("schedule publish failed [{}]", e.getMessage());
				}
			}
		}, 60, 60, TimeUnit.SECONDS);

		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					logger.info("ready send message");
					DefaultProvider.this.providerController.getRegistryController().checkPublishFailMessage();
				} catch (InterruptedException | RemotingException e) {
					logger.warn("schedule republish failed [{}]", e.getMessage());
				}
			}
		}, 1, 1, TimeUnit.MINUTES);

		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				logger.info("ready prepare send Report");
				Metrics.scheduledSendReport();
			}
		}, 3, 60, TimeUnit.SECONDS);

		// 如果监控中心的地址不是null，则需要定时发送统计信息
		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				DefaultProvider.this.providerController.getProviderMonitorController().sendMetricsInfo();
			}
		}, 5, 60, TimeUnit.SECONDS);

		this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					DefaultProvider.this.providerController.getProviderMonitorController().checkMonitorChannel();
				} catch (InterruptedException e) {
					logger.warn("schedule check monitor channel failed [{}]", e.getMessage());
				}
			}
		}, 30, 60, TimeUnit.SECONDS);
	}

	private void registerProcessor() {
		// provider端作为client端去连接registry注册中心的处理器
		this.nettyRemotingClient.registerProcessor(LaopopoProtocol.DEGRADE_SERVICE, new DefaultProviderRegistryProcessor(this), null);
		// provider端连接registry链接inactive的时候要进行的操作(设置registry的状态为不健康，告之registry重新发送服务注册信息)
		this.nettyRemotingClient.registerChannelInactiveProcessor(new DefaultProviderInactiveProcessor(this), null);
		// provider端作为netty的server端去等待调用者连接的处理器，此处理器只处理RPC请求
		this.nettyRemotingServer.registerDefaultProcessor(new DefaultProviderRPCProcessor(this), this.remotingExecutor);
		this.nettyRemotingVipServer.registerDefaultProcessor(new DefaultProviderRPCProcessor(this), this.remotingVipExecutor);
	}

	public List<RemotingTransporter> getPublishRemotingTransporters() {
		return publishRemotingTransporters;
	}

	@Override
	public void publishedAndStartProvider() throws InterruptedException, RemotingException {

		logger.info("publish service....");
		providerController.getRegistryController().publishedAndStartProvider();
		// 发布之后再次将服务状态改成true
		ProviderStateIsHealthy = true;
	}

	@Override
	public Provider publishService(Object... obj) {
		this.obj = obj;
		return this;
	}

	@Override
	public void handlerRPCRequest(RemotingTransporter request, Channel channel) {
		providerRPCController.handlerRPCRequest(request, channel);
	}

	@Override
	public Provider serviceListenPort(int port) {
		this.exposePort = port;
		return this;
	}

	@Override
	public Provider registryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
		return this;
	}

	@Override
	public Provider monitorAddress(String monitorAddress) {
		this.monitorAddress = monitorAddress;
		return this;
	}

	@Override
	public void start() throws InterruptedException, RemotingException {

		// 编织服务
		this.publishRemotingTransporters = providerController.getLocalServerWrapperManager().wrapperRegisterInfo(this.getExposePort(), this.obj);

		logger.info("registry center address [{}] servicePort [{}] service [{}]", this.registryAddress, this.exposePort, this.publishRemotingTransporters);

		// 记录发布的信息的记录，方便其他地方做使用
		initGlobalService();

		nettyRemotingClient.start();
		// 发布任务
		this.publishedAndStartProvider();
		logger.info("provider start successfully");

		int _port = this.exposePort;
		this.serverConfig.setListenPort(exposePort);
		this.nettyRemotingServer.start();

		int vipPort = _port - 2;
		this.serverConfig.setListenPort(vipPort);
		this.nettyRemotingVipServer.start();

		if (monitorAddress != null) {
			initMonitorChannel();
		}

	}

	private void initGlobalService() {
		List<RemotingTransporter> list = this.publishRemotingTransporters; // Stack
																			// copy

		if (null != list && !list.isEmpty()) {
			for (RemotingTransporter remotingTransporter : list) {
				PublishServiceCustomBody customBody = (PublishServiceCustomBody) remotingTransporter.getCustomHeader();
				String serviceName = customBody.getServiceProviderName();
				this.globalPublishService.put(serviceName, customBody);
			}
		}
	}

	public void initMonitorChannel() throws InterruptedException {
		monitorChannel = this.connectionToMonitor();
	}

	/**
	 * 处理注册中心发送过来的服务降级的请求，请求体是ServiceName
	 * 
	 * @param request
	 * @param channel
	 * @return
	 */
	public RemotingTransporter handlerDegradeService(RemotingTransporter request, Channel channel) {

		// 默认的ack返回体
		AckCustomBody ackCustomBody = new AckCustomBody(request.getOpaque(), false);
		RemotingTransporter remotingTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, request.getOpaque());

		// 发布的服务是空的时候，默认返回操作失败
		if (publishRemotingTransporters == null || publishRemotingTransporters.size() == 0) {
			return remotingTransporter;
		}
		// 请求体
		ManagerServiceCustomBody subcribeRequestCustomBody = serializerImpl().readObject(request.bytes(), ManagerServiceCustomBody.class);
		// 服务名
		String serviceName = subcribeRequestCustomBody.getSerivceName();

		// 判断请求的服务名是否在发布的服务中
		boolean checkSerivceIsExist = false;

		for (RemotingTransporter eachTransporter : publishRemotingTransporters) {

			PublishServiceCustomBody body = (PublishServiceCustomBody) eachTransporter.getCustomHeader();
			if (body.getServiceProviderName().equals(serviceName) && body.isSupportDegradeService()) {
				checkSerivceIsExist = true;
				break;
			}
		}

		if (checkSerivceIsExist) {
			// 获取到当前服务的状态
			final Pair<CurrentServiceState, ServiceWrapper> pair = DefaultProvider.this.getProviderController().getProviderContainer()
					.lookupService(serviceName);
			CurrentServiceState currentServiceState = pair.getKey();
			// 如果已经降级了，则直接返回成功
			currentServiceState.getHasDegrade().set(!currentServiceState.getHasDegrade().get());
			ackCustomBody.setSuccess(true);
		}
		return remotingTransporter;
	}

	private Channel connectionToMonitor() throws InterruptedException {
		return this.nettyRemotingClient.createChannel(monitorAddress);
	}

	public NettyRemotingClient getNettyRemotingClient() {
		return nettyRemotingClient;
	}

	public ProviderRegistryController getProviderController() {
		return providerController;
	}

	public String getRegistryAddress() {
		return registryAddress;
	}

	public int getExposePort() {
		return exposePort;
	}

	public void setExposePort(int exposePort) {
		this.exposePort = exposePort;
	}

	public ProviderRPCController getProviderRPCController() {
		return providerRPCController;
	}

	public boolean isProviderStateIsHealthy() {
		return ProviderStateIsHealthy;
	}

	public void setProviderStateIsHealthy(boolean providerStateIsHealthy) {
		ProviderStateIsHealthy = providerStateIsHealthy;
	}

	public Channel getMonitorChannel() {
		return monitorChannel;
	}

	public void setMonitorChannel(Channel monitorChannel) {
		this.monitorChannel = monitorChannel;
	}

	public String getMonitorAddress() {
		return monitorAddress;
	}

	public void setMonitorAddress(String monitorAddress) {
		this.monitorAddress = monitorAddress;
	}

	public ConcurrentMap<String, PublishServiceCustomBody> getGlobalPublishService() {
		return globalPublishService;
	}

	public void setGlobalPublishService(ConcurrentMap<String, PublishServiceCustomBody> globalPublishService) {
		this.globalPublishService = globalPublishService;
	}

}
