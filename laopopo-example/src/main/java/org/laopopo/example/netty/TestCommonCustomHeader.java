package org.laopopo.example.netty;

import org.laopopo.remoting.exception.RemotingCommmonCustomException;
import org.laopopo.remoting.model.CommonCustomHeader;

public class TestCommonCustomHeader implements CommonCustomHeader {
	
	private int id;
	
	private String name;

	public TestCommonCustomHeader(int id, String name) {
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
