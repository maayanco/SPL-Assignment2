package bgu.spl.app.json;

/**
 * Describes a Discount type.
 * Used for the conversion from Json file to a java object
 */
public class Discount {
	
	private String shoeType;
	private int amount;
	private int tick;
	
	/**
	 * Returns the shoe type
	 * @return the shoe type
	 */
	public String getShoeType(){
		return shoeType;
	}
	
	/**
	 * Returns the amount 
	 * @return the amount
	 */
	public int getAmount(){
		return amount;
	}
	
	/** 
	 * Returns the tick
	 * @return the tick
	 */
	public int getTick(){
		return tick;
	}
}
