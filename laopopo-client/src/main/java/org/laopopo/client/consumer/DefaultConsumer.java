package org.laopopo.client.consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.rpc.RegisterMeta;
import org.laopopo.common.utils.ChannelGroup;
import org.laopopo.common.utils.JUnsafe;
import org.laopopo.common.utils.NettyChannelGroup;
import org.laopopo.common.utils.UnresolvedAddress;
import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyRemotingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 默认的消费类
 * @time 2016年8月18日
 * @modifytime
 */
public abstract class DefaultConsumer implements Consumer, ConsumerRegistry {

	private static final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);

	private NettyClientConfig registryClientConfig;

	private NettyClientConfig providerClientConfig;

	private ConsumerConfig consumerConfig;

	protected NettyRemotingClient registryNettyRemotingClient;

	protected NettyRemotingClient providerNettyRemotingClient;

	private DefaultConsumerRegistry defaultConsumerRegistry;

	private ConsumerManager consumerManager;
	
	protected final ConcurrentMap<UnresolvedAddress, ChannelGroup> addressGroups = new ConcurrentHashMap<UnresolvedAddress, ChannelGroup>();

	private Channel registyChannel;

	public DefaultConsumer(NettyClientConfig registryClientConfig, NettyClientConfig providerClientConfig, ConsumerConfig consumerConfig) {
		this.registryClientConfig = registryClientConfig;
		this.providerClientConfig = providerClientConfig;
		this.consumerConfig = consumerConfig;
		defaultConsumerRegistry = new DefaultConsumerRegistry(this);
		consumerManager = new ConsumerManager(this);
		initialize();
	}

	private void initialize() {
		this.registryNettyRemotingClient = new NettyRemotingClient(this.registryClientConfig);
		this.providerNettyRemotingClient = new NettyRemotingClient(this.providerClientConfig);

		// 注册处理器
		this.registerProcessor();

	}

	private void registerProcessor() {
		this.registryNettyRemotingClient.registerProcessor(LaopopoProtocol.SUBCRIBE_RESULT, new DefaultConsumerRegistryProcessor(this), null);
		this.registryNettyRemotingClient.registerProcessor(LaopopoProtocol.SUBCRIBE_SERVICE_CANCEL, new DefaultConsumerRegistryProcessor(this), null);
	}

	@Override
	public SubscribeManager subscribeService(final String service) {

		SubscribeManager manager = new SubscribeManager() {

			private final ReentrantLock lock = new ReentrantLock();
			private final Condition notifyCondition = lock.newCondition();
			private final AtomicBoolean signalNeeded = new AtomicBoolean(false);

			@Override
			public void start() {
				subcribeService(service, new NotifyListener() {
					
					@Override
					public void notify(RegisterMeta registerMeta, NotifyEvent event) {

						// host
						String remoteHost = registerMeta.getAddress().getHost();
						// port vip服务 port端口号-2
						int remotePort = registerMeta.isVIPService() ? (registerMeta.getAddress().getPort() - 2) : registerMeta.getAddress().getPort();

						final ChannelGroup group = group(new UnresolvedAddress(remoteHost, remotePort));
						if (event == NotifyEvent.CHILD_ADDED) {
							// 链路复用，如果此host和port对应的链接的channelGroup是已经存在的，则无需建立新的链接，只需要将此group与service建立关系即可
							if (!group.isAvailable()) {

								int connCount = registerMeta.getConnCount() < 0 ? 1 : registerMeta.getConnCount();

								group.setWeight(registerMeta.getWeight());

								for (int i = 0; i < connCount; i++) {

									try {
										// 所有的consumer与provider之间的链接不进行短线重连操作
										DefaultConsumer.this.getProviderNettyRemotingClient().setreconnect(false);
										DefaultConsumer.this.getProviderNettyRemotingClient().getBootstrap()
												.connect(ConnectionUtils.string2SocketAddress(remoteHost + ":" + remotePort)).addListener(new ChannelFutureListener() {

													@Override
													public void operationComplete(ChannelFuture future) throws Exception {
														group.add(future.channel());
														onSucceed(signalNeeded.getAndSet(false));
													}
													
												});
									} catch (Exception e) {
										logger.error("connection provider host [{}] and port [{}] occor exception [{}]", remoteHost, remotePort, e.getMessage());
									}
								}
							}else{
								onSucceed(signalNeeded.getAndSet(false));
							}
							ServiceChannelGroup.addIfAbsent(service,group);
						}else if(event == NotifyEvent.CHILD_REMOVED){
							ServiceChannelGroup.removedIfAbsent(service, group);
						}
					}
				});
			}

			@Override
			public boolean waitForAvailable(long timeoutMillis) {
				System.out.println(service +" GGGGGGGGG");
				if (isServiceAvailable(service)) {
					System.out.println(service +" is ready");
                    return true;
                }
				boolean available = false;
                long start = System.nanoTime();
                final ReentrantLock _look = lock;
                _look.lock();
                try {
                    while (!isServiceAvailable(service)) {
                        signalNeeded.set(true);
                        notifyCondition.await(timeoutMillis, MILLISECONDS);

                        available = isServiceAvailable(service);
                        if (available || (System.nanoTime() - start) > MILLISECONDS.toNanos(timeoutMillis)) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    JUnsafe.throwException(e);
                } finally {
                    _look.unlock();
                }
                return available;
			}

			private boolean isServiceAvailable(String service) {
				CopyOnWriteArrayList<ChannelGroup> list = ServiceChannelGroup.getChannelGroupByServiceName(service);
				if(list == null){
					return false;
				}else{
					for(ChannelGroup channelGroup : list){
						if(channelGroup.isAvailable()){
							return true;
						}
					}
				}
				return false;
			}

			private void onSucceed(boolean doSignal) {
				if (doSignal) {
                    final ReentrantLock _look = lock;
                    _look.lock();
                    try {
                        notifyCondition.signalAll();
                    } finally {
                        _look.unlock();
                    }
                }
			}

		};
		manager.start();
		return manager;
	}
	
	@Override
	public void subcribeService(String subcribeServices, NotifyListener listener) {
		if (subcribeServices != null) {
			this.defaultConsumerRegistry.subcribeService(subcribeServices,listener);
		}
	}

//	private void subcribeService(String serviceNames) {
//		if (serviceNames != null) {
//			this.defaultConsumerRegistry.subcribeService(serviceNames);
//		}
//	}

	@Override
	public void start() {
		this.registryNettyRemotingClient.start();
		this.providerNettyRemotingClient.setreconnect(false);
		this.providerNettyRemotingClient.start();
		getOrUpdateHealthyChannel();
	}

	@Override
	public void getOrUpdateHealthyChannel() {

		String addresses = this.registryClientConfig.getDefaultAddress();

		if (registyChannel != null && registyChannel.isActive() && registyChannel.isWritable())
			return;

		if (addresses == null || "".equals(addresses)) {
			logger.error("registry address is empty");
			return;
		}
		int retryConnectionTimes = this.consumerConfig.getRetryConnectionRegistryTimes();
		long maxTimeout = this.consumerConfig.getMaxRetryConnectionRegsitryTime();

		String[] adds = addresses.split(",");

		for (int i = 0; i < adds.length; i++) {

			if (registyChannel != null && registyChannel.isActive() && registyChannel.isWritable())
				return;

			String currentAddress = adds[i];
			final long beginTimestamp = System.currentTimeMillis();
			long endTimestamp = beginTimestamp;

			int times = 0;

			for (; times < retryConnectionTimes && (endTimestamp - beginTimestamp) < maxTimeout; times++) {
				try {
					Channel channel = registryNettyRemotingClient.createChannel(currentAddress);
					if (channel != null && channel.isActive() && channel.isWritable()) {
						this.registyChannel = channel;
						break;
					} else {
						continue;
					}
				} catch (InterruptedException e) {
					logger.warn("connection registry center [{}] fail", currentAddress);
					endTimestamp = System.currentTimeMillis();
					continue;
				}
			}
		}

	}

	public NettyRemotingClient getRegistryNettyRemotingClient() {
		return registryNettyRemotingClient;
	}

	public void setRegistryNettyRemotingClient(NettyRemotingClient registryNettyRemotingClient) {
		this.registryNettyRemotingClient = registryNettyRemotingClient;
	}

	public Channel getRegistyChannel() {
		return registyChannel;
	}

	public void setRegistyChannel(Channel registyChannel) {
		this.registyChannel = registyChannel;
	}

	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	public void setConsumerConfig(ConsumerConfig consumerConfig) {
		this.consumerConfig = consumerConfig;
	}

	public NettyClientConfig getRegistryClientConfig() {
		return registryClientConfig;
	}

	public void setRegistryClientConfig(NettyClientConfig registryClientConfig) {
		this.registryClientConfig = registryClientConfig;
	}

	public ConsumerManager getConsumerManager() {
		return consumerManager;
	}

	public void setConsumerManager(ConsumerManager consumerManager) {
		this.consumerManager = consumerManager;
	}

	public NettyRemotingClient getProviderNettyRemotingClient() {
		return providerNettyRemotingClient;
	}

	public void setProviderNettyRemotingClient(NettyRemotingClient providerNettyRemotingClient) {
		this.providerNettyRemotingClient = providerNettyRemotingClient;
	}

	public DefaultConsumerRegistry getDefaultConsumerRegistry() {
		return defaultConsumerRegistry;
	}

	public void setDefaultConsumerRegistry(DefaultConsumerRegistry defaultConsumerRegistry) {
		this.defaultConsumerRegistry = defaultConsumerRegistry;
	}
	
	private ChannelGroup group(UnresolvedAddress address) {

		ChannelGroup group = addressGroups.get(address);
		if (group == null) {
			ChannelGroup newGroup = newChannelGroup(address);
			group = addressGroups.putIfAbsent(address, newGroup);
			if (group == null) {
				group = newGroup;
			}
		}
		return group;
	}

	private ChannelGroup newChannelGroup(UnresolvedAddress address) {
		return new NettyChannelGroup(address);
	}
	

}
