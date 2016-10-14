package org.laopopo.example.generic.test_4;

import org.laopopo.client.provider.DefaultProvider;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.example.demo.service.ByeServiceImpl;
import org.laopopo.example.demo.service.HelloSerivceImpl;

public class ProviderTest {

	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider();

		defaultProvider.serviceListenPort(8899) // 暴露服务的地址
				.publishService(new HelloSerivceImpl(), new ByeServiceImpl()) // 暴露的服务
				.start(); // 启动服务

	}

}
