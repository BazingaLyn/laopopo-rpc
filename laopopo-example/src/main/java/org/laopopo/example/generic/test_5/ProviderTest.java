package org.laopopo.example.generic.test_5;

import org.laopopo.client.provider.DefaultProvider;
import org.laopopo.common.exception.remoting.RemotingException;

public class ProviderTest {
	
public static void main(String[] args) throws InterruptedException, RemotingException {
		
		DefaultProvider defaultProvider = new DefaultProvider();
		
		defaultProvider.serviceListenPort(8899) //暴露服务的地址
					   .publishService(new HelloServiceFlowControllerImpl()) //暴露的服务
					   .start(); //启动服务
		
	}
	

}
