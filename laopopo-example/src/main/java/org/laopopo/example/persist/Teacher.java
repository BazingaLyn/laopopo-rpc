package org.laopopo.example.persist;

import java.io.Serializable;

public class Teacher implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -7057437939312498114L;

	private Integer id;
	
	private String name;
	
	public Teacher(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
