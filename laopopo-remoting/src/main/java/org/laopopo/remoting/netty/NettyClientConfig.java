package org.laopopo.remoting.netty;

public class NettyClientConfig {

	private int clientWorkerThreads = 4;
	private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
	private long connectTimeoutMillis = 3000;
	private long channelNotActiveInterval = 1000 * 60;

	private int clientChannelMaxIdleTimeSeconds = 120;

	private int clientSocketSndBufSize = -1;
	private int clientSocketRcvBufSize = -1;

	private int writeBufferLowWaterMark = -1;
	private int writeBufferHighWaterMark = -1;

	public int getClientWorkerThreads() {
		return clientWorkerThreads;
	}

	public void setClientWorkerThreads(int clientWorkerThreads) {
		this.clientWorkerThreads = clientWorkerThreads;
	}

	public int getClientCallbackExecutorThreads() {
		return clientCallbackExecutorThreads;
	}

	public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
		this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
	}

	public long getConnectTimeoutMillis() {
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(long connectTimeoutMillis) {
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	public long getChannelNotActiveInterval() {
		return channelNotActiveInterval;
	}

	public void setChannelNotActiveInterval(long channelNotActiveInterval) {
		this.channelNotActiveInterval = channelNotActiveInterval;
	}

	public int getClientChannelMaxIdleTimeSeconds() {
		return clientChannelMaxIdleTimeSeconds;
	}

	public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds) {
		this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
	}

	public int getClientSocketSndBufSize() {
		return clientSocketSndBufSize;
	}

	public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
		this.clientSocketSndBufSize = clientSocketSndBufSize;
	}

	public int getClientSocketRcvBufSize() {
		return clientSocketRcvBufSize;
	}

	public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
		this.clientSocketRcvBufSize = clientSocketRcvBufSize;
	}

	public int getWriteBufferLowWaterMark() {
		return writeBufferLowWaterMark;
	}

	public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
		this.writeBufferLowWaterMark = writeBufferLowWaterMark;
	}

	public int getWriteBufferHighWaterMark() {
		return writeBufferHighWaterMark;
	}

	public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
		this.writeBufferHighWaterMark = writeBufferHighWaterMark;
	}

}
