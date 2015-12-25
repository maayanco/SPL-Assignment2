package bgu.spl.app;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeService extends MicroService{
	
	private int currentTick;
	private int speed;
	private int duration;
	private CountDownLatch latchObject;
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	public TimeService(int speed, int duration, CountDownLatch latchObject) throws InterruptedException {
		super("timer");
		
		this.currentTick=1;
		this.speed=speed;
		this.duration=duration;
		this.latchObject=latchObject;
		
		
	}
	
	@Override
	protected void initialize(){
		Timer timer = new Timer();
		
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				currentTick=currentTick+1;
				log.log(Level.INFO, "CURRENT TICK:"+currentTick);
				if(currentTick==duration){
					TerminationBroadcast b = new TerminationBroadcast(true);
					sendBroadcast(b); //tell everyone to terminate
					latchObject.countDown();
					terminate(); //so graceful!
					latchObject.countDown();
				}
				TickBroadcast b = new TickBroadcast(currentTick);
				sendBroadcast(b);
				//System.out.println("current time is: "+currentTick);
			}
		};
		
		timer.scheduleAtFixedRate(task, 0, speed);	
	}
}
