package bgu.spl.app;

import bgu.spl.mics.MicroService;
import java.util.Timer;
import java.util.TimerTask;

public class TimeService extends MicroService{
	
	private int currentTick;
	private int speed;
	private int duration;
	
	public TimeService(int speed, int duration) {
		super("timer");
		
		this.currentTick=1;
		this.speed=speed;
		this.duration=duration;
	}
	
	@Override
	protected void initialize() {
		
		Timer timer = new Timer();
		
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				currentTick=currentTick+1;
				if(currentTick==duration)
					terminate(); //so gracefull!
				TickBroadcast b = new TickBroadcast(currentTick);
				sendBroadcast(b);
				//System.out.println("current time is: "+currentTick);
			}
		};
		
		timer.scheduleAtFixedRate(task, 0, speed);	
	}
}
