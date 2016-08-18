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
	
	private String group;
	
	private String version;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}
	

}
