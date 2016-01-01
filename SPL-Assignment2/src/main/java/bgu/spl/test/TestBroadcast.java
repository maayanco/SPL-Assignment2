package bgu.spl.test;

import bgu.spl.mics.Broadcast;

/**
 * Class that implements Broadcast, used to test the MessageBusImpl
 */
public class TestBroadcast implements Broadcast {
	
	private String result;

	public TestBroadcast(String result) {
		this.result = result;
	}

}
