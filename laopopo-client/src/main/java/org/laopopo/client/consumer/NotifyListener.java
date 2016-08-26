package org.laopopo.client.consumer;

import org.laopopo.common.rpc.RegisterMeta;

/**
 * 
 * @author BazingaLyn
 * @description 当consumer从register注册中心获取到订阅信息之后返回的结果集，这边之所以做的相对复杂的原因就是因为
 * 从注册中心拿到提供者的地址之后，netty去连接的时候是异步的的，ChannelFuture.channel这边是异步的，也就是说你不知道啥时候能够
 * ChannelFuture.isSuccess()== true,除非在后面增加一个Listener，档期operationSuccess的时候才会周知用户，所有的动作初始化完毕了，可以直接调用
 * @time 2016年8月26日
 * @modifytime
 */
public interface NotifyListener {
	
	/**
	 * 接收到register返回的RegisterMeta的时候，去连接provider端
	 * @param registerMeta
	 * @param event
	 */
	void notify(RegisterMeta registerMeta, NotifyEvent event);

    enum NotifyEvent {
        CHILD_ADDED,
        CHILD_REMOVED
    }

}
