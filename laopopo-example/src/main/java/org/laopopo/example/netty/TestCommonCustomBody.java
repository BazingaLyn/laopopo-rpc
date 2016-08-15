package org.laopopo.example.netty;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;
import org.laopopo.common.transport.body.CommonCustomBody;

public class TestCommonCustomBody implements CommonCustomBody {
	
	private int id;
	
	private String name;

	public TestCommonCustomBody(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void checkFields() throws RemotingCommmonCustomException {
		
	}

}
