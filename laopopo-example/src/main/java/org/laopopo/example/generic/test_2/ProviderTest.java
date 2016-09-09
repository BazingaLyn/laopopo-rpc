package org.laopopo.example.generic.test_2;

import org.laopopo.client.provider.DefaultProvider;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.example.demo.service.ByeServiceImpl;
import org.laopopo.example.demo.service.HelloSerivceImpl;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyServerConfig;

public class ProviderTest {
	
public static void main(String[] args) throws InterruptedException, RemotingException {
		
		DefaultProvider defaultProvider = new DefaultProvider(new NettyClientConfig(), new NettyServerConfig());
		
		
		defaultProvider.registryAddress("127.0.0.1:18010") //注册中心的地址
					   .serviceListenPort(8899) //暴露服务的地址
					   .publishService(new HelloSerivceImpl(),new ByeServiceImpl()) //暴露的服务
					   .start(); //启动服务
		
	}
	
}
