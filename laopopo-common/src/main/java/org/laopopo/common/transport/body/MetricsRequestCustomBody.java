package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description 管控页面发送给注册中心集群的统计信息
 * @time 2016年8月30日
 * @modifytime
 */
public class MetricsRequestCustomBody implements CommonCustomBody {
	
	private String serviceName;

	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
