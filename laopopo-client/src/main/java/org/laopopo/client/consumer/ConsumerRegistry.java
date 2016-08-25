package org.laopopo.client.consumer;



/**
 * 
 * @author BazingaLyn
 * @description 消费端的注册
 * @time 2016年8月18日
 * @modifytime 2016年8月22日
 */
public interface ConsumerRegistry {
	
	void getOrUpdateHealthyChannel();
	
	void subcribeService(String subcribeServices,NotifyListener listener);
	
	void start();
	
	

}
