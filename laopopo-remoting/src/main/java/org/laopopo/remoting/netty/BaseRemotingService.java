package org.laopopo.remoting.netty;

import org.laopopo.remoting.RPCHook;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public interface BaseRemotingService {
	
	void init();
	
	void start();
	
	void shutdown();
	
	void registerRPCHook(RPCHook rpcHook);

}
