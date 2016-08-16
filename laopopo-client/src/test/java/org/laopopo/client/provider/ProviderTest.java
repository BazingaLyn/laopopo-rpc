package org.laopopo.client.provider;

import java.util.concurrent.atomic.AtomicInteger;

import org.laopopo.client.provider.flow.control.ControlResult;
import org.laopopo.client.provider.flow.control.FlowController;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyServerConfig;

public class ProviderTest {
	
	public static void main(String[] args) throws InterruptedException, RemotingException {
		
		DefaultProvider defaultProvider = new DefaultProvider(new NettyClientConfig(), new NettyServerConfig());
		defaultProvider.start();
		FlowController controller = new DefaultFlowController();
		controller.setMaxTimes(1000);
		
		defaultProvider.publishServiceAndListening("127.0.0.1:8899", controller, new HelloSerivceImpl(),new ByeServiceImpl());
		
		defaultProvider.publishedAndStartProvider("127.0.0.1:18010");
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
