package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description 注册中心向consumer反馈的服务信息
 * @time 2016年8月15日
 * @modifytime
 */
public class SubcribeResultCustomBody implements CommonCustomBody {

	private String host;

	private int port;
	// 组别
	private String group;
	// 版本信息
	private String version;
	// 服务名
	private String serviceProviderName;
	// 是否该服务是VIP服务，如果该服务是VIP服务，走特定的channel，也可以有降级的服务
	private boolean isVIPService;
	
	public SubcribeResultCustomBody(String host, int port, String group, String version, String serviceProviderName, boolean isVIPService) {
		this.host = host;
		this.port = port;
		this.group = group;
		this.version = version;
		this.serviceProviderName = serviceProviderName;
		this.isVIPService = isVIPService;
	}

	@Override
	public void checkFields() throws RemotingCommmonCustomException {
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

	public String getServiceProviderName() {
		return serviceProviderName;
	}

	public void setServiceProviderName(String serviceProviderName) {
		this.serviceProviderName = serviceProviderName;
	}

	public boolean isVIPService() {
		return isVIPService;
	}

	public void setVIPService(boolean isVIPService) {
		this.isVIPService = isVIPService;
	}

	@Override
	public String toString() {
		return "SubcribeResultCustomBody [host=" + host + ", port=" + port + ", group=" + group + ", version=" + version + ", serviceProviderName="
				+ serviceProviderName + ", isVIPService=" + isVIPService + "]";
	}
	

}
