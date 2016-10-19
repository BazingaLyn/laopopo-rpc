package org.laopopo.example.benchmark;

import org.laopopo.base.registry.DefaultRegistryServer;
import org.laopopo.base.registry.RegistryServerConfig;
import org.laopopo.common.rpc.ServiceReviewState;
import org.laopopo.remoting.netty.NettyServerConfig;

/**
 * 
 * @author BazingaLyn
 * @description 性能测试的注册中心端
 * @time
 * @modifytime
 */
public class BenchmarkRegistryTest {
	
	
	public static void main(String[] args) {
        
		NettyServerConfig config = new NettyServerConfig();
		RegistryServerConfig registryServerConfig = new RegistryServerConfig();
		registryServerConfig.setDefaultReviewState(ServiceReviewState.PASS_REVIEW);
		//注册中心的端口号
		config.setListenPort(18010);
		new DefaultRegistryServer(config,registryServerConfig).start();
		
	}
	
	

}
