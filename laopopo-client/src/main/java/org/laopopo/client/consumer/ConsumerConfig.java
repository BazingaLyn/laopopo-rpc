package org.laopopo.client.consumer;

/**
 * 
 * @author BazingaLyn
 * @description 消费端配置属性
 * @time
 * @modifytime
 */
public class ConsumerConfig {

	private int retryConnectionRegistryTimes = 4;

	private long maxRetryConnectionRegsitryTime = 5000;
	
	private long registryTimeout = 3000;

	public int getRetryConnectionRegistryTimes() {
		return retryConnectionRegistryTimes;
	}

	public void setRetryConnectionRegistryTimes(int retryConnectionRegistryTimes) {
		this.retryConnectionRegistryTimes = retryConnectionRegistryTimes;
	}

	public long getMaxRetryConnectionRegsitryTime() {
		return maxRetryConnectionRegsitryTime;
	}

	public void setMaxRetryConnectionRegsitryTime(long maxRetryConnectionRegsitryTime) {
		this.maxRetryConnectionRegsitryTime = maxRetryConnectionRegsitryTime;
	}

	public long getRegistryTimeout() {
		return registryTimeout;
	}

	public void setRegistryTimeout(long registryTimeout) {
		this.registryTimeout = registryTimeout;
	}
	

}
