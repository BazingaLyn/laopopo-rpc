package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class SubcribeRequestCustomBody implements CommonCustomBody {
	
	private String serviceName;
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}
	

}
