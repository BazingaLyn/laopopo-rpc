package org.laopopo.common.metrics;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.loadbalance.LoadBalanceStrategy;
import org.laopopo.common.rpc.RegisterMeta.Address;
import org.laopopo.common.rpc.ServiceReviewState;

/**
 * 
 * @author BazingaLyn
 * @description 服务统计信息，用于管理人员管理服务数据
 * @time 2016年8月29日
 * @modifytime 2016年8月31日
 */
public class ServiceMetrics {
	
	private String serviceName;                      							  	  //服务名
	private Long totalCallCount = 0l;                     							  //该服务的总共的统计次数
	private Long totalFailCount = 0l;                    							  //该服务的总共的失败次数
	private Double handlerAvgTime = 0d;
	private ConcurrentMap<Address,ProviderInfo> providerMaps = 
			new ConcurrentHashMap<Address, ProviderInfo>();     	                  //该服务的消费者的信息
	private Set<ConsumerInfo> consumerInfos = new HashSet<ConsumerInfo>();        	  //该服务的提供者的信息
	private LoadBalanceStrategy loadBalanceStrategy; 							  	  //负载均衡策略
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Long getTotalCallCount() {
		return totalCallCount;
	}

	public void setTotalCallCount(Long totalCallCount) {
		this.totalCallCount = totalCallCount;
	}

	public Long getTotalFailCount() {
		return totalFailCount;
	}

	public void setTotalFailCount(Long totalFailCount) {
		this.totalFailCount = totalFailCount;
	}

	public ConcurrentMap<Address, ProviderInfo> getProviderMaps() {
		return providerMaps;
	}

	public void setProviderMaps(ConcurrentMap<Address, ProviderInfo> providerMaps) {
		this.providerMaps = providerMaps;
	}

	public Set<ConsumerInfo> getConsumerInfos() {
		return consumerInfos;
	}

	public void setConsumerInfos(Set<ConsumerInfo> consumerInfos) {
		this.consumerInfos = consumerInfos;
	}

	public LoadBalanceStrategy getLoadBalanceStrategy() {
		return loadBalanceStrategy;
	}

	public void setLoadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy) {
		this.loadBalanceStrategy = loadBalanceStrategy;
	}
	
	public Double getHandlerAvgTime() {
		return handlerAvgTime;
	}

	public void setHandlerAvgTime(Double handlerAvgTime) {
		this.handlerAvgTime = handlerAvgTime;
	}

	public static class ConsumerInfo {
		
		private int port;
		
		private String host;

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		@Override
		public String toString() {
			return "ConsumerInfo [port=" + port + ", host=" + host + "]";
		}
		
		
	}
	
	//提供者的信息
	public static class ProviderInfo {
		
		private int port;                       //端口号
		private String host; 			        //host
		private Long callCount = 0l;            //调用的次数
		private Long failCount = 0l;            //失败的次数
		private Double handlerAvgTime = 0d;     //处理的平均时间
		private Boolean isDegradeService;       //是否已经降级
		private Boolean isSupportDegrade;       //是否支持降级
		private Boolean isVipService;           //是否是VIP服务
		private ServiceReviewState serviceReviewState;   //服务的审核状态
		
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public Long getCallCount() {
			return callCount;
		}
		public void setCallCount(Long callCount) {
			this.callCount = callCount;
		}
		public Long getFailCount() {
			return failCount;
		}
		public void setFailCount(Long failCount) {
			this.failCount = failCount;
		}
		public Double getHandlerAvgTime() {
			return handlerAvgTime;
		}
		public void setHandlerAvgTime(Double handlerAvgTime) {
			this.handlerAvgTime = handlerAvgTime;
		}
		public Boolean getIsDegradeService() {
			return isDegradeService;
		}
		public void setIsDegradeService(Boolean isDegradeService) {
			this.isDegradeService = isDegradeService;
		}
		public Boolean getIsSupportDegrade() {
			return isSupportDegrade;
		}
		public void setIsSupportDegrade(Boolean isSupportDegrade) {
			this.isSupportDegrade = isSupportDegrade;
		}
		public Boolean getIsVipService() {
			return isVipService;
		}
		public void setIsVipService(Boolean isVipService) {
			this.isVipService = isVipService;
		}
		public ServiceReviewState getServiceReviewState() {
			return serviceReviewState;
		}
		public void setServiceReviewState(ServiceReviewState serviceReviewState) {
			this.serviceReviewState = serviceReviewState;
		}
		@Override
		public String toString() {
			return "ProviderInfo [port=" + port + ", host=" + host + ", callCount=" + callCount + ", failCount=" + failCount + ", handlerAvgTime="
					+ handlerAvgTime + ", isDegradeService=" + isDegradeService + ", isSupportDegrade=" + isSupportDegrade + ", isVipService=" + isVipService
					+ ", serviceReviewState=" + serviceReviewState + "]";
		}
		
	}

	@Override
	public String toString() {
		return "ServiceMetrics [serviceName=" + serviceName + ", totalCallCount=" + totalCallCount + ", totalFailCount=" + totalFailCount + ", handlerAvgTime="
				+ handlerAvgTime + ", providerMaps=" + providerMaps + ", consumerInfos=" + consumerInfos + ", loadBalanceStrategy=" + loadBalanceStrategy + "]";
	}

}
