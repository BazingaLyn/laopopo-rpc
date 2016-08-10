package org.laopopo.remoting;

import org.laopopo.remoting.model.RemotingTransporter;

/**
 * 
 * @author BazingaLyn
 * @description RPC的回调钩子，在发送请求和接收请求的时候触发，这样做事增加程序的健壮性和灵活性
 * @time 2016年8月10日14:40:47
 * @modifytime
 */
public interface RPCHook {
	
	void doBeforeRequest(final String remoteAddr, final RemotingTransporter request);

    void doAfterResponse(final String remoteAddr, final RemotingTransporter request,final RemotingTransporter response);

}
