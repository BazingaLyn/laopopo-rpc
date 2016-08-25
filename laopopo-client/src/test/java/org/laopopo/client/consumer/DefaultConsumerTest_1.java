package org.laopopo.client.consumer;

import org.laopopo.client.consumer.Consumer.SubscribeManager;
import org.laopopo.remoting.netty.NettyClientConfig;

/**
 * 
 * @author BazingaLyn
 * @description 测试
 * @time
 * @modifytime
 */
public class DefaultConsumerTest_1 {
	
	public static void main(String[] args) throws Throwable {
		
		NettyClientConfig registryNettyClientConfig = new NettyClientConfig();
		registryNettyClientConfig.setDefaultAddress("127.0.0.1:18010");
		
		NettyClientConfig provideClientConfig = new NettyClientConfig();
		
		ConsumerClient client = new ConsumerClient(registryNettyClientConfig, provideClientConfig, new ConsumerConfig());
		
		client.start();
		
		SubscribeManager subscribeManager = client.subscribeService("LAOPOPO.TEST.SAYHELLO");
		
		if(!subscribeManager.waitForAvailable(3000l)){
			throw new Exception("init service timeout or init occor exception");
		}
		
		Object obj = client.call("LAOPOPO.TEST.SAYHELLO", "shine");
		if(obj instanceof String){
			System.out.println((String)obj);
		}
		
	}

}
