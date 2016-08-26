package org.laopopo.example.demo.service;

import org.laopopo.client.annotation.RPCService;

public class HelloSerivceImpl implements HelloSerivce {

	@Override
	@RPCService(responsibilityName="xiaoy",serviceName="LAOPOPO.TEST.SAYHELLO",isVIPService = false,isSupportDegradeService = true,degradeServicePath="org.laopopo.client.provider.HelloServiceMock",degradeServiceDesc="默认返回hello")
	public String sayHello(String str) {
		return "hello "+ str;
	}

}
