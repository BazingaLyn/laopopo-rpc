package org.laopopo.registry.model;

import org.laopopo.common.transport.body.PublishServiceCustomBody;

/**
 * 
 * @author BazingaLyn
 * @description provider端向注册中心提供注册服务的信息类
 * @time 2016年8月15日
 * @modifytime
 */
public class RegisterMeta {

	private Address address = new Address();

	private String serviceName;
	
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
	
	private ServiceReviewState isReviewed = ServiceReviewState.HAS_NOT_REVIEWED;
	
	private boolean hasDegradeService = false;
	
	public RegisterMeta(Address address, String serviceName,boolean isVIPService, boolean isSupportDegradeService, String degradeServicePath,
			String degradeServiceDesc, int weight, int connCount) {
		this.address = address;
		this.serviceName = serviceName;
		this.isVIPService = isVIPService;
		this.isSupportDegradeService = isSupportDegradeService;
		this.degradeServicePath = degradeServicePath;
		this.degradeServiceDesc = degradeServiceDesc;
		this.weight = weight;
		this.connCount = connCount;
	}
	
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
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

	public ServiceReviewState getIsReviewed() {
		return isReviewed;
	}

	public void setIsReviewed(ServiceReviewState isReviewed) {
		this.isReviewed = isReviewed;
	}

	public boolean isHasDegradeService() {
		return hasDegradeService;
	}

	public void setHasDegradeService(boolean hasDegradeService) {
		this.hasDegradeService = hasDegradeService;
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

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}



	public static class Address {
		// 地址
		private String host;
		// 端口
		private int port;

		public Address() {
		}

		public Address(String host, int port) {
			this.host = host;
			this.port = port;
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

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Address address = (Address) o;

			return port == address.port && !(host != null ? !host.equals(address.host) : address.host != null);
		}

		@Override
		public int hashCode() {
			int result = host != null ? host.hashCode() : 0;
			result = 31 * result + port;
			return result;
		}

		@Override
		public String toString() {
			return "Address{" + "host='" + host + '\'' + ", port=" + port + '}';
		}
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		RegisterMeta that = (RegisterMeta) obj;

		return !(address != null ? !address.equals(that.address) : that.address != null)
				&& !(serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null);

	}

	@Override
	public int hashCode() {
		int result = address != null ? address.hashCode() : 0;
		result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
		return result;
	}
	
	@Override
	public String toString() {
		return "RegisterMeta [address=" + address + ", serviceName=" + serviceName + ", isVIPService=" + isVIPService + ", isSupportDegradeService="
				+ isSupportDegradeService + ", degradeServicePath=" + degradeServicePath + ", degradeServiceDesc=" + degradeServiceDesc + ", weight=" + weight
				+ ", connCount=" + connCount + ", isReviewed=" + isReviewed + ", hasDegradeService=" + hasDegradeService + "]";
	}

	public static RegisterMeta createRegiserMeta(PublishServiceCustomBody publishServiceCustomBody) {

		Address address = new Address(publishServiceCustomBody.getHost(), 
									  publishServiceCustomBody.getPort());
		
		RegisterMeta registerMeta = new RegisterMeta(address,publishServiceCustomBody.getServiceProviderName(),
				publishServiceCustomBody.isVIPService(),
				publishServiceCustomBody.isSupportDegradeService(),
				publishServiceCustomBody.getDegradeServicePath(),
				publishServiceCustomBody.getDegradeServiceDesc(),
				publishServiceCustomBody.getWeight(),
				publishServiceCustomBody.getConnCount()
				);
		return registerMeta;
	}

}
