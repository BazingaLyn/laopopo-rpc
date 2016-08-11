package org.laopopo.example.netty;

import org.laopopo.remoting.netty.NettyRemotingServer;
import org.laopopo.remoting.netty.NettyServerConfig;

public class NettyServerTest {
	
	public static void main(String[] args) {
		NettyServerConfig config = new NettyServerConfig();
		config.setListenPort(18001);
		NettyRemotingServer server = new NettyRemotingServer(config);
		server.start();
	}

}
