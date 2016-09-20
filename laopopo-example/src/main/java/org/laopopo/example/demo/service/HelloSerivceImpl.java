package org.laopopo.example.demo.service;

import org.laopopo.client.annotation.RPCService;

/**
 * 
 * @author BazingaLyn
 * @description Demo
 * @time 2016年8月19日
 * @modifytime
 */
public class HelloSerivceImpl implements HelloSerivce {

	@Override
	@RPCService(responsibilityName="xiaoy",
				serviceName="LAOPOPO.TEST.SAYHELLO",
				isVIPService = false,
				isSupportDegradeService = true,
				degradeServicePath="org.laopopo.example.demo.service.HelloServiceMock",
				degradeServiceDesc="默认返回hello")
	public String sayHello(String str) {
		return "hello "+ str;
	}

}
