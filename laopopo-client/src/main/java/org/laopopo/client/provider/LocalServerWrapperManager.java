package org.laopopo.client.provider;

import static net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION;
import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static org.laopopo.common.utils.Reflects.getValue;
import static org.laopopo.common.utils.Reflects.newInstance;
import static org.laopopo.common.utils.Reflects.setValue;
import io.netty.util.internal.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.bytebuddy.ByteBuddy;

import org.laopopo.client.annotation.RPCService;
import org.laopopo.client.provider.flow.control.ServiceFlowControllerManager;
import org.laopopo.client.provider.interceptor.ProviderProxyHandler;
import org.laopopo.client.provider.model.ServiceWrapper;
import org.laopopo.common.exception.rpc.RpcWrapperException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 服务提供者端本地服务的编织管理类 将某个方法的某个方法编织成信息发送给registry
 * @time 2016年8月16日
 * @modifytime
 */
public class LocalServerWrapperManager {

	private static final Logger logger = LoggerFactory.getLogger(LocalServerWrapperManager.class);
	
	private ProviderRegistryController providerController;
	

	public LocalServerWrapperManager(ProviderRegistryController providerRegistryController) {
		this.providerController = providerRegistryController;
	}

	/**
	 * 
	 * @param listeningAddress 该服务暴露的网络地址 例如172.30.53.58::8989
	 * @param controller 全局限流工具
	 * @param obj 暴露的方法的实例
	 * @return
	 */
	public List<RemotingTransporter> wrapperRegisterInfo(int port, Object... obj) {

		List<RemotingTransporter> remotingTransporters = new ArrayList<RemotingTransporter>();
		
		//基本判断，如果暴露的方法是null或者是0，则说明无需编织服务
		if (null != obj && obj.length > 0) {
			
			for (Object o : obj) {
				
				//默认的编织对象
				DefaultServiceWrapper defaultServiceWrapper = new DefaultServiceWrapper();
				
				List<ServiceWrapper> serviceWrappers = defaultServiceWrapper.provider(o).create();
				
				if(null != serviceWrappers  && !serviceWrappers.isEmpty()){
					for(ServiceWrapper serviceWrapper : serviceWrappers){
						
						PublishServiceCustomBody commonCustomHeader = new PublishServiceCustomBody();
						commonCustomHeader.setConnCount(serviceWrapper.getConnCount());
						commonCustomHeader.setDegradeServiceDesc(serviceWrapper.getDegradeServiceDesc());
						commonCustomHeader.setDegradeServicePath(serviceWrapper.getDegradeServicePath());
						commonCustomHeader.setPort(port);
						commonCustomHeader.setServiceProviderName(serviceWrapper.getServiceName());
						commonCustomHeader.setVIPService(serviceWrapper.isVIPService());
						commonCustomHeader.setWeight(serviceWrapper.getWeight());
						commonCustomHeader.setSupportDegradeService(serviceWrapper.isSupportDegradeService());
						
						RemotingTransporter remotingTransporter =  RemotingTransporter.createRequestTransporter(LaopopoProtocol.PUBLISH_SERVICE, commonCustomHeader);
						remotingTransporters.add(remotingTransporter);
					}
				}
			}
		}
		return remotingTransporters;
		
	}

	/**
	 * 
	 * @author BazingaLyn
	 * @description 方法编织服务
	 * @time 2016年8月16日
	 * @modifytime
	 */
	class DefaultServiceWrapper implements ServiceWrapperWorker {
		
		//全局拦截proxy
		private volatile ProviderProxyHandler globalProviderProxyHandler;

		//某个方法实例编织后的对象
		private Object serviceProvider;
		//该方法降级时所对应的mock对象实例(最好是两个同样的接口)
		private Object mockDegradeServiceProvider;

		@Override
		public ServiceWrapperWorker provider(Object serviceProvider) {
			//如果proxy的对象是null,实例对象无需编织，直接返回
			if(null  == globalProviderProxyHandler){
				this.serviceProvider = serviceProvider;
			}else{
				Class<?> globalProxyCls = generateProviderProxyClass(globalProviderProxyHandler, serviceProvider.getClass());
	            this.serviceProvider = copyProviderProperties(serviceProvider, newInstance(globalProxyCls));
			}
			return this;
		}

		@Override
		public ServiceWrapperWorker provider(ProviderProxyHandler proxyHandler, Object serviceProvider) {
			Class<?> proxyCls = generateProviderProxyClass(proxyHandler, serviceProvider.getClass());
	        if (globalProviderProxyHandler == null) {
	            this.serviceProvider = copyProviderProperties(serviceProvider, newInstance(proxyCls));
	        } else {
	            Class<?> globalProxyCls = generateProviderProxyClass(globalProviderProxyHandler, proxyCls);
	            this.serviceProvider = copyProviderProperties(serviceProvider, newInstance(globalProxyCls));
	        }
	        return this;
		}

		@Override
		public List<ServiceWrapper> create() {
			
			List<ServiceWrapper> serviceWrappers = new ArrayList<ServiceWrapper>();
			
			//读取对象的方法注解
			RPCService rpcService = null;
			
			for (Class<?> cls = serviceProvider.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
				Method[] methods = cls.getMethods();
				if(null != methods && methods.length > 0){
					
					for(Method method :methods){
						rpcService = method.getAnnotation(RPCService.class);
						if(null != rpcService){
							
							//服务名
							String serviceName = StringUtil.isNullOrEmpty(rpcService.serviceName())?method.getName():rpcService.serviceName();
							//负责人
							String responsiblityName = rpcService.responsibilityName();
							//方法weight
							Integer weight = rpcService.weight();
							//连接数 默认是1 一个实例一个1链接其实是够用的
							Integer connCount = rpcService.connCount();
							//是否支持服务降级
							boolean isSupportDegradeService = rpcService.isSupportDegradeService();
							//是否是VIP服务，如果是VIP服务，则默认是在port-2的端口暴露方法，与其他的方法使用不同的
							boolean isVIPService = rpcService.isVIPService();
							//暴露的降级方法的路径
							String degradeServicePath = rpcService.degradeServicePath();
							//降级方法的描述
							String degradeServiceDesc = rpcService.degradeServiceDesc();
							//每分钟调用的最大调用次数
							Long maxCallCount = rpcService.maxCallCountInMinute();
							if(maxCallCount <= 0){
								throw new RpcWrapperException("max call count must over zero at unit time");
							}
							ServiceFlowControllerManager serviceFlowControllerManager = providerController.getServiceFlowControllerManager();
							serviceFlowControllerManager.setServiceLimitVal(serviceName, maxCallCount);
							//如果是支持服务降级服务，则需要根据降级方法的路径去创建这个实例，并编制proxy
							if(isSupportDegradeService){
								Class<?> degradeClass = null;
								try {
									degradeClass = Class.forName(degradeServicePath);
									Object nativeObj = degradeClass.newInstance();
									if(null  == globalProviderProxyHandler){
										this.mockDegradeServiceProvider = nativeObj;
									}else{
										Class<?> globalProxyCls = generateProviderProxyClass(globalProviderProxyHandler, nativeObj.getClass());
							            this.mockDegradeServiceProvider = copyProviderProperties(nativeObj, newInstance(globalProxyCls));
									}
								} catch (Exception e) {
									logger.error("[{}] class can not create by reflect [{}]",degradeServicePath,e.getMessage());
									throw new RpcWrapperException("degradeService path " + degradeServicePath +"create failed" ); 
								} 
								
							}
							
							String methodName = method.getName();
							Class<?>[] classes = method.getParameterTypes();
							List<Class<?>[]> paramters = new ArrayList<Class<?>[]>();
							paramters.add(classes);
							
							ServiceWrapper serviceWrapper = new ServiceWrapper(serviceProvider,
																			   mockDegradeServiceProvider,
																			   serviceName,
																			   responsiblityName,
																			   methodName,
																			   paramters,
																			   isSupportDegradeService,
																			   degradeServicePath,
																			   degradeServiceDesc,
																			   weight,
																			   connCount,
																			   isVIPService);
							//放入到一个缓存中，方便以后consumer来调取服务的时候，该来获取对应真正的编织类
							providerController.getProviderContainer().registerService(serviceName, serviceWrapper);
							
							serviceWrappers.add(serviceWrapper);
						}
					}
				}
			}
			return serviceWrappers;
		}
		
		private <T> Class<? extends T> generateProviderProxyClass(ProviderProxyHandler proxyHandler, Class<T> providerCls) {
			
			try {
				return new ByteBuddy()
				.subclass(providerCls)
				.method(isDeclaredBy(providerCls))
				.intercept(to(proxyHandler, "handler").filter(not(isDeclaredBy(Object.class))))
				.make()
				.load(providerCls.getClassLoader(), INJECTION)
	            .getLoaded();
			} catch (Exception e) {
				logger.error("Generate proxy [{}, handler: {}] fail: {}.", providerCls, proxyHandler,e.getMessage());
	            return providerCls;
			}
		}
		
		private <F, T> T copyProviderProperties(F provider, T proxy) {
			List<String> providerFieldNames = new ArrayList<String>();
			
			for (Class<?> cls = provider.getClass(); cls != null; cls = cls.getSuperclass()) {
	            try {
	                for (Field f : cls.getDeclaredFields()) {
	                    providerFieldNames.add(f.getName());
	                }
	            } catch (Throwable ignored) {}
	        }

	        for (String name : providerFieldNames) {
	            try {
	                setValue(proxy, name, getValue(provider, name));
	            } catch (Throwable ignored) {}
	        }
	        return proxy;
		}
		
		

	}

}
