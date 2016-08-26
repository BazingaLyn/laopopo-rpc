package org.laopopo.example.demo.service;

import org.laopopo.client.annotation.RPCService;

public class ByeServiceImpl implements ByeService {

	@Override
	@RPCService(responsibilityName="fly100%",serviceName ="LAOPOPO.TEST.SAYBYE",isVIPService = true,isSupportDegradeService = false)
	public String sayBye(String str) {
		return "bye " + str;
	}

}
