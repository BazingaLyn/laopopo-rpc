package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description provider端向Registry注册中心发送的注册服务信息
 * @time 2016年8月15日
 * @modifytime
 */
public class PublishServiceCustomBody implements CommonCustomBody {

	// 服务的地址
	private String host;
	// 服务的端口
	private int port;
	// 服务名
	private String serviceProviderName;
	// 是否该服务是VIP服务，如果该服务是VIP服务，走特定的channel，也可以有降级的服务
	private boolean isVIPService;
	// 是否支持服务降级
	private boolean isSupportDegradeService;
	// 降级服务的mock方法的路径
	private String degradeServicePath;
	// 降级服务的描述
	private String degradeServiceDesc;
	// 服务的权重
	private volatile int weight;
	// 建议连接数 hashCode()与equals()不把connCount计算在内
	private volatile int connCount;

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

	public boolean isSupportDegradeService() {
		return isSupportDegradeService;
	}

	public void setSupportDegradeService(boolean isSupportDegradeService) {
		this.isSupportDegradeService = isSupportDegradeService;
	}

	public String getDegradeServicePath() {
		return degradeServicePath;
	}

	public void setDegradeServicePath(String degradeServicePath) {
		this.degradeServicePath = degradeServicePath;
	}

	public String getDegradeServiceDesc() {
		return degradeServiceDesc;
	}

	public void setDegradeServiceDesc(String degradeServiceDesc) {
		this.degradeServiceDesc = degradeServiceDesc;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getConnCount() {
		return connCount;
	}

	public void setConnCount(int connCount) {
		this.connCount = connCount;
	}

	@Override
	public String toString() {
		return "PublishServiceCustomBody [host=" + host + ", port=" + port + ", serviceProviderName=" + serviceProviderName + ", isVIPService=" + isVIPService
				+ ", isSupportDegradeService=" + isSupportDegradeService + ", degradeServicePath=" + degradeServicePath + ", degradeServiceDesc="
				+ degradeServiceDesc + ", weight=" + weight + ", connCount=" + connCount + "]";
	}
 

}
