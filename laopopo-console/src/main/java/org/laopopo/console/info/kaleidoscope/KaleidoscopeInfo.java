package org.laopopo.console.info.kaleidoscope;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.laopopo.common.utils.NamedThreadFactory;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.laopopo.remoting.netty.NettyRemotingClient;


/**
 * 
 * @author BazingaLyn
 * @description 信息收集的万花筒
 * @time 2016年8月17日
 * @modifytime
 */
public class KaleidoscopeInfo {
	
	private String registryAddress;
	
	private String monitorAddress;
	
	private NettyClientConfig clientConfig;
	
	// 连接monitor和注册中心
	private NettyRemotingClient nettyRemotingClient;
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("console-timer"));

	public KaleidoscopeInfo(String registryAddress,String monitorAddress) {
		this.registryAddress = registryAddress;
		this.monitorAddress = monitorAddress;
		this.nettyRemotingClient = new NettyRemotingClient(clientConfig);
	}
	
}
