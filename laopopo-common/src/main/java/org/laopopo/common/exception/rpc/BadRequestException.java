package org.laopopo.common.exception.rpc;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 5227140869990506563L;

	public BadRequestException() {}

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
