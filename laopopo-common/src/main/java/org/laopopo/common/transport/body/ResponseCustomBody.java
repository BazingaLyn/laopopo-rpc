package org.laopopo.common.transport.body;

import static org.laopopo.common.utils.Status.OK;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;
import org.laopopo.common.exception.rpc.ProviderHandlerException;

public class ResponseCustomBody implements CommonCustomBody {
	
	private byte status = OK.value();

    private ResultWrapper resultWrapper;	
    
	public ResponseCustomBody(byte status, ResultWrapper resultWrapper) {
		this.status = status;
		this.resultWrapper = resultWrapper;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public ResultWrapper getResultWrapper() {
		return resultWrapper;
	}

	public void setResultWrapper(ResultWrapper resultWrapper) {
		this.resultWrapper = resultWrapper;
	}

	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}
	
	public static class ResultWrapper {
		
		private Object result;
	    private String error;
	    
		public Object getResult() {
			return result;
		}
		
		public void setResult(Object result) {
			this.result = result;
		}
		public String getError() {
			return error;
		}
		public void setError(Throwable t) {
			this.error = t.getMessage();
		}
	    
	}

	public Object getResult() {
		
		if(status == OK.value()){
			return getResultWrapper().getResult();
		}else{
			throw new ProviderHandlerException(getResultWrapper().getError());
		}
	}

}
