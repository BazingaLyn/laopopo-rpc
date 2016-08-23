package org.laopopo.client.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.client.provider.model.ServiceWrapper;
import org.laopopo.common.utils.Pair;

/**
 * 
 * @author BazingaLyn
 * @description 服务容器
 * @time 2016年8月23日
 * @modifytime
 */
public class DefaultServiceProviderContainer implements ServiceProviderContainer {
	
	
	private final ConcurrentMap<String, Pair<CurrentServiceState, ServiceWrapper>> serviceProviders = new ConcurrentHashMap<String, Pair<CurrentServiceState, ServiceWrapper>>();

	public void registerService(String uniqueKey, ServiceWrapper serviceWrapper) {
		
		Pair<CurrentServiceState, ServiceWrapper> pair = new Pair<DefaultServiceProviderContainer.CurrentServiceState, ServiceWrapper>();
		pair.setKey(new CurrentServiceState());
		pair.setValue(serviceWrapper);
		serviceProviders.put(uniqueKey, pair);
		
	}

	public Pair<CurrentServiceState, ServiceWrapper> lookupService(String uniqueKey) {
		return serviceProviders.get(uniqueKey);
	}
	
	public static class CurrentServiceState {
		
		private boolean hasDegrade = false;
		
		private boolean hasLimitStream = true;
		
		private boolean isVipService = false;

		public boolean isHasDegrade() {
			return hasDegrade;
		}

		public void setHasDegrade(boolean hasDegrade) {
			this.hasDegrade = hasDegrade;
		}

		public boolean isHasLimitStream() {
			return hasLimitStream;
		}

		public void setHasLimitStream(boolean hasLimitStream) {
			this.hasLimitStream = hasLimitStream;
		}

		public boolean isVipService() {
			return isVipService;
		}

		public void setVipService(boolean isVipService) {
			this.isVipService = isVipService;
		}
		
	}

}
