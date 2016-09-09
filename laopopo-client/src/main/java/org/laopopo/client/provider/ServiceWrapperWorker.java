package org.laopopo.client.provider;

import java.util.List;

import org.laopopo.client.provider.interceptor.ProviderProxyHandler;
import org.laopopo.client.provider.model.ServiceWrapper;


/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public interface ServiceWrapperWorker {
	
	ServiceWrapperWorker provider(Object serviceProvider);
	
	ServiceWrapperWorker provider(ProviderProxyHandler proxyHandler,Object serviceProvider);
	
	List<ServiceWrapper> create();

}
