package org.laopopo.client.consumer.promise;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.laopopo.common.utils.Status.CLIENT_TIMEOUT;
import static org.laopopo.common.utils.Status.OK;
import static org.laopopo.common.utils.Status.SERVER_TIMEOUT;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.laopopo.client.provider.DefaultProviderRPCProcessor;
import org.laopopo.common.exception.remoting.RemotingTimeoutException;
import org.laopopo.common.exception.rpc.RemoteException;
import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.ResponseCustomBody;
import org.laopopo.common.transport.body.ResponseCustomBody.ResultWrapper;
import org.laopopo.common.utils.Status;
import org.laopopo.common.utils.SystemClock;
import org.laopopo.remoting.model.RemotingTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultResultPromise {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultProviderRPCProcessor.class);
	
	private static final ConcurrentMap<Long, DefaultResultPromise> resultsGathers = new ConcurrentHashMap<Long, DefaultResultPromise>();
	
	private final ReentrantLock lock = new ReentrantLock();
	
    private final Condition doneCondition = lock.newCondition();
    
    public static final long DEFAULT_TIMEOUT =  5 * 1000;
    
    private final long invokeId;
    private final Channel channel;
    private final RemotingTransporter request;
    private final long timeoutMillis;
    private final long startTimestamp = SystemClock.millisClock().now();
    
    private volatile long sentTimestamp;
    private volatile RemotingTransporter response;
    
    public DefaultResultPromise(Channel channel, RemotingTransporter request) {
    	this(channel, request, DEFAULT_TIMEOUT);
    }
    
    public DefaultResultPromise(Channel channel, RemotingTransporter request, long timeoutMillis) {
    	
    	invokeId = request.getOpaque();
        this.channel = channel;
        this.request = request;
        this.timeoutMillis = timeoutMillis > 0 ? timeoutMillis : DEFAULT_TIMEOUT;
        
        resultsGathers.put(invokeId, this);
	}
    
    public static boolean received(Channel channel, RemotingTransporter response) {
    	
    	long invokeId = response.getOpaque();
    	DefaultResultPromise defaultResultGather = resultsGathers.remove(invokeId);
    	
    	if (defaultResultGather == null) {
            logger.warn("A timeout response [{}] finally returned on {}.", response, channel);
            return false;
        }
    	defaultResultGather.doReceived(response);
        return true;
    	
	}

    private void doReceived(RemotingTransporter response) {
    	
    	this.response = response;
    	
    	final ReentrantLock _lock = lock;
        _lock.lock();
        try {
            doneCondition.signal();
        } finally {
            _lock.unlock();
        }
	}

	/**
     * 获取
     * @return
     */
	public Object getResult() throws Throwable {
		if (!isDone()) {
            long start = System.nanoTime();
            final ReentrantLock _lock = lock;
            _lock.lock();
            try {
                while (!isDone()) {
                    doneCondition.await(timeoutMillis, MILLISECONDS);

                    if (isDone() || (System.nanoTime() - start) > MILLISECONDS.toNanos(timeoutMillis)) {
                        break;
                    }
                }
            } finally {
                _lock.unlock();
            }

            if (!isDone()) {
                throw new RemotingTimeoutException(channel.remoteAddress().toString() + (sentTimestamp > 0 ? SERVER_TIMEOUT : CLIENT_TIMEOUT));
            }
        }
        return resultFromResponse();
	}

	private Object resultFromResponse() {
		final RemotingTransporter _response = this.response;
		ResponseCustomBody body = (ResponseCustomBody)_response.getCustomHeader();
        byte status = body.getStatus();
        if (status == OK.value()) {
        	ResultWrapper wrapper = body.getResultWrapper();
            return wrapper.getResult();
        }
        throw new RemoteException(_response.toString(), channel.remoteAddress());
	}

	private boolean isDone() {
		return response != null;
	}
	
	private static class TimeoutScanner implements Runnable {
		
		public void run() {
			for (;;) {
				try {
					for(DefaultResultPromise defaultResultGather:resultsGathers.values()){
						if(null == defaultResultGather || defaultResultGather.isDone()){
							continue;
						}
						if (SystemClock.millisClock().now() - defaultResultGather.startTimestamp > defaultResultGather.timeoutMillis) {
                            processingTimeoutFuture(defaultResultGather);
                        }
					}
					Thread.sleep(30);
				} catch (Exception e) {
				}
			}
		}

		private void processingTimeoutFuture(DefaultResultPromise defaultResultPromise) {
			ResultWrapper result = new ResultWrapper();
            Status status = defaultResultPromise.sentTimestamp > 0 ? SERVER_TIMEOUT : CLIENT_TIMEOUT;
            result.setError(new RemotingTimeoutException(defaultResultPromise.channel.remoteAddress().toString()+status));

    		ResponseCustomBody body = new ResponseCustomBody(status.value(), result);
            final RemotingTransporter response = RemotingTransporter.createResponseTransporter(LaopopoProtocol.RPC_RESPONSE, body, defaultResultPromise.invokeId);
            DefaultResultPromise.received(defaultResultPromise.channel, response);
		}
		
	}
	
	static {
        Thread t = new Thread(new TimeoutScanner(), "timeout.scanner");
        t.setDaemon(true);
        t.start();
    }

}
