package org.laopopo.example.generic.test_7;

import org.laopopo.client.consumer.ConsumerClient;
import org.laopopo.client.consumer.proxy.ProxyFactory;
import org.laopopo.common.loadbalance.LoadBalanceStrategy;
import org.laopopo.common.utils.UnresolvedAddress;
import org.laopopo.example.generic.test_5.HelloService;

public class ConsumerTest {
	
	public static void main(String[] args) throws Exception {
		
		ConsumerClient client = new ConsumerClient();

		client.start();
		
		UnresolvedAddress addresses_1 = new UnresolvedAddress("127.0.0.1", 9001);
		UnresolvedAddress addresses_2 = new UnresolvedAddress("127.0.0.1", 8001);
		UnresolvedAddress addresses_3 = new UnresolvedAddress("127.0.0.1", 7001);
		
		HelloService helloService = ProxyFactory.factory(HelloService.class).consumer(client).addProviderAddress(addresses_1,addresses_2,addresses_3).loadBalance(LoadBalanceStrategy.WEIGHTINGRANDOM).timeoutMillis(3000l).newProxyInstance();
		
		for(int index = 1;index < 45;index++){
			
			Thread.sleep(1000l);
			String str = helloService.sayHello("Lyncc");
			System.out.println("当前调用的次数是：" + index);
			System.out.println(str);
		}
		
	}

}
