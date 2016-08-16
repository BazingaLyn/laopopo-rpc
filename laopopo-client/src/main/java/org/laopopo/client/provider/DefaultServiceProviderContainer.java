package org.laopopo.client.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.client.provider.model.ServiceWrapper;

/**
 * 提供者的服务注册容器
 * 每一个服务都有唯一的key来代表这个服务
 * @author BazingaLyn
 * @copyright fjc
 * @time 2016年6月27日
 */
public class DefaultServiceProviderContainer implements ServiceProviderContainer {
	
	
	private final ConcurrentMap<String, ServiceWrapper> serviceProviders = new ConcurrentHashMap<String, ServiceWrapper>();

	public void registerService(String uniqueKey, ServiceWrapper serviceWrapper) {
		
		serviceProviders.put(uniqueKey, serviceWrapper);
		
	}

	public ServiceWrapper lookupService(String uniqueKey) {
		return serviceProviders.get(uniqueKey);
	}

}
