package org.laopopo.base.registry.model;

import java.util.ArrayList;
import java.util.List;

import org.laopopo.common.loadbalance.LoadBalanceStrategy;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.common.rpc.ServiceReviewState;

/**
 * 
 * @author BazingaLyn
 * @description 注册中心的记录的数据去持久化的数据
 * @time 2016年9月8日
 * @modifytime
 */
public class RegistryPersistRecord {
	
	private String serviceName;
	
	private LoadBalanceStrategy balanceStrategy;
	
	private List<PersistProviderInfo> providerInfos = new ArrayList<PersistProviderInfo>();
	
	public RegistryPersistRecord() {
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public LoadBalanceStrategy getBalanceStrategy() {
		return balanceStrategy;
	}

	public void setBalanceStrategy(LoadBalanceStrategy balanceStrategy) {
		this.balanceStrategy = balanceStrategy;
	}

	public List<PersistProviderInfo> getProviderInfos() {
		return providerInfos;
	}

	public void setProviderInfos(List<PersistProviderInfo> providerInfos) {
		this.providerInfos = providerInfos;
	}


	public static class PersistProviderInfo {
		
		private Address address;
		
		private ServiceReviewState isReviewed = ServiceReviewState.PASS_REVIEW;
		
		public PersistProviderInfo() {
		}
		
		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public ServiceReviewState getIsReviewed() {
			return isReviewed;
		}

		public void setIsReviewed(ServiceReviewState isReviewed) {
			this.isReviewed = isReviewed;
		}

		
	}

}
