package org.laopopo.remoting.model;

/**
 * 
 * @author BazingaLyn
 * @description 
 * @time 2016年8月9日
 * @modifytime
 */
public class ByteHolder {
	
	private transient byte[] bytes;

    public byte[] bytes() {
        return bytes;
    }

    public void bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int size() {
        return bytes == null ? 0 : bytes.length;
    }

}
