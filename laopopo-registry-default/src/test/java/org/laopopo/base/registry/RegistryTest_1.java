package org.laopopo.base.registry;

import org.laopopo.remoting.netty.NettyServerConfig;

/**
 * 
 * @author BazingaLyn
 * @description 
 * @time
 * @modifytime
 */
public class RegistryTest_1 {
	
	public static void main(String[] args) {
		NettyServerConfig config = new NettyServerConfig();
		//注册中心的端口号
		config.setListenPort(18010);
		DefaultRegistryServer defaultRegistryServer = new DefaultRegistryServer(config);
		defaultRegistryServer.start();
	}

}
