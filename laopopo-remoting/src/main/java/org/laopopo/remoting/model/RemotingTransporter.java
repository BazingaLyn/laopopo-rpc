package org.laopopo.remoting.model;

import java.util.concurrent.atomic.AtomicInteger;

public class RemotingTransporter {

	private static final AtomicInteger requestId = new AtomicInteger(0);

	private int code;

	private transient CommonCustomHeader customHeader;

	private transient long timestamp;

	private int opaque = requestId.getAndIncrement();

	protected RemotingTransporter() {
	}
	
	public static RemotingTransporter createRequestTransporter(int code,CommonCustomHeader commonCustomHeader){
		RemotingTransporter remotingTransporter = new RemotingTransporter();
		remotingTransporter.setCode(code);
		remotingTransporter.customHeader = commonCustomHeader;
		return remotingTransporter;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getOpaque() {
		return opaque;
	}

	public void setOpaque(int opaque) {
		this.opaque = opaque;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "RemotingTransporter [code=" + code + ", customHeader=" + customHeader + ", timestamp=" + timestamp + ", opaque=" + opaque + "]";
	}
	
	

}
