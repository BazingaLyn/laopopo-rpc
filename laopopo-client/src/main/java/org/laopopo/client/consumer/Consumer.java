package org.laopopo.client.consumer;

import io.netty.channel.Channel;

import org.laopopo.common.exception.remoting.RemotingSendRequestException;
import org.laopopo.common.exception.remoting.RemotingTimeoutException;
import org.laopopo.common.loadbalance.LoadBalanceStrategy;
import org.laopopo.common.utils.ChannelGroup;
import org.laopopo.common.utils.UnresolvedAddress;
import org.laopopo.remoting.model.RemotingTransporter;



/**
 * 
 * @author BazingaLyn
 * @description 消费端的接口
 * @time 2016年8月15日
 * @modifytime
 */
public interface Consumer {
	
	/**
	 * 远程调用方法
	 * @param serviceName 远程调用的服务名
	 * @param args 参数
	 * @return 
	 * @throws Throwable
	 */
	Object call(String serviceName,Object... args) throws Throwable;
	
	/**
	 * 
	 * @param serviceName
	 * @param timeout 调用超时时间
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	Object call(String serviceName,long timeout,Object... args) throws Throwable;
	
	/**
	 * 在当服务端向注册中心订阅服务的时候进行管理
	 * @param serviceName
	 * @return
	 */
	SubscribeManager subscribeService(String serviceName);
	
	
	/**
	 * 去连接注册中心，获取到与注册中心的唯一一个channel
	 */
	void getOrUpdateHealthyChannel();
	
	/**
	 * 去注册中心订阅服务，注册中心返回结果之后，回调NotifyListener的方法
	 * @param subcribeServices
	 * @param listener
	 */
	void subcribeService(String subcribeServices,NotifyListener listener);
	
	/**
	 * 
	 * @param address 该地址是提供者的地址
	 * 一个服务提供者的地址可以维护一组channel，因为一个消费者实例与一个提供者之间的长链接数可以不止一个，不过当然一般情况下，一个就可以了
	 * @return
	 */
	ChannelGroup group(UnresolvedAddress address);
	
	/**
	 * 根据一个服务名，匹配用户给这个服务设定的负载均衡策略，根据这个负载均衡算法去找到这个服务对应的与提供者的Channel
	 * @param serviceName
	 * @return
	 */
	ChannelGroup loadBalance(String serviceName);
	
	/**
	 * 当注册中心告之某个服务多了一个提供者之后，我们需要将其更新
	 * @param serviceName
	 * @param group
	 * @return
	 */
	boolean addChannelGroup(String serviceName, ChannelGroup group);
	
	/**
	 * 当注册中心告之某个服务的提供者下线的时候，我们也需要服务路由表
	 * @param serviceName
	 * @param group
	 * @return
	 */
	boolean removeChannelGroup(String serviceName, ChannelGroup group);
	
	/**
	 * 核心方法，远程调用
	 * @param channel 消费者与服务提供者的之间建立的长连接的channel
	 * @param request 请求体 包含请求的参数，请求的方法名
	 * @param timeout 请求超时时间
	 * @return
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 * @throws InterruptedException
	 */
	RemotingTransporter sendRpcRequestToProvider(Channel channel, RemotingTransporter request,long timeout) throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException;
	
	/**
	 * 当注册中心推送某个服务的负载均衡策略发送变化之后，需要变更的信息
	 * @param serviceName
	 * @param loadBalanceStrategy
	 */
	void setServiceLoadBalanceStrategy(String serviceName,LoadBalanceStrategy loadBalanceStrategy);
	
	/**
	 * 启动consumer端实例
	 */
	void start();
	
	
	interface SubscribeManager {

		/**
		 * 启动对订阅服务管理的服务
		 */
        void start();

        /**
         * 当某个服务去注册中心注册之后，注册中心返回订阅结果，consumer实例
         * 拿着订阅结果，去向服务提供者建立长连接，因为建立长连接的过程是异步的，简而言之获取一个active的channel的是异步的，所以当一切貌似看起来ok的时候
         * 其实未必，所以必须进行回调管理，否则远程调用的时候，可能channel还没有准备就绪，会报错
         * 
         * 详细见 {@link NotifyListener} 类的头部注释
         * @param timeoutMillis
         * @return
         */
        boolean waitForAvailable(long timeoutMillis);
    }
	

}
