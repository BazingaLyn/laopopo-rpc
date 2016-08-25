package org.laopopo.client.consumer;

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
		
		client.subcribeService("LAOPOPO.TEST.SAYHELLO");
		
		Thread.sleep(1000l);
		
		Object obj = client.call("LAOPOPO.TEST.SAYHELLO", "shine");
		if(obj instanceof String){
			System.out.println((String)obj);
		}
		
	}

}
