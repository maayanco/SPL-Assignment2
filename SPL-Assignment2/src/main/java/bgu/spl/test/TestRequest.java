package bgu.spl.test;

import bgu.spl.mics.Request;

/**
 * Class that implements Request, used to test the MessageBusImpl
 */
public class TestRequest implements Request<String> {
	
	private String result;

	public TestRequest(String result) {
		this.result = result;
	}

}
