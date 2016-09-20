package org.laopopo.common.exception.rpc;

/**
 * 
 * @author BazingaLyn
 * @description 服务提供者端处理消费者的请求的时候，出现的异常
 * @time 2016年9月14日
 * @modifytime
 */
public class ProviderHandlerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5212501638591073686L;
	
	public ProviderHandlerException() {}

    public ProviderHandlerException(String message) {
        super(message);
    }

    public ProviderHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderHandlerException(Throwable cause) {
        super(cause);
    }
	
	

}
