package org.laopopo.example.generic.test_4;

import org.laopopo.client.consumer.ConsumerClient;
import org.laopopo.client.consumer.proxy.ProxyFactory;
import org.laopopo.common.utils.UnresolvedAddress;

/**
 * 
 * @author BazingaLyn
 * @description 测试consumer直连provider，进行服务调用
 * @time
 * @modifytime
 */
public class ConsumerTest {
	
	public static void main(String[] args) throws Exception {
		
		ConsumerClient client = new ConsumerClient();

		client.start();
		
		UnresolvedAddress addresses = new UnresolvedAddress("127.0.0.1", 8899);
		
		HelloService helloService = ProxyFactory.factory(HelloService.class).consumer(client).addProviderAddress(addresses).timeoutMillis(3000l).newProxyInstance();
		
		for(int i = 0; i < 10000;i++){
			
			String str = helloService.sayHello("Lyncc");
			System.out.println(str);
		}
		
			
		
		
	}

}
