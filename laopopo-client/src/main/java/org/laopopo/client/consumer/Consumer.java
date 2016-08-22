package org.laopopo.client.consumer;

import org.laopopo.client.consumer.ConsumerRegistry.SubcribeService;


public interface Consumer {
	
	Object call(String serviceName,Object... args) throws Throwable;
	
	Object call(SubcribeService subcribeService,Object... args) throws Throwable;
	
	Object call(SubcribeService subcribeService,long timeout,Object... args) throws Throwable;
	
	Object call(String serviceName,long timeout,Object... args) throws Throwable;
	

}
