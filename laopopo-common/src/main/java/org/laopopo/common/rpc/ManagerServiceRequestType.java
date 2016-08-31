package org.laopopo.common.rpc;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public enum ManagerServiceRequestType {
	
	REVIEW,             //审核某个地址的服务  发送的信息含服务名和审核状态和提供该服务的地址就ok，只修改该地址的审核状态
	DEGRADE,            //降级或者还原某个地址的服务
	MODIFY_WEIGHT,      //修改某个地址某个服务的权重
	MODIFY_LOADBALANCE, //修改某个服务的访问策略
	METRICS;            //统计服务

}
