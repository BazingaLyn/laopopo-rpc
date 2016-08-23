package org.laopopo.common.transport.body;

import java.util.concurrent.atomic.AtomicLong;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

public class RequestCustomBody implements CommonCustomBody {

	private static final AtomicLong invokeIdGenerator = new AtomicLong(0);

	private final long invokeId;
	
	private String serviceName;
	
	private Object[] args;

	private transient long timestamp;

	public RequestCustomBody() {
		this(invokeIdGenerator.getAndIncrement());
	}
	
	public RequestCustomBody(long invokeId) {
		this.invokeId = invokeId;
	}
	
	

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getInvokeId() {
		return invokeId;
	}
	
	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}

}
