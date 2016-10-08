package org.laopopo.example.demo.service;

public class HelloServiceMock implements HelloSerivce {

	@Override
	public String sayHello(String str) {
		
		//直接给出默认的返回值
		return "hello";
	}

}
