package bgu.spl.app.json;

/**
 * Describes a manager type. Used for conversion from json file to a java
 * object.
 */
public class Manager {

	private Discount[] discountSchedule;

	/**
	 * @return an array of discount schedules
	 */
	public Discount[] getDiscountSchedule() {
		return discountSchedule;
	}
}
