package bgu.spl.test;

import bgu.spl.mics.MicroService;

public class TestMicroService extends MicroService {
	public int name;

	public TestMicroService(String name) {
		super(name);
	}

	protected void initialize() {
		
	}
}
