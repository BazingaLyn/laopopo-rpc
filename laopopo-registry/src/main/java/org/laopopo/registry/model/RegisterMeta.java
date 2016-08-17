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

	private ServiceMeta serviceMeta = new ServiceMeta();
	// 服务的权重
	private volatile int weight;
	// 建议连接数 hashCode()与equals()不把connCount计算在内
	private volatile int connCount;
	
	private ServiceReviewState isReviewed = ServiceReviewState.HAS_NOT_REVIEWED;
	
	private boolean hasDegradeService;
	
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public ServiceMeta getServiceMeta() {
		return serviceMeta;
	}

	public void setServiceMeta(ServiceMeta serviceMeta) {
		this.serviceMeta = serviceMeta;
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


	public RegisterMeta(Address address, ServiceMeta serviceMeta, int weight, int connCount) {
		this.address = address;
		this.serviceMeta = serviceMeta;
		this.weight = weight;
		this.connCount = connCount;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		RegisterMeta that = (RegisterMeta) obj;

		return !(address != null ? !address.equals(that.address) : that.address != null)
				&& !(serviceMeta != null ? !serviceMeta.equals(that.serviceMeta) : that.serviceMeta != null);

	}

	@Override
	public int hashCode() {
		int result = address != null ? address.hashCode() : 0;
		result = 31 * result + (serviceMeta != null ? serviceMeta.hashCode() : 0);
		return result;
	}

	public static RegisterMeta createRegiserMeta(PublishServiceCustomBody publishServiceCustomBody) {

		Address address = new Address(publishServiceCustomBody.getHost(), 
									  publishServiceCustomBody.getPort());
		ServiceMeta meta = new ServiceMeta(publishServiceCustomBody.getGroup(), //group
										   publishServiceCustomBody.getVersion(), 
										   publishServiceCustomBody.getServiceProviderName(),
										   publishServiceCustomBody.isVIPService(),
										   publishServiceCustomBody.isSupportDegradeService(), 
										   publishServiceCustomBody.getDegradeServicePath(),
										   publishServiceCustomBody.getDegradeServiceDesc());
		
		RegisterMeta registerMeta = new RegisterMeta(address,meta,publishServiceCustomBody.getWeight(),publishServiceCustomBody.getConnCount());
		return registerMeta;
	}

}
