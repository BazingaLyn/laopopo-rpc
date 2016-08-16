package org.laopopo.common.protocal;


public class LaopopoProtocol {
	
	/** 协议头长度 */
    public static final int HEAD_LENGTH = 16;
	
	/** Magic */
    public static final short MAGIC = (short) 0xbabe;
    
    public static final byte REQUEST_REMOTING = 1;
    
    public static final byte RESPONSE_REMOTING = 2;
    
    public static final byte HANDLER_ERROR = -1;
    
    public static final byte HANDLER_BUSY = -2;
    
    //provider端向registry发送注册信息的code
    public static final byte PUBLISH_SERVICE = 65;
    
    //心跳
    public static final byte HEARTBEAT = 127;
    
    public static final byte ACK = 126;

    //consumer端向registry订阅服务后返回的订阅结果
	public static final byte SUBCRIBE_RESULT = 66;

	//向monitor端请求某个服务是否审核的请求code
	public static final byte REQUEST_REVIEW_RESULT = 71;
    
    
    private byte type;
    private byte sign;
    private long id;
    private int bodyLength;
    
    
	public byte type() {
		return type;
	}

	public void type(byte type) {
		this.type = type;
	}

	public byte sign() {
        return sign;
    }

    public void sign(byte sign) {
        this.sign = sign;
    }

    public long id() {
        return id;
    }

    public void id(long id) {
        this.id = id;
    }

    public int bodyLength() {
        return bodyLength;
    }

    public void bodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

}
