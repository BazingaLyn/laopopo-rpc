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
	
	private String serviceName;
	
	private AtomicLong callCount = new AtomicLong(0l);
	
	private AtomicLong failedCount = new AtomicLong(0l);
	
	private AtomicLong totalCallTime = new AtomicLong(0l);
	
	private AtomicLong totalRequestSize = new AtomicLong(0l);
	
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
	
//	public long getAvgHandlerTime(){
//		
//		long callTotalCount = callCount.get();
//		long failTotalCount = failedCount.get();
//		long totalHandlerTime = totalCallTime.get();
//		
//		return totalHandlerTime /(callTotalCount - failTotalCount);
//		
//	}

}
