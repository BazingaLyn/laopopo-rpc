package org.laopopo.common.protocal;

public class LaopopoProtocol {
	
	/** Magic */
    public static final short MAGIC = (short) 0xbabe;
    
    public static final byte REQUEST_REMOTING = 1;
    
    public static final byte RESPONSE_REMOTING = 2;
    
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
