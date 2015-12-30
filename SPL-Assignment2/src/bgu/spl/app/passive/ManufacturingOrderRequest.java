package bgu.spl.app.passive;

import bgu.spl.mics.Request;

/**
 * Request that is sent when the the store manager want that a shoe factory will
 * manufacture a shoe for the store.
 * <p>
 * Its response type expected to be a Receipt.
 * On the case the manufacture was not completed successfully null should be
 * returned as the request result.
 *
 */
public class ManufacturingOrderRequest implements Request<Receipt> {
	
	private String shoeType;
	private int amount;
	private int initialRequestTick;
	
	/**
	 * 
	 * @param shoeType - the type of shoe that should be manufactured
	 * @param amount - the amount of shoes to be manufactured
	 * @param initialRequestTick - the tick at which the initial request was issued
	 */
	public ManufacturingOrderRequest(String shoeType, int amount, int initialRequestTick){
		this.shoeType=shoeType;
		this.amount=amount;
		this.initialRequestTick=initialRequestTick;
	}

	/**
	 * @return the shoe type to be manufactured
	 */
	public String getShoeType() {
		return shoeType;
	}
	
	/**
	 * @return - the amount of shoes to be manufactured
	 */
	public int getAmount() {
		return amount;
	}
	
	/**
	 * @return - the tick at which the initial request was issued
	 */
	public int getInitialRequestTick(){
		return initialRequestTick;
	}
	
}
