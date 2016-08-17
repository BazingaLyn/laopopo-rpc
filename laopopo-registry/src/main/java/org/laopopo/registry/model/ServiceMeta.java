package org.laopopo.registry.model;


public class ServiceMeta {
	

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
