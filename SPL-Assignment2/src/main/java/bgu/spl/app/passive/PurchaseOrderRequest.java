package bgu.spl.app.passive;

import bgu.spl.mics.Request;

/**
 * Request that is sent when the a store client wish to buy a shoe. Its response
 * type expected to be a Receipt. On the case the purchase was not completed
 * successfully null should be returned as the request result.
 */
public class PurchaseOrderRequest implements Request<Receipt> {

	private String customer;
	private String shoeType;
	private boolean discount;
	private int requestTick;
	private int amountSold;

	/**
	 * 
	 * @param customer
	 *            - the customer name
	 * @param shoeType
	 *            - the shoe type to be purchased
	 * @param discount
	 *            - true if the purchase should be at discount, false otherwise.
	 * @param requestTick
	 *            - the tick at which the purchase request was issued by the
	 *            customer
	 * @param amountSold
	 *            - The amount of shoes to be sold
	 */
	public PurchaseOrderRequest(String customer, String shoeType, boolean discount, int requestTick, int amountSold) {
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}

	/**
	 * @return - the customer name
	 */
	public String getCustomer() {
		return customer;
	}

	/**
	 * @return - the shoe type to be purchased
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return - true if the purchase should be at discount, false otherwise.
	 */
	public boolean isDiscount() {
		return discount;
	}

	/**
	 * @return - the tick at which the purchase request was issued by the
	 *         customer
	 */
	public int getRequestTick() {
		return requestTick;
	}

	/**
	 * Note that the amount should be 1
	 * 
	 * @return - The amount of shoes to be sold
	 */
	public int getAmountSold() {
		return amountSold;
	}

}
