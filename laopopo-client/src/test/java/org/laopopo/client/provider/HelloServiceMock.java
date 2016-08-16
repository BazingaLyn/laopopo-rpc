package org.laopopo.client.provider;

public class HelloServiceMock implements HelloSerivce {

	@Override
	public String sayHello(String str) {
		return "hello";
	}

}
