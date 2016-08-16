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
import org.laopopo.client.provider.flow.control.FlowController;
import org.laopopo.client.provider.interceptor.ProviderProxyHandler;
import org.laopopo.client.provider.model.ServiceWrapper;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.PublishServiceCustomBody;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class LocalServerWrapperManager {

	private static final Logger logger = LoggerFactory.getLogger(LocalServerWrapperManager.class);
	
	private ProviderController providerController;

	public LocalServerWrapperManager(ProviderController providerController) {
		this.providerController = providerController;
	}

	public List<RemotingTransporter> wrapperRegisterInfo(String listeningAddress, FlowController controller, Object... obj) {

		List<RemotingTransporter> remotingTransporters = new ArrayList<RemotingTransporter>();
		
		if(null != obj && obj.length > 0){
			
			for(Object o:obj){
				
				DefaultServiceWrapper defaultServiceWrapper = new DefaultServiceWrapper();
				List<ServiceWrapper> serviceWrappers = defaultServiceWrapper.provider(o).flowController(controller).create();
				
				if(null != serviceWrappers  && !serviceWrappers.isEmpty()){
					for(ServiceWrapper serviceWrapper : serviceWrappers){
						
						String[] address = listeningAddress.split(":");
						String host = address[0];
						int port = Integer.parseInt(address[1]);
						PublishServiceCustomBody commonCustomHeader = new PublishServiceCustomBody();
						commonCustomHeader.setConnCount(serviceWrapper.getConnCount());
						commonCustomHeader.setDegradeServiceDesc(serviceWrapper.getDegradeServiceDesc());
						commonCustomHeader.setDegradeServicePath(serviceWrapper.getDegradeServicePath());
						commonCustomHeader.setGroup(serviceWrapper.getGroup());
						commonCustomHeader.setVersion(serviceWrapper.getVersion());
						commonCustomHeader.setHost(host);
						commonCustomHeader.setPort(port);
						commonCustomHeader.setServiceProviderName(serviceWrapper.getServiceName());
						commonCustomHeader.setVIPService(serviceWrapper.isVIPService());
						commonCustomHeader.setWeight(serviceWrapper.getWeight());
						commonCustomHeader.setSupportDegradeService(serviceWrapper.isSupportDegradeService());
						
						RemotingTransporter remotingTransporter =  RemotingTransporter.createRequestTransporter(LaopopoProtocol.PUBLISH_SERVICE, commonCustomHeader, LaopopoProtocol.REQUEST_REMOTING);
						remotingTransporters.add(remotingTransporter);
					}
				}
			}
		}
		return remotingTransporters;
		
	}

	class DefaultServiceWrapper implements ServiceWrapperWorker {
		
		//全局拦截proxy
		private volatile ProviderProxyHandler globalProviderProxyHandler;

		private Object serviceProvider;
		private Object mockDegradeServiceProvider;
		protected FlowController flowController;
		
		@Override
		public ServiceWrapperWorker flowController(FlowController flowController){
			this.flowController = flowController;
			return this;
		}

		@Override
		public ServiceWrapperWorker provider(Object serviceProvider) {
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
			
			RPCService rpcService = null;
			for (Class<?> cls = serviceProvider.getClass(); cls != Object.class; cls = cls.getSuperclass()) {
				Method[] methods = cls.getMethods();
				if(null != methods && methods.length > 0){
					
					for(Method method :methods){
						rpcService = method.getAnnotation(RPCService.class);
						if(null != rpcService){
							
							String serviceName = StringUtil.isNullOrEmpty(rpcService.serviceName())?method.getName():rpcService.serviceName();
							String version = rpcService.version();
							String group = rpcService.group();
							String responsiblityName = rpcService.responsibilityName();
							Integer weight = rpcService.weight();
							Integer connCount = rpcService.connCount();
							boolean isSupportDegradeService = rpcService.isSupportDegradeService();
							boolean isVIPService = rpcService.isVIPService();
							String degradeServicePath = rpcService.degradeServicePath();
							String degradeServiceDesc = rpcService.degradeServiceDesc();
							if(isSupportDegradeService){
								Class<?> degradeClass = null;
								try {
									degradeClass = Class.forName(degradeServicePath);
									mockDegradeServiceProvider = degradeClass.newInstance();
								} catch (Exception e) {
									logger.error("[{}] class can not create by reflect [{}]",degradeServicePath,e.getMessage());
								} 
								
							}
							
							String methodName = method.getName();
							Class<?>[] classes = method.getParameterTypes();
							List<Class<?>[]> paramters = new ArrayList<Class<?>[]>();
							paramters.add(classes);
							
							ServiceWrapper serviceWrapper = new ServiceWrapper(serviceProvider,
																			   mockDegradeServiceProvider,
																			   version,
																			   group,
																			   serviceName,
																			   responsiblityName,
																			   methodName,
																			   paramters,
																			   isSupportDegradeService,
																			   degradeServicePath,
																			   degradeServiceDesc,
																			   weight,
																			   connCount,
																			   isVIPService,
																			   flowController);
									
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
