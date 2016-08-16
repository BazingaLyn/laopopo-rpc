package org.laopopo.client.provider;

import org.laopopo.client.provider.model.ServiceWrapper;

public interface ServiceProviderContainer {
	
	/**
	 * 将服务放置在服务容器中，用来进行统一的管理
	 * @param uniqueKey 该服务的名称
	 * @param serviceWrapper 该服务的包装编织类
	 */
	void registerService(String uniqueKey, ServiceWrapper serviceWrapper);

	/**
	 * 根据服务的名称来获取对应的服务编织类
	 * @param uniqueKey 服务名
	 * @return 服务编织类
	 */
    ServiceWrapper lookupService(String uniqueKey);

}
