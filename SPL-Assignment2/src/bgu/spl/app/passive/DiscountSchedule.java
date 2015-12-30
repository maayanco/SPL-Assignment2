package bgu.spl.app.passive;

/**
 * An object which describes a schedule of a single discount that the manager will add to a specific 
 * shoe at a specific tick.
 */
public class DiscountSchedule {
	
	private String shoeType;
	private int amount;
	private int tick;
	
	/**
	 * @param shoeType - the type of the shoe that should be on discount
	 * @param tick - the tick at which the discount should be occur
	 * @param amount - the amount of shoes that should go on discount
	 */
	public DiscountSchedule(String shoeType,int tick,int amount) {
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}
	
	/**
	 * @return the shoe type that will be on discount
	 */
	public String getShoeType() {
		return shoeType;
	}
	
	/**
	 * @return the tick at which the discount will occur
	 */
	public int getTick() {
		return tick;
	}
	
	/**
	 * @return - the amount of shoes that will go on discount
	 */
	public int getAmount() {
		return amount;
	}
	
}
