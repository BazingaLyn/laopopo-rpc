package org.laopopo.remoting.netty;

import static org.laopopo.common.utils.Constants.AVAILABLE_PROCESSORS;


public class NettyServerConfig implements Cloneable{
	
    private int listenPort = 8888;
    
    private int serverWorkerThreads = AVAILABLE_PROCESSORS << 1;
    
    private int channelInactiveHandlerThreads = 1;

    private int serverSocketSndBufSize = -1;
    
    private int serverSocketRcvBufSize = -1;
    
    private int writeBufferLowWaterMark = -1;
    private int writeBufferHighWaterMark = -1;

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	public int getServerWorkerThreads() {
		return serverWorkerThreads;
	}

	public void setServerWorkerThreads(int serverWorkerThreads) {
		this.serverWorkerThreads = serverWorkerThreads;
	}
	

	public int getChannelInactiveHandlerThreads() {
		return channelInactiveHandlerThreads;
	}

	public void setChannelInactiveHandlerThreads(int channelInactiveHandlerThreads) {
		this.channelInactiveHandlerThreads = channelInactiveHandlerThreads;
	}

	public int getServerSocketSndBufSize() {
		return serverSocketSndBufSize;
	}

	public void setServerSocketSndBufSize(int serverSocketSndBufSize) {
		this.serverSocketSndBufSize = serverSocketSndBufSize;
	}

	public int getServerSocketRcvBufSize() {
		return serverSocketRcvBufSize;
	}

	public void setServerSocketRcvBufSize(int serverSocketRcvBufSize) {
		this.serverSocketRcvBufSize = serverSocketRcvBufSize;
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
