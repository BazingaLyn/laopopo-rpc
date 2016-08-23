package org.laopopo.common.exception.rpc;

public class ServerBusyException extends RuntimeException {

	private static final long serialVersionUID = 4322371152841396783L;

	public ServerBusyException() {}

    public ServerBusyException(String message) {
        super(message);
    }

    public ServerBusyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerBusyException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
