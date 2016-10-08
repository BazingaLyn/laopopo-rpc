package org.laopopo.client.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author BazingaLyn
 * @description RPC调用统计
 * @time 2016年9月18日
 * @modifytime
 */
public class Meter {
	
	private String serviceName;								 //服务名
	private AtomicLong callCount = new AtomicLong(0l);       //调用次数
	private AtomicLong failedCount = new AtomicLong(0l);	 //失败次数
	private AtomicLong totalCallTime = new AtomicLong(0l);   //总的调用时间
	private AtomicLong totalRequestSize = new AtomicLong(0l);//入参大小
	
	public Meter(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public AtomicLong getCallCount() {
		return callCount;
	}

	public void setCallCount(AtomicLong callCount) {
		this.callCount = callCount;
	}

	public AtomicLong getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(AtomicLong failedCount) {
		this.failedCount = failedCount;
	}

	public AtomicLong getTotalCallTime() {
		return totalCallTime;
	}

	public void setTotalCallTime(AtomicLong totalCallTime) {
		this.totalCallTime = totalCallTime;
	}

	public AtomicLong getTotalRequestSize() {
		return totalRequestSize;
	}

	public void setTotalRequestSize(AtomicLong totalRequestSize) {
		this.totalRequestSize = totalRequestSize;
	}
	
}
