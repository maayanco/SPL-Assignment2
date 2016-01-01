package bgu.spl.app.json;

/**
 * Describes a Time type Used for the conversion from a json file to a java
 * object
 *
 */
public class Time {

	private int speed;
	private int duration;

	/**
	 * 
	 * @return - the speed of the timer
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * 
	 * @return - the duration of the simulation
	 */
	public int getDuration() {
		return duration;
	}
}
