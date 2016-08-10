package org.laopopo.remoting.exception;

public class RemotingException extends Exception {
	

	private static final long serialVersionUID = -298481855025395391L;


	public RemotingException(String message) {
        super(message);
    }


    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
