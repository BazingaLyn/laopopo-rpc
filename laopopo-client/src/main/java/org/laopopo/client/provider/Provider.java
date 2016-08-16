package org.laopopo.client.provider;

import java.util.List;

import org.laopopo.client.provider.flow.control.FlowController;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.remoting.model.RemotingTransporter;

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
	 * @param address
	 * @throws RemotingException 
	 * @throws InterruptedException 
	 */
	void publishedAndStartProvider(String address) throws InterruptedException, RemotingException;
	
	/**
	 * 
	 * @param listeningAddress
	 * @param controller
	 * @param obj
	 * @return
	 */
	void publishServiceAndListening(String listeningAddress,FlowController controller,Object ...obj);

}
