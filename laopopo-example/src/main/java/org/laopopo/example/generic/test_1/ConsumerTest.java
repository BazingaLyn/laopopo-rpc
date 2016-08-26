package org.laopopo.example.generic.test_1;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.laopopo.client.consumer.Consumer.SubscribeManager;
import org.laopopo.client.consumer.ConsumerClient;
import org.laopopo.client.consumer.ConsumerConfig;
import org.laopopo.client.consumer.ServiceChannelGroup;
import org.laopopo.common.utils.ChannelGroup;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerTest {

	private static final Logger logger = LoggerFactory.getLogger(ConsumerTest.class);

	public static void main(String[] args) throws Throwable {

		Thread t = new Thread(new SubcribeResultScanner(), "timeout.scanner");
		t.setDaemon(true);
		t.start();

		NettyClientConfig registryNettyClientConfig = new NettyClientConfig();
		registryNettyClientConfig.setDefaultAddress("127.0.0.1:18010");

		NettyClientConfig provideClientConfig = new NettyClientConfig();

		ConsumerClient client = new ConsumerClient(registryNettyClientConfig, provideClientConfig, new ConsumerConfig());

		client.start();

		SubscribeManager subscribeManager = client.subscribeService("LAOPOPO.TEST.SAYHELLO");

		if (!subscribeManager.waitForAvailable(3000l)) {
			throw new Exception("no service provider");
		}

		Object obj = client.call("LAOPOPO.TEST.SAYHELLO", "shine");
		if (obj instanceof String) {
			System.out.println((String) obj);
		}
		
		SubscribeManager subscribeManager2 = client.subscribeService("LAOPOPO.TEST.SAYBYE");
		
		if (!subscribeManager2.waitForAvailable(3000l)) {
			throw new Exception("no service provider");
		}

		Object obj1 = client.call("LAOPOPO.TEST.SAYBYE", "shine");
		if (obj1 instanceof String) {
			System.out.println((String) obj1);
		}

	}

	private static class SubcribeResultScanner implements Runnable {

		@Override
		public void run() {

			for (;;) {
				try {
					logger.info("统计中");
					Thread.sleep(10000);
					ConcurrentMap<String, CopyOnWriteArrayList<ChannelGroup>> result = ServiceChannelGroup.getGroups();
					if(result != null){
						for(String serviceName:result.keySet()){
							System.out.println(serviceName);
							for(ChannelGroup channelGroup :result.get(serviceName)){
								System.out.println(channelGroup.toString());
							}
						}
					}
				} catch (Throwable t) {
					logger.error("An exception has been caught while scanning the timeout acknowledges {}.", t);
				}
			}
		}
	}

}
