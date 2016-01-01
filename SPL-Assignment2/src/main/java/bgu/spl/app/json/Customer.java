package bgu.spl.app.json;

import bgu.spl.app.passive.PurchaseSchedule;

/**
 * Describes a Customer type. Used for the conversion from Json file to a java
 * object
 */
public class Customer {

	private String name;
	private String[] wishList;
	private PurchaseSchedule[] purchaseSchedule;

	/**
	 * Returns the name of the customer
	 * 
	 * @return name of the customer
	 */
	public String getName() {
		return name;
	}

	/**
	 * returns the wishList array
	 * 
	 * @return the wishList array
	 */
	public String[] getWishList() {
		return wishList;
	}

	/**
	 * returns the purchase schedule array
	 * 
	 * @return the purchase schedule array
	 */
	public PurchaseSchedule[] getPurchaseSchedule() {
		return purchaseSchedule;
	}
}
