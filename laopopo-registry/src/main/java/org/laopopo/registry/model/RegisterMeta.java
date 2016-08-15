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

	public static class ServiceMeta {
		// 组别
		private String group;
		// 版本信息
		private String version;
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

		public ServiceMeta() {
		}

		public ServiceMeta(String group, String version, String serviceProviderName, boolean isVIPService, boolean isSupportDegradeService,
				String degradeServicePath, String degradeServiceDesc) {
			this.group = group;
			this.version = version;
			this.serviceProviderName = serviceProviderName;
			this.isVIPService = isVIPService;
			this.isSupportDegradeService = isSupportDegradeService;
			this.degradeServicePath = degradeServicePath;
			this.degradeServiceDesc = degradeServiceDesc;
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

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			ServiceMeta that = (ServiceMeta) o;

			return !(group != null ? !group.equals(that.group) : that.group != null)
					&& !(version != null ? !version.equals(that.version) : that.version != null)
					&& !(serviceProviderName != null ? !serviceProviderName.equals(that.serviceProviderName) : that.serviceProviderName != null)
					&& isVIPService == that.isVIPService && isSupportDegradeService == that.isSupportDegradeService
					&& !(degradeServicePath != null ? !degradeServicePath.equals(that.degradeServicePath) : that.degradeServicePath != null)
					&& !(degradeServiceDesc != null ? !degradeServiceDesc.equals(that.degradeServiceDesc) : that.degradeServiceDesc != null);
		}

		@Override
		public int hashCode() {
			int result = group != null ? group.hashCode() : 0;
			result = 31 * result + (version != null ? version.hashCode() : 0);
			result = 31 * result + (serviceProviderName != null ? serviceProviderName.hashCode() : 0);
			result = 31 * result + (isVIPService == true ? 1 : 0);
			result = 31 * result + (isSupportDegradeService == true ? 1 : 0);
			result = 31 * result + (degradeServicePath != null ? degradeServicePath.hashCode() : 0);
			result = 31 * result + (degradeServiceDesc != null ? degradeServiceDesc.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "ServiceMeta [group=" + group + ", version=" + version + ", serviceProviderName=" + serviceProviderName + ", isVIPService=" + isVIPService
					+ ", isSupportDegradeService=" + isSupportDegradeService + ", degradeServicePath=" + degradeServicePath + ", degradeServiceDesc="
					+ degradeServiceDesc + "]";
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
