package org.laopopo.common.exception.remoting;

public class RemotingContextException extends RemotingException {

	private static final long serialVersionUID = -6365082302690352325L;


	public RemotingContextException(String message) {
        super(message, null);
    }


    public RemotingContextException(String message, Throwable cause) {
        super(message, cause);
    }

}
