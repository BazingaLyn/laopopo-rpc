package org.laopopo.remoting.model;

import java.util.concurrent.atomic.AtomicLong;

import org.laopopo.common.protocal.LaopopoProtocol;
import org.laopopo.common.transport.body.CommonCustomBody;

/**
 * 
 * @author BazingaLyn 
 * @description 网络传输的唯一对象
 * @time 2016年8月10日
 * @modifytime
 */
public class RemotingTransporter extends ByteHolder {

	private static final AtomicLong requestId = new AtomicLong(0l);

	private byte code;

	private transient CommonCustomBody customHeader;

	private transient long timestamp;

	private long opaque = requestId.getAndIncrement();
	
	private byte transporterType;

	protected RemotingTransporter() {
	}
	
	public static RemotingTransporter createRequestTransporter(byte code,CommonCustomBody commonCustomHeader){
		RemotingTransporter remotingTransporter = new RemotingTransporter();
		remotingTransporter.setCode(code);
		remotingTransporter.customHeader = commonCustomHeader;
		remotingTransporter.transporterType = LaopopoProtocol.REQUEST_REMOTING;
		return remotingTransporter;
	}
	
	
	public static RemotingTransporter createResponseTransporter(byte code,CommonCustomBody commonCustomHeader,long opaque){
		RemotingTransporter remotingTransporter = new RemotingTransporter();
		remotingTransporter.setCode(code);
		remotingTransporter.customHeader = commonCustomHeader;
		remotingTransporter.setOpaque(opaque);
		remotingTransporter.transporterType = LaopopoProtocol.RESPONSE_REMOTING;
		return remotingTransporter;
	}
	

	public byte getTransporterType() {
		return transporterType;
	}

	public void setTransporterType(byte transporterType) {
		this.transporterType = transporterType;
	}

	public byte getCode() {
		return code;
	}

	public void setCode(byte code) {
		this.code = code;
	}

	public long getOpaque() {
		return opaque;
	}

	public void setOpaque(long opaque) {
		this.opaque = opaque;
	}

	public long timestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public CommonCustomBody getCustomHeader() {
		return customHeader;
	}

	public void setCustomHeader(CommonCustomBody customHeader) {
		this.customHeader = customHeader;
	}
	

	public static RemotingTransporter newInstance(long id, byte sign,byte type, byte[] bytes) {
		RemotingTransporter remotingTransporter = new RemotingTransporter();
		remotingTransporter.setCode(sign);
		remotingTransporter.setTransporterType(type);
		remotingTransporter.setOpaque(id);
		remotingTransporter.bytes(bytes);
		return remotingTransporter;
	}

	@Override
	public String toString() {
		return "RemotingTransporter [code=" + code + ", customHeader=" + customHeader + ", timestamp=" + timestamp + ", opaque=" + opaque
				+ ", transporterType=" + transporterType + "]";
	}
	

}
