package org.laopopo.client.provider;

import org.laopopo.client.annotation.RPCService;

public class ByeServiceImpl implements ByeService {

	@Override
	@RPCService(group="LAOPOPO",version = "1.0.0",responsibilityName="fly100%",serviceName ="LAOPOPO.TEST.SAYBYE",isVIPService = true,isSupportDegradeService = false)
	public String sayBye(String str) {
		return "bye " + str;
	}

}
