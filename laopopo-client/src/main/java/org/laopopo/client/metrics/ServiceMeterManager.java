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
	 * 计算某个服务的调用成功率，四舍五入到个位数
	 * @param serviceName
	 * @return
	 */
	public static Integer calcServiceSuccessRate(String serviceName){
		
		Meter meter = globalMeterManager.get(serviceName);
		
		if(meter == null){
			return 0;
		}
		
		int callCount = meter.getCallCount().intValue();
		int failCount = meter.getFailedCount().intValue();
		
		//如果调用的此时是0.默认成功率是100%
		if(callCount == 0){
			return 100;
		}
		
		return (100 *(callCount - failCount ) / callCount);
		
	}
	
	
	/**
	 * 增加一次调用次数
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
	 * 增加一次调用失败次数
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
	 * 累加某个服务的调用时间
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
	
	/**
	 * 累加某个服务的请求入参的大小
	 * @param serviceName
	 * @param byteSize
	 */
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
