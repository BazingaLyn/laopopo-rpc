package org.laopopo.common.exception.remoting;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class RemotingContextException extends RemotingException {

	private static final long serialVersionUID = -6365082302690352325L;


	public RemotingContextException(String message) {
        super(message, null);
    }


    public RemotingContextException(String message, Throwable cause) {
        super(message, cause);
    }

}
