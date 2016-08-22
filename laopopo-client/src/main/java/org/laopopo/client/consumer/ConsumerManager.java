package org.laopopo.client.consumer;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import static org.laopopo.common.utils.Constants.ACK_SUBCRIBE_SERVICE_FAILED;
import static org.laopopo.common.utils.Constants.ACK_SUBCRIBE_SERVICE_SUCCESS;
import static org.laopopo.common.utils.Constants.ACK_SUBCRIBE_SERVICE_CANCEL_SUCCESS;
import static org.laopopo.common.utils.Constants.ACK_SUBCRIBE_SERVICE_CANCEL_FAIL;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.laopopo.client.consumer.ConsumerRegistry.SubcribeService;
import org.laopopo.client.preheater.ConectionPreHeater;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.AckCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody;
import org.laopopo.common.transport.body.SubcribeResultCustomBody.ServiceInfo;
import org.laopopo.common.utils.ChannelGroup;
import org.laopopo.common.utils.NettyChannelGroup;
import org.laopopo.common.utils.UnresolvedAddress;
import org.laopopo.remoting.ConnectionUtils;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 消费者端的一些逻辑处理
 * @time 2016年8月22日
 * @modifytime
 */
public class ConsumerManager {

	private static final Logger logger = LoggerFactory.getLogger(ConsumerManager.class);

	private DefaultConsumer defaultConsumer;

	private final ReentrantReadWriteLock registriesLock = new ReentrantReadWriteLock();

	private final Map<SubcribeService, List<ServiceInfo>> registries = new ConcurrentHashMap<ConsumerRegistry.SubcribeService, List<ServiceInfo>>();
	// TODO group中健康度的检查 定时任务
	protected final ConcurrentMap<UnresolvedAddress, ChannelGroup> addressGroups = new ConcurrentHashMap<UnresolvedAddress, ChannelGroup>();

	public ConsumerManager(DefaultConsumer defaultConsumer) {
		this.defaultConsumer = defaultConsumer;
	}

    /**
     * 处理服务的订阅结果
     * @param request
     * @param channel
     * @return
     */
	public RemotingTransporter handlerSubcribeResult(RemotingTransporter request, Channel channel) {

		AckCustomBody ackCustomBody = new AckCustomBody(request.getOpaque(), false, ACK_SUBCRIBE_SERVICE_FAILED);
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, request.getOpaque());

		SubcribeService subcribeService = null;
		
		SubcribeResultCustomBody subcribeResultCustomBody = serializerImpl().readObject(request.bytes(), SubcribeResultCustomBody.class);

		if (subcribeResultCustomBody != null && subcribeResultCustomBody.getServiceInfos() != null && !subcribeResultCustomBody.getServiceInfos().isEmpty()) {

			for (ServiceInfo serivceInfo : subcribeResultCustomBody.getServiceInfos()) {
				if(null == subcribeService){
					subcribeService = new SubcribeService(serivceInfo.getGroup(), serivceInfo.getVersion(), serivceInfo.getServiceProviderName());
				}
				notify(subcribeService, serivceInfo,ServiceInfoState.CHILD_ADDED);
			}
			//到此为止，说明该服务的链路已经建立成功，该服务算预热成功，可以远程调用
			ConectionPreHeater.finishPreConnection(subcribeService);
		}

		ackCustomBody.setDesc(ACK_SUBCRIBE_SERVICE_SUCCESS);
		ackCustomBody.setSuccess(true);
		return responseTransporter;
	}

	/**
	 * 处理服务取消的时候逻辑处理
	 * @param request
	 * @param channel
	 * @return
	 */
	public RemotingTransporter handlerSubscribeResultCancel(RemotingTransporter request, Channel channel) {
		AckCustomBody ackCustomBody = new AckCustomBody(request.getOpaque(), false, ACK_SUBCRIBE_SERVICE_CANCEL_FAIL);
		RemotingTransporter responseTransporter = RemotingTransporter.createResponseTransporter(LaopopoProtocol.ACK, ackCustomBody, request.getOpaque());

		SubcribeService subcribeService = null;
		
		SubcribeResultCustomBody subcribeResultCustomBody = serializerImpl().readObject(request.bytes(), SubcribeResultCustomBody.class);

		if (subcribeResultCustomBody != null && subcribeResultCustomBody.getServiceInfos() != null && !subcribeResultCustomBody.getServiceInfos().isEmpty()) {

			for (ServiceInfo serivceInfo : subcribeResultCustomBody.getServiceInfos()) {
				if(null == subcribeService){
					subcribeService = new SubcribeService(serivceInfo.getGroup(), serivceInfo.getVersion(), serivceInfo.getServiceProviderName());
				}
				notify(subcribeService, serivceInfo,ServiceInfoState.CHILD_REMOVED);
			}
		}

		ackCustomBody.setDesc(ACK_SUBCRIBE_SERVICE_CANCEL_SUCCESS);
		ackCustomBody.setSuccess(true);
		return responseTransporter;
	}

	/**
	 * 处理某个地址下线的逻辑处理
	 * @param request
	 * @param channel
	 * @return
	 */
	public RemotingTransporter handlerOffline(RemotingTransporter request, Channel channel) {
		return null;
	}

	public enum ServiceInfoState {
		CHILD_ADDED, CHILD_REMOVED
	}

	/************************* ↑为核心方法，下面为内部方法 ************************/

	private void notify(SubcribeService subcribeService, ServiceInfo serivceInfo, ServiceInfoState event) {

		boolean notifyNeeded = false;

		final Lock writeLock = registriesLock.writeLock();
		writeLock.lock();
		try {
			List<ServiceInfo> infos = registries.get(subcribeService);
			if (infos == null) {
				if (event == ServiceInfoState.CHILD_REMOVED) {
					return;
				}
				infos = new ArrayList<ServiceInfo>();
				infos.add(serivceInfo);
				notifyNeeded = true;
			} else {
				if (event == ServiceInfoState.CHILD_REMOVED) {
					infos.remove(serivceInfo);
				} else if (event == ServiceInfoState.CHILD_ADDED) {
					infos.add(serivceInfo);
				}
				notifyNeeded = true;
			}
			registries.put(subcribeService, infos);
		} finally {
			writeLock.unlock();
		}

		if (notifyNeeded) {
			// host
			String remoteHost = serivceInfo.getHost();
			// port vip服务 port端口号-2
			int remotePort = serivceInfo.isVIPService() ? (serivceInfo.getPort() - 2) : serivceInfo.getPort();

			final ChannelGroup group = group(new UnresolvedAddress(remoteHost, remotePort));
			if (event == ServiceInfoState.CHILD_ADDED) {
				// 链路复用，如果此host和port对应的链接的channelGroup是已经存在的，则无需建立新的链接，只需要将此group与service建立关系即可
				if (!group.isAvailable()) {

					int connCount = serivceInfo.getConnCount() < 0 ? 1 : serivceInfo.getConnCount();

					group.setWeight(serivceInfo.getWeight());

					for (int i = 0; i < connCount; i++) {

						Channel channel = connectToProvider(remotePort, remoteHost);
						if (null == channel) {
							logger.warn("port {} and host {} connection failed.", remotePort, remoteHost);
							continue;
						} else {
							group.add(channel);
						}
					}
					ServiceChannelGroup.addIfAbsent(subcribeService,group);
				}
			}else if(event == ServiceInfoState.CHILD_REMOVED){
				ServiceChannelGroup.removedIfAbsent(subcribeService, group);
				//TODO 这边如果此channel只被一个服务使用，那么此时应该最好是关闭channel
			}
		}
	}

	private Channel connectToProvider(int remotePort, String remoteHost) {
		try {
			// 所有的consumer与provider之间的链接不进行短线重连操作
			this.defaultConsumer.getProviderNettyRemotingClient().setreconnect(false);
			ChannelFuture channelFuture = this.defaultConsumer.getProviderNettyRemotingClient().getBootstrap()
					.connect(ConnectionUtils.string2SocketAddress(remoteHost + ":" + remotePort));
			if (channelFuture.isSuccess()) {
				return channelFuture.channel();
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("connection provider host [{}] and port [{}] occor exception [{}]", remoteHost, remotePort, e.getMessage());
			return null;
		}
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
