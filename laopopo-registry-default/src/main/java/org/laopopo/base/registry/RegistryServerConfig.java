package org.laopopo.base.registry;

import java.io.File;

import org.laopopo.common.loadbalance.LoadBalanceStrategy;
import org.laopopo.common.rpc.ServiceReviewState;

/**
 * 
 * @author BazingaLyn
 * @description 注册中心的一些基本配置文件
 * @time 2016年9月8日
 * @modifytime
 */
public class RegistryServerConfig {
	
	//持久化保存的位置
	private String storePathRootDir = System.getProperty("user.home") + File.separator + "test" + File.separator + "serviceInfo.json";
	//每个多久时间刷盘到硬盘上，默认30s
	private int persistTime = 30;
	//默认的负载均衡策略
	private LoadBalanceStrategy defaultLoadBalanceStrategy = LoadBalanceStrategy.WEIGHTINGRANDOM;
	//默认的审核状态，默认状态是未审核，测试的时候可以修改成审核通过
	private ServiceReviewState defaultReviewState = ServiceReviewState.HAS_NOT_REVIEWED;

	public String getStorePathRootDir() {
		return storePathRootDir;
	}

	public void setStorePathRootDir(String storePathRootDir) {
		this.storePathRootDir = storePathRootDir;
	}

	public int getPersistTime() {
		return persistTime;
	}

	public void setPersistTime(int persistTime) {
		this.persistTime = persistTime;
	}

	public LoadBalanceStrategy getDefaultLoadBalanceStrategy() {
		return defaultLoadBalanceStrategy;
	}

	public void setDefaultLoadBalanceStrategy(LoadBalanceStrategy defaultLoadBalanceStrategy) {
		this.defaultLoadBalanceStrategy = defaultLoadBalanceStrategy;
	}

	public ServiceReviewState getDefaultReviewState() {
		return defaultReviewState;
	}

	public void setDefaultReviewState(ServiceReviewState defaultReviewState) {
		this.defaultReviewState = defaultReviewState;
	}
	

}
