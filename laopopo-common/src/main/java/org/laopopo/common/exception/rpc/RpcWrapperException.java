package org.laopopo.common.exception.rpc;


/**
 * 
 * @author BazingaLyn
 * @description 对服务进行编织的时候，发送的异常
 * @time 2016年9月9日
 * @modifytime
 */
public class RpcWrapperException extends RuntimeException {
	

	private static final long serialVersionUID = 5395455693773821359L;

	public RpcWrapperException() {}

    public RpcWrapperException(String message) {
        super(message);
    }

    public RpcWrapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcWrapperException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
