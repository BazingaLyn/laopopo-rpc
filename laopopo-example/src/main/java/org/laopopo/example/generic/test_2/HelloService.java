package org.laopopo.example.generic.test_2;

import org.laopopo.client.annotation.RPConsumer;

public interface HelloService {

	@RPConsumer(serviceName="LAOPOPO.TEST.SAYHELLO")
	String sayHello(String str);
	
}
