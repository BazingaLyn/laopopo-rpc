package org.laopopo.client.comsumer;

import java.util.List;

import org.laopopo.client.comsumer.ConsumerRegistry.SubcribeService;

/**
 * 
 * @author BazingaLyn
 * @description 消费者的注册处理功能
 * @time 2016年8月18日
 * @modifytime
 */
public class DefaultConsumerRegistry {
	
	private DefaultConsumer defaultConsumer;

	public DefaultConsumerRegistry(DefaultConsumer defaultConsumer) {
		this.defaultConsumer = defaultConsumer;
	}

	public void subcribeService(List<SubcribeService> subcribeServices) {
		
		if(null != subcribeServices && !subcribeServices.isEmpty()){
			
			for(SubcribeService service : subcribeServices){
				
//				this.defaultConsumer.
				
			}
			
		}
		
	}

}
