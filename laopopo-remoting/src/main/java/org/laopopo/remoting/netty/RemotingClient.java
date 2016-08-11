package org.laopopo.remoting.netty;

import java.util.concurrent.ExecutorService;

import org.laopopo.remoting.exception.RemotingException;
import org.laopopo.remoting.exception.RemotingSendRequestException;
import org.laopopo.remoting.exception.RemotingTimeoutException;
import org.laopopo.remoting.model.NettyRequestProcessor;
import org.laopopo.remoting.model.RemotingTransporter;

public interface RemotingClient extends BaseRemotingService {

	public RemotingTransporter invokeSync(final String addr ,final RemotingTransporter request,final long timeoutMillis) throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException, RemotingException;

	void registerProcessor(final int requestCode, final NettyRequestProcessor processor, final ExecutorService executor);

	boolean isChannelWriteable(final String addr);

}
