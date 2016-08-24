package org.laopopo.common.exception.rpc;

public class NoServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7743840667451216980L;
	
	public NoServiceException() {
        super();
    }

    public NoServiceException(String message) {
        super(message);
    }

    public NoServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoServiceException(Throwable cause) {
        super(cause);
    }

}
