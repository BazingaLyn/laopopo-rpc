package org.laopopo.example.generic.test_7;

import org.laopopo.client.provider.DefaultProvider;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.example.demo.service.HelloService_2;

public class ProviderTest_2 {
	
	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider();

		defaultProvider.serviceListenPort(8001)    					  // 暴露服务的地址
				.publishService(new HelloService_2()) 				  // 暴露的服务
				.start(); 											  // 启动服务

	}

}
