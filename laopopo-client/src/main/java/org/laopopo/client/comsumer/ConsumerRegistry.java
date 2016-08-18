package org.laopopo.client.comsumer;

import java.util.List;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public interface ConsumerRegistry {
	
	
	void subcribeService(List<SubcribeService> subcribeServices);
	
	
	public class SubcribeService {
		
		private String group;
		
		private String version;
		
		private String serviceName;

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
		
	}

}
