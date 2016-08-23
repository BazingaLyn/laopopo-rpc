package org.laopopo.common.exception.rpc;

public class ServiceNotFoundException extends RuntimeException {


	private static final long serialVersionUID = -6386677056268077683L;

	public ServiceNotFoundException() {}

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
