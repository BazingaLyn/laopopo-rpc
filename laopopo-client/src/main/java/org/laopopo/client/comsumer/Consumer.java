package org.laopopo.client.comsumer;

import org.laopopo.client.comsumer.ConsumerRegistry.SubcribeService;


public interface Consumer {
	
	Object call(String serviceName,Object... args) throws Throwable;
	
	Object call(SubcribeService subcribeService,Object... args) throws Throwable;
	
	Object call(SubcribeService subcribeService,long timeout,Object... args) throws Throwable;
	
	Object call(String serviceName,long timeout,Object... args) throws Throwable;
	

}
