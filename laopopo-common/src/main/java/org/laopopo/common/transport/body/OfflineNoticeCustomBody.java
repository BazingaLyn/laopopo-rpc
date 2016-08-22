package org.laopopo.common.transport.body;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;

/**
 * 
 * @author BazingaLyn
 * @description 下线通知的时候发送给consumer的主体
 * @time 2016年8月22日
 * @modifytime
 */
public class OfflineNoticeCustomBody implements CommonCustomBody {
	
	private int port;
	
	private String host;

	@Override
	public void checkFields() throws RemotingCommmonCustomException {
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	

}
