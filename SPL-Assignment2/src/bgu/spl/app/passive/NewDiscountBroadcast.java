package bgu.spl.app.passive;

import bgu.spl.mics.Broadcast;

/**
 * Broadcast message that is sent when the manager of the store decides to have
 * a sale on a specific shoe
 */
public class NewDiscountBroadcast implements Broadcast {

	private String shoeType;
	private int amountOnSale;

	/**
	 * 
	 * @param shoeType
	 *            - the shoe type to be set on discount
	 * @param amountOnSale
	 *            - the amount of shoes to be set on sale
	 */
	public NewDiscountBroadcast(String shoeType, int amountOnSale) {
		this.shoeType = shoeType;
		this.amountOnSale = amountOnSale;
	}

	/**
	 * @return - the shoe type to be set on discount
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return - the amount of shoes to be set on sale
	 */
	public int getAmountOnSale() {
		return amountOnSale;
	}

}
