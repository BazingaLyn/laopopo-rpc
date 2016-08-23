package org.laopopo.client.provider;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.common.transport.body.AckCustomBody;
import org.laopopo.common.utils.SystemClock;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description provider端专业去连接registry的管理控制对象，用来处理provider与registry一切交互事宜
 * @time 2016年8月16日
 * @modifytime
 */
public class RegistryController {

	private static final Logger logger = LoggerFactory.getLogger(RegistryController.class);

	private DefaultProvider defaultProvider;

	private final ConcurrentMap<Long, MessageNonAck> messagesNonAcks = new ConcurrentHashMap<Long, MessageNonAck>();

	public RegistryController(DefaultProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

	/**
	 * 
	 * @param address
	 *            registry的地址，多个地址格式是host1:port1,host2:port2
	 * @throws InterruptedException
	 * @throws RemotingException
	 */
	public void publishedAndStartProvider() throws InterruptedException, RemotingException {

		// stack copy
		List<RemotingTransporter> transporters = defaultProvider.getPublishRemotingTransporters();
		
		if(null == transporters || transporters.isEmpty()){
			logger.warn("service is empty please call DefaultProvider #publishService method");
			return;
		}
		
		String address = defaultProvider.getRegistryAddress();

		if (address == null) {
            logger.warn("registry center address is empty please check your address");
            return;
		}
		String[] addresses = address.split(",");
		if (null != addresses && addresses.length > 0 ) {
			
			for (String eachAddress : addresses) {
				
				for (RemotingTransporter request : transporters) {

					logger.info("[{}] transporters matched", request);
					messagesNonAcks.put(request.getOpaque(), new MessageNonAck(request, eachAddress));
					RemotingTransporter remotingTransporter = defaultProvider.getNettyRemotingClient().invokeSync(eachAddress, request, 3000);
					AckCustomBody ackCustomBody = serializerImpl().readObject(remotingTransporter.bytes(), AckCustomBody.class);

					logger.info("received ack info [{}]", ackCustomBody);
					messagesNonAcks.remove(ackCustomBody.getRequestId());
				}
			}
		}

	}

	static class MessageNonAck {

		private final long id;

		private final RemotingTransporter msg;
		private final String address;
		private final long timestamp = SystemClock.millisClock().now();

		public MessageNonAck(RemotingTransporter msg, String address) {
			this.msg = msg;
			this.address = address;

			id = msg.getOpaque();
		}

		public long getId() {
			return id;
		}

		public RemotingTransporter getMsg() {
			return msg;
		}

		public String getAddress() {
			return address;
		}

		public long getTimestamp() {
			return timestamp;
		}

	}

}
