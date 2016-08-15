package org.laopopo.common.exception.remoting;

public class RemotingNoSighException extends RemotingException {

	private static final long serialVersionUID = -1661779813708564404L;


	public RemotingNoSighException(String message) {
        super(message, null);
    }


    public RemotingNoSighException(String message, Throwable cause) {
        super(message, cause);
    }

}
