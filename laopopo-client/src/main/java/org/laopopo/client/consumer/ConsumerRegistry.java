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
	
	void subcribeService(SubcribeService... subcribeServices);
	
	void start();
	
	
	public class SubcribeService {
		
		private String group;
		
		private String version;
		
		private String serviceName;
		
		public SubcribeService(){
			
		}

		public SubcribeService(String group, String version, String serviceName) {
			super();
			this.group = group;
			this.version = version;
			this.serviceName = serviceName;
		}

		public String getGroup() {
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}
		
		//TODO EQUAL HASHCODE
		
	}

}
