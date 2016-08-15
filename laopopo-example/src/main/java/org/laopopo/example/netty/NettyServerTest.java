package org.laopopo.example.netty;

import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executors;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;
import org.laopopo.remoting.netty.NettyRemotingServer;
import org.laopopo.remoting.netty.NettyServerConfig;

public class NettyServerTest {
	
	public static final byte TEST = -1;
	
	public static void main(String[] args) {
		
		NettyServerConfig config = new NettyServerConfig();
		config.setListenPort(18001);
		NettyRemotingServer server = new NettyRemotingServer(config);
		server.registerProecessor(TEST, new NettyRequestProcessor() {
			
			@Override
			public RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter transporter) throws Exception {
				System.out.println(transporter);
				transporter.setCustomHeader(serializerImpl().readObject(transporter.bytes(), TestCommonCustomBody.class));
				transporter.setTransporterType(LaopopoProtocol.RESPONSE_REMOTING);
				return transporter;
			}
		}, Executors.newCachedThreadPool());
		server.start();
	}

}
