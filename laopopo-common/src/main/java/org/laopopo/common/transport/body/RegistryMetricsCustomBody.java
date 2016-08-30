package org.laopopo.common.transport.body;

import java.util.List;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;
import org.laopopo.common.metrics.ServiceMetrics;

public class RegistryMetricsCustomBody implements CommonCustomBody {
	
	private List<ServiceMetrics> serviceMetricses;

	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}

	public List<ServiceMetrics> getServiceMetricses() {
		return serviceMetricses;
	}

	public void setServiceMetricses(List<ServiceMetrics> serviceMetricses) {
		this.serviceMetricses = serviceMetricses;
	}
	
	
	

}
