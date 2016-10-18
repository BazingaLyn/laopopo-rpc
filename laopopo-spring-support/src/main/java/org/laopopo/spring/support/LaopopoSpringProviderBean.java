package org.laopopo.spring.support;

import org.laopopo.client.provider.DefaultProvider;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author BazingaLyn
 * @description 服务提供者对spring的支持
 * @time 2016年10月17日
 * @modifytime
 */
public class LaopopoSpringProviderBean implements InitializingBean {
	
	
	private DefaultProvider defaultProvider;
	private String registryAddress;
	private String monitorAddress;
	private int listenerPort;
	private Object[] publishObjs;

	@Override
	public void afterPropertiesSet() throws Exception {
		
		if(registryAddress != null){
			defaultProvider.registryAddress(registryAddress);
		}
		
		if(monitorAddress != null){
			defaultProvider.monitorAddress(monitorAddress);
		}
		
		defaultProvider.serviceListenPort(listenerPort);
		
		defaultProvider.publishService(publishObjs);
		
		defaultProvider.start();
		
	}

	public String getRegistryAddress() {
		return registryAddress;
	}

	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public String getMonitorAddress() {
		return monitorAddress;
	}

	public void setMonitorAddress(String monitorAddress) {
		this.monitorAddress = monitorAddress;
	}

	public int getListenerPort() {
		return listenerPort;
	}

	public void setListenerPort(int listenerPort) {
		this.listenerPort = listenerPort;
	}

	public Object[] getPublishObjs() {
		return publishObjs;
	}

	public void setPublishObjs(Object[] publishObjs) {
		this.publishObjs = publishObjs;
	}

	public DefaultProvider getDefaultProvider() {
		return defaultProvider;
	}

	public void setDefaultProvider(DefaultProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}
	

}
