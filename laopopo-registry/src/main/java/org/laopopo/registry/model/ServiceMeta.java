package org.laopopo.registry.model;


public class ServiceMeta {
	

	// 组别
	private String group;
	// 版本信息
	private String version;
	// 服务名
	private String serviceProviderName;

	public ServiceMeta() {
	}

	public ServiceMeta(String group, String version, String serviceProviderName) {
		this.group = group;
		this.version = version;
		this.serviceProviderName = serviceProviderName;
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
				&& !(serviceProviderName != null ? !serviceProviderName.equals(that.serviceProviderName) : that.serviceProviderName != null);
	}

	@Override
	public int hashCode() {
		int result = group != null ? group.hashCode() : 0;
		result = 31 * result + (version != null ? version.hashCode() : 0);
		result = 31 * result + (serviceProviderName != null ? serviceProviderName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ServiceMeta [group=" + group + ", version=" + version + ", serviceProviderName=" + serviceProviderName + "]";
	}


}
