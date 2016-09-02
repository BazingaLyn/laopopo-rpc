package org.laopopo.example.generic.test_3;

import java.util.concurrent.atomic.AtomicInteger;

import org.laopopo.client.provider.DefaultProvider;
import org.laopopo.client.provider.flow.control.ControlResult;
import org.laopopo.client.provider.flow.control.FlowController;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.example.demo.service.ByeServiceImpl;
import org.laopopo.example.demo.service.HelloSerivceImpl;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyServerConfig;

public class ProviderTest {
	
public static void main(String[] args) throws InterruptedException, RemotingException {
		
		DefaultProvider defaultProvider = new DefaultProvider(new NettyClientConfig(), new NettyServerConfig());
		
		FlowController controller = new DefaultFlowController();
		controller.setMaxTimes(1000);
		
		defaultProvider.globalController(controller) //全局限流器
					   .registryAddress("127.0.0.1:18010") //注册中心的地址
					   .monitorAddress("127.0.0.1:19010") //监控中心的地址
					   .serviceListenAddress("127.0.0.1:8899") //暴露服务的地址
					   .publishService(new HelloSerivceImpl(),new ByeServiceImpl()) //暴露的服务
					   .start(); //启动服务
		
	}
	
	public static class DefaultFlowController implements FlowController {

		private AtomicInteger count = new AtomicInteger();
		
		private int maxLimit;
		
		@Override
		public void setMaxTimes(int limit) {
			this.maxLimit = limit;
		}
		
		@Override
		public ControlResult flowControl() {
			if (count.getAndIncrement() > maxLimit) {
                return new ControlResult(false, "has over maxlimit please wait");
            }
            return ControlResult.ALLOWED;
		}
		
	}

}
