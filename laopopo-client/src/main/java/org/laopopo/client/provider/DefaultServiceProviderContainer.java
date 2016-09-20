package org.laopopo.client.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
	
	/**
	 * 
	 * @author BazingaLyn
	 * @description 当前实例的服务状态
	 * @time 2016年8月29日
	 * @modifytime
	 */
	public static class CurrentServiceState {
		
		
		private AtomicBoolean hasDegrade = new AtomicBoolean(false);   //是否已经降级
		private AtomicBoolean hasLimitStream =new AtomicBoolean(true); //是否已经限流
		private AtomicBoolean isAutoDegrade =new AtomicBoolean(false); //是否已经开始自动降级
		private Integer failedPercent = 90;                            //调用成功率低于多少开始自动降级

		public AtomicBoolean getHasDegrade() {
			return hasDegrade;
		}

		public void setHasDegrade(AtomicBoolean hasDegrade) {
			this.hasDegrade = hasDegrade;
		}

		public AtomicBoolean getHasLimitStream() {
			return hasLimitStream;
		}

		public void setHasLimitStream(AtomicBoolean hasLimitStream) {
			this.hasLimitStream = hasLimitStream;
		}

		public AtomicBoolean getIsAutoDegrade() {
			return isAutoDegrade;
		}

		public void setIsAutoDegrade(AtomicBoolean isAutoDegrade) {
			this.isAutoDegrade = isAutoDegrade;
		}

		public Integer getFailedPercent() {
			return failedPercent;
		}

		public void setFailedPercent(Integer failedPercent) {
			this.failedPercent = failedPercent;
		}
	}

}
