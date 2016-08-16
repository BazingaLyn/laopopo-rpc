package org.laopopo.client.provider.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class ProviderProxyHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(ProviderProxyHandler.class);
	
	private final CopyOnWriteArrayList<ProviderInterceptor> interceptors = new CopyOnWriteArrayList<ProviderInterceptor>();
	
	@RuntimeType
    public Object invoke(@SuperCall Callable<Object> superMethod,
            @Origin Method method,
            @AllArguments @RuntimeType Object[] args) throws Exception{
		
		String methodName = method.getName();
		
		for (int i = interceptors.size() - 1; i >= 0; i--) {
            ProviderInterceptor interceptor = interceptors.get(i);
            try {
                interceptor.beforeInvoke(methodName, args);
            } catch (Throwable t) {
                logger.warn("Interceptor[{}#beforeInvoke]: {}.", interceptor.getClass().getName());
            }
        }
		Object result = null;
		try {
            result = superMethod.call();
        } finally {
            for (int i = 0; i < interceptors.size(); i++) {
                ProviderInterceptor interceptor = interceptors.get(i);
                try {
                    interceptor.afterInvoke(methodName, args, result);
                } catch (Throwable t) {
                    logger.warn("Interceptor[{}#afterInvoke]: {}.", interceptor.getClass().getName());
                }
            }
        }
        return result;
     }
	
	public ProviderProxyHandler withIntercept(ProviderInterceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    public ProviderProxyHandler withIntercept(ProviderInterceptor... interceptors) {
        for (ProviderInterceptor interceptor : interceptors) {
            withIntercept(interceptor);
        }
        return this;
    }
	
	
}
