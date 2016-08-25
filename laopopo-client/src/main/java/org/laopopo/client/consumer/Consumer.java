package org.laopopo.client.consumer;




public interface Consumer {
	
	Object call(String serviceName,Object... args) throws Throwable;
	
	Object call(String serviceName,long timeout,Object... args) throws Throwable;
	
	SubscribeManager subscribeService(String service);
	
	interface SubscribeManager {

        void start();

        boolean waitForAvailable(long timeoutMillis);
    }
	

}
