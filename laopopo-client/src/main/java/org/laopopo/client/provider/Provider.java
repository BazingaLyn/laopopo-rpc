package org.laopopo.client.provider;

import io.netty.channel.Channel;

import org.laopopo.client.provider.flow.control.FlowController;
import org.laopopo.common.exception.remoting.RemotingException;
import org.laopopo.remoting.model.RemotingTransporter;


/**
 * 
 * @author BazingaLyn
 * @description provider端的接口
 * 服务提供者端需要提供以下接口
 * 1)需要暴露哪些服务【必要】{@link Provider #publishService(Object...)}
 * 2)暴露的服务在哪个端口上提供【必要】{@link Provider #serviceListenAddress(String)}
 * 3)设置服务的全局限流器【非必要】{@link Provider #globalController(FlowController)}
 * 4)设置注册中心的地址【必要】{@link Provider #registryAddress(String)}
 * 5)暴露启动服务提供者的方法【必须调用】{@link Provider #start()}
 * @time 2016年8月16日
 * @modifytime 2016年8月23日
 */
public interface Provider {
	
	/**
	 * 启动provider的实例
	 * @throws RemotingException 
	 * @throws InterruptedException 
	 */
	void start() throws InterruptedException, RemotingException;
	
	
	/**
	 * 发布服务
	 * @throws InterruptedException
	 * @throws RemotingException
	 */
	void publishedAndStartProvider() throws InterruptedException, RemotingException;
	
	
	/**
	 * 设置全局限流器
	 * @param globalController
	 * @return
	 */
	Provider globalController(FlowController globalController);
	
	
	
	/**
	 * 暴露服务的地址
	 * @param serviceListenAddress
	 * @return
	 */
	Provider serviceListenAddress(String serviceListenAddress);
	
	
	
	/**
	 * 设置注册中心的地址  host:port,host1:port1
	 * @param registryAddress
	 * @return
	 */
	Provider registryAddress(String registryAddress);
	
	/**
	 * 需要暴露的接口
	 * @param obj
	 */
	Provider publishService(Object ...obj);
	
	
	/**
	 * 处理消费者的rpc请求
	 * @param request
	 * @param channel
	 * @return
	 */
	void handlerRPCRequest(RemotingTransporter request, Channel channel);
	
	
}
