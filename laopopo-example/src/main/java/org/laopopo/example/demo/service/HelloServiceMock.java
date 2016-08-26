package org.laopopo.example.demo.service;

public class HelloServiceMock implements HelloSerivce {

	@Override
	public String sayHello(String str) {
		return "hello";
	}

}
