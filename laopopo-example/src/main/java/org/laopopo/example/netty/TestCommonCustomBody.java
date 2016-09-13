package org.laopopo.example.netty;

import java.io.Serializable;

import org.laopopo.common.exception.remoting.RemotingCommmonCustomException;
import org.laopopo.common.transport.body.CommonCustomBody;

public class TestCommonCustomBody implements CommonCustomBody,Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7679994718274344134L;

	private int id;
	
	private String name;
	
	private ComplexTestObj complexTestObj;
	
	public TestCommonCustomBody() {
	}

	public TestCommonCustomBody(int id, String name, ComplexTestObj complexTestObj) {
		this.id = id;
		this.name = name;
		this.complexTestObj = complexTestObj;
	}

	
	public ComplexTestObj getComplexTestObj() {
		return complexTestObj;
	}

	public void setComplexTestObj(ComplexTestObj complexTestObj) {
		this.complexTestObj = complexTestObj;
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

	@Override
	public String toString() {
		return "TestCommonCustomBody [id=" + id + ", name=" + name + ", complexTestObj=" + complexTestObj + "]";
	}

	public static class ComplexTestObj implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5694424296393939225L;

		private String attr1;
		
		private Integer attr2;
		
		public ComplexTestObj() {
		}
		
		public ComplexTestObj(String attr1, Integer attr2) {
			super();
			this.attr1 = attr1;
			this.attr2 = attr2;
		}


		public String getAttr1() {
			return attr1;
		}

		public void setAttr1(String attr1) {
			this.attr1 = attr1;
		}

		public Integer getAttr2() {
			return attr2;
		}

		public void setAttr2(Integer attr2) {
			this.attr2 = attr2;
		}

		@Override
		public String toString() {
			return "ComplexTestObj [attr1=" + attr1 + ", attr2=" + attr2 + "]";
		}
		
	}

}
