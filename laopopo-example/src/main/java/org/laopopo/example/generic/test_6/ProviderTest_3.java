package org.laopopo.example.generic.test_6;

import org.laopopo.client.provider.DefaultProvider;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.example.demo.service.HelloService_3;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyServerConfig;

public class ProviderTest_3 {
	
	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider(new NettyClientConfig(), new NettyServerConfig());

		defaultProvider.registryAddress("127.0.0.1:18010") // 注册中心的地址
				.serviceListenPort(7001) // 暴露服务的端口
				.publishService(new HelloService_3()) // 暴露的服务
				.start(); // 启动服务

	}

}
