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

	/**
	 * 请求的类型
	 * 例如该请求是用来订阅服务的，该请求是用来发布服务的等等的
	 * 假设 code == 1 代表是消费者订阅服务，则接收方注册中心接到该对象的时候，就会先获取该code，判断如果该code==1 则走订阅服务的处理分支代码
	 * 假设 code == 2 代表是提供者发布服务，则接收发注册中心接收该对象的时候，也会先获取该code，判断如果该code==2则走发布服务的处理分支代码
	 */
	private byte code;

	/**
	 * 请求的主体信息 {@link CommonCustomBody}是一个接口
	 * 假如code==1 则CommonCustomBody中则是一些订阅服务的具体信息
	 * 假如code==2 则CommonCustomBody中则是一些发布服务的具体信息
	 */
	private transient CommonCustomBody customHeader;

	/**
	 * 请求的时间戳
	 */
	private transient long timestamp;

	/**
	 * 请求的id
	 */
	private long opaque = requestId.getAndIncrement();
	
	/**
	 * 定义该传输对象是请求还是响应信息
	 */
	private byte transporterType;

	protected RemotingTransporter() {
	}
	
	/**
	 * 创建一个请求传输对象
	 * @param code 请求的类型
	 * @param commonCustomHeader 请求的正文
	 * @return
	 */
	public static RemotingTransporter createRequestTransporter(byte code,CommonCustomBody commonCustomHeader){
		RemotingTransporter remotingTransporter = new RemotingTransporter();
		remotingTransporter.setCode(code);
		remotingTransporter.customHeader = commonCustomHeader;
		remotingTransporter.transporterType = LaopopoProtocol.REQUEST_REMOTING;
		return remotingTransporter;
	}
	
	/**
	 * 创建一个响应对象
	 * @param code 响应对象的类型 
	 * @param commonCustomHeader 响应对象的正文
	 * @param opaque 此响应对象对应的请求对象的id
	 * @return
	 */
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
