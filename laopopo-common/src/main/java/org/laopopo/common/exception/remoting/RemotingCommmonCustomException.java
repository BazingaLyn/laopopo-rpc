package org.laopopo.common.exception.remoting;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class RemotingCommmonCustomException extends RemotingException {


	private static final long serialVersionUID = 1546308581637799641L;


	public RemotingCommmonCustomException(String message) {
        super(message, null);
    }


    public RemotingCommmonCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
