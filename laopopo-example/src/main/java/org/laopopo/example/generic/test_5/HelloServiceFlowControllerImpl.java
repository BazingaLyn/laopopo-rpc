package org.laopopo.example.generic.test_5;

import org.laopopo.client.annotation.RPCService;
import org.laopopo.example.demo.service.HelloSerivce;

public class HelloServiceFlowControllerImpl implements HelloSerivce {

	@Override
	@RPCService(responsibilityName="xiaoy",serviceName="LAOPOPO.TEST.SAYHELLO",maxCallCountInMinute = 40)
	public String sayHello(String str) {
		return "hello "+ str;
	}

}
