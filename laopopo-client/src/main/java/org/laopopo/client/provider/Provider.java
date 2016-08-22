package org.laopopo.client.provider;

import org.laopopo.client.provider.flow.control.FlowController;
import org.laopopo.common.exception.remoting.RemotingException;

/**
 * 
 * @author BazingaLyn
 * @description provider端的接口
 * @time 2016年8月16日
 * @modifytime
 */
public interface Provider {
	
	
	void start();
	
	/**
	 * 连接到注册中心 address : host:port,host:port
	 * @param address 注册中心的地址
	 * @throws RemotingException 
	 * @throws InterruptedException 
	 */
	void publishedAndStartProvider(String address) throws InterruptedException, RemotingException;
	
	/**
	 * 在某个ip和端口上提供哪些服务
	 * @param listeningAddress 供consumer连接的地址号
	 * @param controller
	 * @param obj
	 * @return
	 */
	void publishServiceAndListening(String listeningAddress,FlowController controller,Object ...obj);

}
