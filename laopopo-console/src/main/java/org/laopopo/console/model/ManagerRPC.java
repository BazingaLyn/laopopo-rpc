package org.laopopo.console.model;

public class ManagerRPC {
	
	// 1 禁用 2 降级 3解禁 4取消降级 5
	private Integer managerType;
	
	private String host;
	
	private int port;
	
	private String serviceName;
	
	private Integer serviceState;
	
	private int weightVal;

	public Integer getManagerType() {
		return managerType;
	}

	public void setManagerType(Integer managerType) {
		this.managerType = managerType;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Integer getServiceState() {
		return serviceState;
	}

	public void setServiceState(Integer serviceState) {
		this.serviceState = serviceState;
	}

	public int getWeightVal() {
		return weightVal;
	}

	public void setWeightVal(int weightVal) {
		this.weightVal = weightVal;
	}

	@Override
	public String toString() {
		return "ManagerRPC [managerType=" + managerType + ", host=" + host + ", port=" + port + ", serviceName=" + serviceName + ", serviceState="
				+ serviceState + ", weightVal=" + weightVal + "]";
	}

}
