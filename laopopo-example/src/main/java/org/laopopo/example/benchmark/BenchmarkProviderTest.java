package org.laopopo.example.benchmark;

import org.laopopo.client.provider.DefaultProvider;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.example.demo.service.HelloServiceBenchmark;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyServerConfig;

/**
 * 
 * @author BazingaLyn
 * @description 性能测试的provider端
 * @time
 * @modifytime
 */
public class BenchmarkProviderTest {

	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider(new NettyClientConfig(), new NettyServerConfig());

		defaultProvider.registryAddress("127.0.0.1:18010") // 注册中心的地址
				.serviceListenPort(8899) // 暴露服务的地址
				.publishService(new HelloServiceBenchmark()) // 暴露的服务
				.start(); // 启动服务

	}

}
