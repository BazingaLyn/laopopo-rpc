package org.laopopo.common.protocal;


/**
 * 
 * @author BazingaLyn
 * @description 网络传输的协议头信息
 * @time 2016年8月9日
 * @modifytime
 */
public class LaopopoProtocol {
	
	/** 协议头长度 */
    public static final int HEAD_LENGTH = 16;
	
	/** Magic */
    public static final short MAGIC = (short) 0xbabe;
    
    /** 发送的是请求信息*/
    public static final byte REQUEST_REMOTING = 1;
    
    /** 发送的是响应信息*/
    public static final byte RESPONSE_REMOTING = 2;
    
    
    public static final byte RPC_REMOTING = 3;
    
    public static final byte HANDLER_ERROR = -1;
    
    public static final byte HANDLER_BUSY = -2;
    
    //provider端向registry发送注册信息的code
    public static final byte PUBLISH_SERVICE = 65;
    //consumer端向registry订阅服务后返回的订阅结果
  	public static final byte SUBCRIBE_RESULT = 66;
  	//订阅服务取消
  	public static final byte SUBCRIBE_SERVICE_CANCEL = 67;
  	//取消发布服务
  	public static final byte PUBLISH_CANCEL_SERVICE = 68;
  	//consumer发送给registry注册服务
  	public static final byte SUBSCRIBE_SERVICE = 69;
  	//管理服务的请求
  	public static final byte MANAGER_SERVICE = 70;
  	//远程调用的请求
  	public static final byte RPC_REQUEST = 72;
  	//降级
  	public static final byte DEGRADE_SERVICE = 73;
    //自动降级
  	public static final byte AUTO_DEGRADE_SERVICE = 74;
  	//调用调用的响应
  	public static final byte RPC_RESPONSE = 75;
  	//修改负载策略
  	public static final byte CHANGE_LOADBALANCE = 76;
  	//统计信息
  	public static final byte MERTRICS_SERVICE = 77;
    //心跳
    public static final byte HEARTBEAT = 127;
    //ACK
    public static final byte ACK = 126;

    
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
