package bgu.spl.app.services;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.passive.TerminationBroadcast;
import bgu.spl.app.passive.TickBroadcast;
import bgu.spl.mics.MicroService;

/**
 * This micro-service is our global system timer (handles the clock ticks in the
 * system). It is responsible for counting how much clock ticks passed since the
 * beggining of its execution and notifying every other microservice (thats
 * intersted) about it using the TickBroadcast.
 * <p>
 * The TimeService receives the number of milliseconds each clock tick takes
 * (speed:int) toogether with the number of ticks before termination
 * (duration:int) as a constructor arguments. Be careful that you are not
 * blocking the event loop of the timer micro-service. You can use the Timer
 * class in java to help you with that. The current time always start from 1.
 *
 */
public class TimeService extends MicroService {

	private static final Logger log = Logger.getLogger(TimeService.class.getName());
	private int currentTick;
	private int speed;
	private int duration;
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;

	/**
	 * 
	 * @param speed
	 *            - the interval between every two ticks
	 * @param duration
	 *            - ticks until TimeService should terminate
	 * @param startLatchObject
	 *            - CountDownLatch object which is used in the initialization
	 * @param endLatchObject
	 *            - CountDownLatch which is used in termination
	 * @throws InterruptedException
	 */
	public TimeService(int speed, int duration, CountDownLatch startLatchObject, CountDownLatch endLatchObject) {
		super("timer");
		this.currentTick = 0;
		this.speed = speed;
		this.duration = duration;
		this.startLatchObject = startLatchObject;
		this.endLatchObject = endLatchObject;
	}

	/**
	 * This method creates a timer (using a timertask) and set it to run at
	 * fixed rate (speed). Every interval it sends a new TickBroadcast. When the
	 * duration is finished, The time service sends a termination broadcast.
	 */
	@Override
	protected void initialize() {
		Timer timer = new Timer();

		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				currentTick = currentTick + 1;

				if (currentTick > duration) {
					TerminationBroadcast b = new TerminationBroadcast(true);
					sendBroadcast(b);
				} else {
					log.log(Level.INFO, "CURRENT TICK:" + currentTick);
					TickBroadcast b = new TickBroadcast(currentTick);
					sendBroadcast(b);
				}
			}
		};

		try {
			startLatchObject.await();
		} catch (InterruptedException e) {
			log.log(Level.WARNING, " TimeService received a InterruptedException");
		}

		timer.scheduleAtFixedRate(task, 0, speed);

		subscribeToTerminationBroadcast(timer, task);
	}

	/**
	 * This method handles TerminationBroadcasts, by starting a graceful
	 * termination of the TimeService. Firstly It subscribes to
	 * TerminationBroadcasts. When a new termination broadcast is received, we
	 * invoke the end latch countDown to indicate the TimeService is
	 * terminating, and call the terminate method in order to gracefully finish
	 * running the service.
	 */
	private void subscribeToTerminationBroadcast(Timer timer, TimerTask task) {
		subscribeBroadcast(TerminationBroadcast.class, terminationBroadcast -> {
			if (terminationBroadcast.getTerminationStatus() == true) {
				timer.cancel();
				task.cancel();
				terminate();
				endLatchObject.countDown();
			}
		});
	}

}
