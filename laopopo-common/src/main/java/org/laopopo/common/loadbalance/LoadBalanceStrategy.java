package org.laopopo.common.loadbalance;

/**
 * 
 * @author BazingaLyn
 * @description 负载均衡的访问策略
 * @time 2016年8月31日
 * @modifytime
 */
public enum LoadBalanceStrategy {
	
	RANDOM, //随机
	WEIGHTINGRANDOM, //加权随机
	ROUNDROBIN, //轮询

}
