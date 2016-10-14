package org.laopopo.example.demo.service;

import org.laopopo.client.annotation.RPCService;
import org.laopopo.example.demo.service.HelloSerivce;

public class HelloService_2 implements HelloSerivce {

	@Override
	@RPCService(responsibilityName = "xiaoy", serviceName = "LAOPOPO.TEST.SAYHELLO", weight = 90)
	public String sayHello(String str) {
		return "hello_2";
	}

}
