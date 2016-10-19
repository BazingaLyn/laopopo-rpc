package org.laopopo.client.provider.interceptor;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public interface ProviderInterceptor {
	
    void beforeInvoke(String methodName, Object[] args);

    void afterInvoke(String methodName, Object[] args, Object result);

}
