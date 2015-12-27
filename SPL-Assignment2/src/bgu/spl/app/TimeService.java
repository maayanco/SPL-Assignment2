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
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;
	
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	public TimeService(int speed, int duration, CountDownLatch startLatchObject, CountDownLatch endLatchObject) throws InterruptedException {
		super("timer");
		
		this.currentTick=1;
		this.speed=speed;
		this.duration=duration;
		
		this.startLatchObject=startLatchObject;
		startLatchObject.countDown();
		this.endLatchObject=endLatchObject;
		
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
					sendBroadcast(b); 
					System.out.println("CountDownLatch - counted down at "+getName());//debuuuug
					endLatchObject.countDown();
					terminate(); 
					this.cancel();
				}
				else{
					TickBroadcast b = new TickBroadcast(currentTick);
					sendBroadcast(b);
				}
			}
		};
		
		try {
			startLatchObject.await();
		} catch (InterruptedException e) {
			System.out.println("damnnn");
			e.printStackTrace();
		}
		
		timer.scheduleAtFixedRate(task, 0, speed);
		
	}
}
