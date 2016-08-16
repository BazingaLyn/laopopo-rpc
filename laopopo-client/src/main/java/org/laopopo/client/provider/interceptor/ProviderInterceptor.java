package org.laopopo.client.provider.interceptor;


public interface ProviderInterceptor {
	
    void beforeInvoke(String methodName, Object[] args);

    void afterInvoke(String methodName, Object[] args, Object result);

}
