package org.laopopo.common.transport.body;

import java.util.concurrent.atomic.AtomicLong;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description 远程调用的参数
 * @time 2016年8月19日
 * @modifytime
 */
public class RequestCustomBody implements CommonCustomBody {

	private static final AtomicLong invokeIdGenerator = new AtomicLong(0);

	private final long invokeId; //调用的Id,全局只有一个
	private String serviceName;  //调用的服务名
	private Object[] args;       //调用服务的参数
	private long timestamp;      //调用的时间

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
