package org.laopopo.remoting;

import org.laopopo.remoting.model.RemotingResponse;


/**
 * 
 * @author BazingaLyn
 * @description 远程调用之后的回调函数
 * @time 2016年8月10日11:06:40
 * @modifytime
 */
public interface InvokeCallback {
	
    void operationComplete(final RemotingResponse remotingResponse);
    
}
