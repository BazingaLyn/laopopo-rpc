package org.laopopo.base.registry;

import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyServerConfig;

public class RegistryTest_1 {
	
	public static void main(String[] args) {
		NettyServerConfig config = new NettyServerConfig();
		//注册中心的端口号
		config.setListenPort(18010);
		NettyClientConfig clientConfig = new NettyClientConfig();
		//monitor的地址
		clientConfig.setDefaultAddress("127.0.0.1:19999");
		DefaultRegistryServer defaultRegistryServer = new DefaultRegistryServer(config, clientConfig);
		defaultRegistryServer.start();
	}

}
