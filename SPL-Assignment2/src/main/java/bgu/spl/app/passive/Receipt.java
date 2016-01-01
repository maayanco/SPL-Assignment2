package bgu.spl.app.passive;

/**
 * An object representing a receipt that should be sent to a client after buying
 * a shoe (when the clients PurchaseRequest completed).
 */
public class Receipt {

	private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;

	/**
	 * 
	 * @param seller
	 *            - the seller of the shoe
	 * @param customer
	 *            - the customer to which the shoes are sold
	 * @param shoeType
	 *            - the shoe type
	 * @param discount
	 *            - true if the purchase was at a discount, false otherwise.
	 * @param issuedTick
	 *            - the tick at which the purchase was completed
	 * @param requestTick
	 *            - the tick at which the purchase was initiated by the customer
	 * @param amountSold
	 *            - the amount of shoes sold
	 */
	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick,
			int amountSold) {
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}

	/**
	 * @return - the seller of the shoe
	 */
	public String getSeller() {
		return seller;
	}

	/**
	 * @return - the customer to which the shoes are sold
	 */
	public String getCustomer() {
		return customer;
	}

	/**
	 * @return - the shoe type
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * @return - true - if the purchase was at a discount, false - otherwise.
	 */
	public boolean isDiscount() {
		return discount;
	}

	/**
	 * @return - the tick at which the purchase was completed
	 */
	public int getIssuedTick() {
		return issuedTick;
	}

	/**
	 * @return - the tick at which the purchase was initiated by the customer
	 */
	public int getRequestTick() {
		return requestTick;
	}

	/**
	 * @return - the amount of shoes sold
	 */
	public int getAmountSold() {
		return amountSold;
	}

}
