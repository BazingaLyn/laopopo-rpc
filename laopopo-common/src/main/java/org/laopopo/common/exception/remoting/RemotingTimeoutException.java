package org.laopopo.common.exception.remoting;

public class RemotingTimeoutException extends RemotingException {

	private static final long serialVersionUID = 8752267201986569541L;


	public RemotingTimeoutException(String message) {
        super(message);
    }


    public RemotingTimeoutException(String addr, long timeoutMillis) {
        this(addr, timeoutMillis, null);
    }


    public RemotingTimeoutException(String addr, long timeoutMillis, Throwable cause) {
        super("wait response on the channel <" + addr + "> timeout, " + timeoutMillis + "(ms)", cause);
    }
}
