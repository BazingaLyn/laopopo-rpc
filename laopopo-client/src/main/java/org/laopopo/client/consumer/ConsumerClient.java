package org.laopopo.client.consumer;

import io.netty.channel.Channel;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;

import org.laopopo.common.exception.remoting.RemotingSendRequestException;
import org.laopopo.common.exception.remoting.RemotingTimeoutException;
import org.laopopo.common.exception.rpc.NoServiceException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.serialization.SerializerHolder;
import org.laopopo.common.transport.body.RequestCustomBody;
import org.laopopo.common.transport.body.ResponseCustomBody;
import org.laopopo.common.utils.ChannelGroup;
import org.laopopo.remoting.model.RemotingTransporter;
import org.laopopo.remoting.netty.NettyClientConfig;

public class ConsumerClient extends DefaultConsumer {


	public static final long DEFAULT_TIMEOUT = 3 * 1000l;
	

	public ConsumerClient(NettyClientConfig registryClientConfig, NettyClientConfig providerClientConfig, ConsumerConfig consumerConfig) {
		super(registryClientConfig, providerClientConfig, consumerConfig);
	}

	@Override
	public Object call(String serviceName, Object... args) throws Throwable {
		return call(serviceName, DEFAULT_TIMEOUT, args);
	}

	@Override
	public Object call(String serviceName, long timeout, Object... args) throws Throwable {
		// 查看该服务是否已经可用，第一次调用的时候，需要预热

		if (null == serviceName || serviceName.length() == 0) {
			throw new NoServiceException("调用的服务名不能为空");
		}
		ChannelGroup channelGroup = getAllMatchedChannel(serviceName);
		if (channelGroup.size() == 0) {
			throw new NoServiceException("没有第三方提供该服务，请检查服务名");
		}

		RequestCustomBody body = new RequestCustomBody();
		body.setArgs(args);
		body.setServiceName(serviceName);
		RemotingTransporter request = RemotingTransporter.createRequestTransporter(LaopopoProtocol.RPC_REQUEST, body);
		RemotingTransporter response = sendRpcRequestToProvider(channelGroup.next(),request);
		ResponseCustomBody customBody = serializerImpl().readObject(response.bytes(), ResponseCustomBody.class);
		return customBody.getResultWrapper().getResult();
	}

	private RemotingTransporter sendRpcRequestToProvider(Channel channel, RemotingTransporter request) throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException {
		return super.providerNettyRemotingClient.invokeSyncImpl(channel, request, 3000l);
	}

	private ChannelGroup getAllMatchedChannel(String serviceName) {
		CopyOnWriteArrayList<ChannelGroup> channelGroups = ServiceChannelGroup.getChannelGroupByServiceName(serviceName);
		return loadBalance(channelGroups);
	}

	private ChannelGroup loadBalance(CopyOnWriteArrayList<ChannelGroup> group) {
		int count = group.size();
		if (count == 0) {
			throw new IllegalArgumentException("empty elements for select");
		}
		Object[] wcObjects = group.toArray();
		if (count == 1) {
			return (ChannelGroup) (wcObjects[0]);
		}
		int totalWeight = 0;
		int[] weightSnapshots = new int[count];
		for (int i = 0; i < count; i++) {
			totalWeight += (weightSnapshots[i] = getWeight((ChannelGroup) wcObjects[i]));
		}

		boolean allSameWeight = true;
		for (int i = 1; i < count; i++) {
			if (weightSnapshots[0] != weightSnapshots[i]) {
				allSameWeight = false;
				break;
			}
		}

		ThreadLocalRandom random = ThreadLocalRandom.current();
		// 如果权重不相同且总权重大于0, 则按总权重数随机
		if (!allSameWeight && totalWeight > 0) {
			int offset = random.nextInt(totalWeight);
			// 确定随机值落在哪个片
			for (int i = 0; i < count; i++) {
				offset -= weightSnapshots[i];
				if (offset < 0) {
					return (ChannelGroup) wcObjects[i];
				}
			}
		}

		return (ChannelGroup) wcObjects[random.nextInt(count)];
	}

	private int getWeight(ChannelGroup channelGroup) {
		return channelGroup.getWeight();
	}


}
