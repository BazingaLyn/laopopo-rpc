package org.laopopo.client.provider.flow.control;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.laopopo.common.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author BazingaLyn
 * @description 限制每个服务单位时间(分钟)的调用次数
 * @time 2016年9月9日
 * @modifytime
 */
public class ServiceFlowControllerManager {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceFlowControllerManager.class);
	
	private static final ConcurrentMap<String, Pair<Long,ServiceFlowController>> globalFlowControllerMap = new ConcurrentHashMap<String, Pair<Long,ServiceFlowController>>();

	/**
	 * 设置某个服务的单位时间的最大调用次数
	 * @param serviceName
	 * @param maxCallCount
	 */
	public void setServiceLimitVal(String serviceName,Long maxCallCount){
		
		Pair<Long,ServiceFlowController> pair = new Pair<Long, ServiceFlowController>();
		pair.setKey(maxCallCount);
		pair.setValue(new ServiceFlowController());
		globalFlowControllerMap.put(serviceName, pair);
		
	}
	
	/**
	 * 原子增加某个服务的调用次数
	 * @param serviceName
	 */
	public void incrementCallCount(String serviceName){
		
		Pair<Long,ServiceFlowController> pair = globalFlowControllerMap.get(serviceName);
		
		if(null == pair){
			logger.warn("serviceName [{}] matched no flowController",serviceName);
			return;
		}
		
		ServiceFlowController serviceFlowController = pair.getValue();
		serviceFlowController.incrementAtCurrentMinute();
		
	}
	
	/**
	 * 查看某个服务是否可用
	 * @param serviceName
	 * @return
	 */
	public boolean isAllow(String serviceName){
		
		Pair<Long,ServiceFlowController> pair = globalFlowControllerMap.get(serviceName);
		
		if(null == pair){
			logger.warn("serviceName [{}] matched no flowController",serviceName);
			return false;
		}
		
		ServiceFlowController serviceFlowController = pair.getValue();
		Long maxCallCount = pair.getKey();
		long hasCallCount = serviceFlowController.incrementAtCurrentMinute();
		
		serviceFlowController.incrementAtCurrentMinute();
		return hasCallCount > maxCallCount ? false :true;
		
	}
	
	/**
	 * 获取到某个服务的上一分钟的调用次数
	 * @param serviceName
	 * @return
	 */
	public Long getLastMinuteCallCount(String serviceName){
		Pair<Long,ServiceFlowController> pair = globalFlowControllerMap.get(serviceName);
		
		if(null == pair){
			logger.warn("serviceName [{}] matched no flowController",serviceName);
			return 0l;
		}
		ServiceFlowController serviceFlowController = pair.getValue();
		return serviceFlowController.getLastCallCountAtLastMinute();
	}
	
	/**
	 * 将下一秒的调用次数置为0
	 */
	public void clearAllServiceNextMinuteCallCount(){
		
		for(String service : globalFlowControllerMap.keySet()){
			
			Pair<Long,ServiceFlowController> pair = globalFlowControllerMap.get(service);
			if(null == pair){
				logger.warn("serviceName [{}] matched no flowController",service);
				continue;
			}
			ServiceFlowController serviceFlowController = pair.getValue();
			serviceFlowController.clearNextMinuteCallCount();
		}
	}
	
	
}
