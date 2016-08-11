package org.laopopo.example.netty;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.remoting.exception.RemotingCommmonCustomException;
import org.laopopo.remoting.exception.RemotingException;
import org.laopopo.remoting.model.CommonCustomHeader;
import org.laopopo.remoting.model.RemotingTransporter;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyRemotingClient;

public class NettyClientTest {
	
	public static final byte TEST = -1;
	
	public static void main(String[] args) throws InterruptedException, RemotingException {
		NettyClientConfig nettyClientConfig = new NettyClientConfig();
		NettyRemotingClient client = new NettyRemotingClient(nettyClientConfig);
		client.start();
		
		TestCommonCustomHeader commonCustomHeader = new TestCommonCustomHeader(1, "test");
		
		RemotingTransporter remotingTransporter = RemotingTransporter.createRequestTransporter(TEST, commonCustomHeader, LaopopoProtocol.REQUEST_REMOTING);
		RemotingTransporter request = client.invokeSync("127.0.0.1:18001", remotingTransporter, 3000);
		System.out.println(request);
	}
	
	public static class TestCommonCustomHeader implements CommonCustomHeader {
		
		private int id;
		
		private String name;

		public TestCommonCustomHeader(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		@Override
		public void checkFields() throws RemotingCommmonCustomException {
			
		}
		
	}

}
