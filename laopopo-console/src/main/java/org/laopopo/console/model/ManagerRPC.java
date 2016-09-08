package org.laopopo.console.model;

/**
 * 
 * @author BazingaLyn
 * @description 管理员在管理页面发出来的请求命令
 * @time 2016年9月6日
 * @modifytime
 */
public class ManagerRPC {
	
	
	private Integer managerType; // 1 禁用 2 降级 3解禁 4取消降级 5
	private String host;		 //host
	private int port;			 //port
	private String serviceName;  //服务名
	private Integer serviceState;//服务状态
	private int weightVal;       //权重

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
