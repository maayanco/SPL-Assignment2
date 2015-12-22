package bgu.spl.app;

import bgu.spl.mics.MicroService;

public class TimeService extends MicroService{

	public TimeService(int speed, int duration) {
		super("timer");
		//speed is in milliseconds
		
	}

	@Override
	protected void initialize() {
		
		
	}
	
	private void doSomething(){
		new TickBroadcast(){
			
		}
	}

}
