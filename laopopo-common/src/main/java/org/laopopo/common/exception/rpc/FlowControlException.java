package org.laopopo.common.exception.rpc;

public class FlowControlException extends RuntimeException {

	private static final long serialVersionUID = 7655695153711594056L;

	public FlowControlException() {}

    public FlowControlException(String message) {
        super(message);
    }

    public FlowControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowControlException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
