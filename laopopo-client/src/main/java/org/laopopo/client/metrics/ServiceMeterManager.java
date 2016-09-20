package org.laopopo.client.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 
 * @author BazingaLyn
 * @description 服务统计管理
 * @time 2016年9月18日
 * @modifytime
 */
public class ServiceMeterManager {
	
	//key是serviceName
	private static ConcurrentMap<String, Meter> globalMeterManager = new ConcurrentHashMap<String, Meter>();
	
	/**
	 * 
	 * @param serviceName
	 */
	public static void incrementCallTimes(String serviceName){
		
		Meter meter = globalMeterManager.get(serviceName);
		
		if(meter == null){
			meter = new Meter(serviceName);
			globalMeterManager.put(serviceName, meter);
		}
		meter.getCallCount().incrementAndGet();
		
	}
	
	/**
	 * 
	 * @param serviceName
	 */
	public static void incrementFailTimes(String serviceName){
		
		Meter meter = globalMeterManager.get(serviceName);
		
		if(meter == null){
			meter = new Meter(serviceName);
			globalMeterManager.put(serviceName, meter);
		}
		meter.getFailedCount().incrementAndGet();
	}
	
	/**
	 * 
	 * @param serviceName
	 * @param byteSize
	 */
	public static void incrementTotalTime(String serviceName,Long timecost){
		
		Meter meter = globalMeterManager.get(serviceName);
		
		if(meter == null){
			meter = new Meter(serviceName);
			globalMeterManager.put(serviceName, meter);
		}
		meter.getTotalCallTime().addAndGet(timecost);
	}
	
	
	public static void incrementRequestSize(String serviceName,int byteSize){
		
		Meter meter = globalMeterManager.get(serviceName);
		
		if(meter == null){
			meter = new Meter(serviceName);
			globalMeterManager.put(serviceName, meter);
		}
		meter.getTotalRequestSize().addAndGet(byteSize);
	}


	public static void scheduledSendReport() {
		
	}

	public static ConcurrentMap<String, Meter> getGlobalMeterManager() {
		return globalMeterManager;
	}


}
