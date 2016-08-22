package org.laopopo.common.transport.body;

import java.util.ArrayList;
import java.util.List;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description 注册中心向consumer反馈的服务信息
 * @time 2016年8月15日
 * @modifytime 2016年8月18日 修改成List 这样方便订阅的时候，一起将所有的服务信息发送出去
 */
public class SubcribeResultCustomBody implements CommonCustomBody {
	
	
	private List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();
	
	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}
	
	public static class ServiceInfo {
		private String host;

		private int port;
		// 服务名
		private String serviceProviderName;
		// 是否该服务是VIP服务，如果该服务是VIP服务，走特定的channel，也可以有降级的服务
		private boolean isVIPService;
		//权重
		private volatile int weight;
		// 建议连接数 hashCode()与equals()不把connCount计算在内
		private volatile int connCount;

		public ServiceInfo(String host, int port, String serviceProviderName, boolean isVIPService, int weight, int connCount) {
			this.host = host;
			this.port = port;
			this.serviceProviderName = serviceProviderName;
			this.isVIPService = isVIPService;
			this.weight = weight;
			this.connCount = connCount;
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
			return "ServiceInfo [host=" + host + ", port=" + port + ", serviceProviderName=" + serviceProviderName + ", isVIPService=" + isVIPService
					+ ", weight=" + weight + ", connCount=" + connCount + "]";
		}
		
	}
	

	public List<ServiceInfo> getServiceInfos() {
		return serviceInfos;
	}

	public void setServiceInfos(List<ServiceInfo> serviceInfos) {
		this.serviceInfos = serviceInfos;
	}

	@Override
	public String toString() {
		return "SubcribeResultCustomBody [serviceInfos=" + serviceInfos + "]";
	}

}
