package org.laopopo.remoting.netty;

import org.laopopo.remoting.RPCHook;

/**
 * 
 * @author BazingaLyn
 * @description Netty网络通讯端Client端和Server端都需要实现的方法集合
 * @time 2016年8月10日
 * @modifytime
 */
public interface BaseRemotingService {
	
	/**
	 * Netty的一些参数的初始化
	 */
	void init();
	
	/**
	 * 启动Netty方法
	 */
	void start();
	
	/**
	 * 关闭Netty C/S 实例
	 */
	void shutdown();
	
	/**
	 * 注入钩子，Netty在处理的过程中可以嵌入一些方法，增加代码的灵活性
	 * @param rpcHook
	 */
	void registerRPCHook(RPCHook rpcHook);

}
