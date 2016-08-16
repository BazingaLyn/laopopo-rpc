package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description
 * @time
 * @modifytime
 */
public class AckCustomBody implements CommonCustomBody {
	
	private int requestId;
	
	private boolean success;
	
    private String desc;
    
	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return "AckCustomBody [requestId=" + requestId + ", success=" + success + ", desc=" + desc + "]";
	}
	

}
