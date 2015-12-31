package bgu.spl.app.passive;

/**
 * An object which describes a schedule of a single client-purchase at a
 * specific tick.
 */
public class PurchaseSchedule {

	private String shoeType;
	private int tick;

	/**
	 * @param shoeType
	 *            - the shoe type to be purchased
	 * @param tick
	 *            - the tick at which the purchase should be initiated
	 */
	public PurchaseSchedule(String shoeType, int tick) {
		this.shoeType = shoeType;
		this.tick = tick;
	}

	/**
	 * @return - the shoe type to be purchased
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return - the tick at which the purchase should be initiated
	 */
	public int getTick() {
		return tick;
	}

}
