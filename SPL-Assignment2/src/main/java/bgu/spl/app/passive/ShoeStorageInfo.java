package bgu.spl.app.passive;

/**
 * An object which represents information about a single type of shoe in the
 * store (e.g., red-sneakers,blue-sandals, etc.)
 */
public class ShoeStorageInfo {
	private String shoeType;
	private int amountOnStorage;
	private int discountedAmount;

	/**
	 * 
	 * @param shoeType
	 *            - the shoe type
	 * @param amountOnStorage
	 *            - the amount of shoes on storage
	 * @param discountedAmount
	 *            - the amount of shoes on discount
	 */
	public ShoeStorageInfo(String shoeType, int amountOnStorage, int discountedAmount) {
		this.shoeType = shoeType;
		this.amountOnStorage = amountOnStorage;
		this.discountedAmount = discountedAmount;
	}

	/**
	 * @return - the shoe type
	 */
	public String getShoeType() {
		return this.shoeType;
	}

	/**
	 * @return - the amount of shoes on storage
	 */
	public int getAmountOnStorage() {
		return this.amountOnStorage;
	}

	/**
	 * @return - the amount of shoes on discount
	 */
	public int getDiscountedAmount() {
		return this.discountedAmount;
	}

	/**
	 * Set the shoe type
	 * 
	 * @param shoeType
	 *            - the type of the shoe
	 */
	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}

	/**
	 * Set the amount on storage
	 * 
	 * @param amountOnStorage
	 *            - the amount of shoes
	 */
	public void setAmountOnStorage(int amountOnStorage) {
		this.amountOnStorage = amountOnStorage;
	}

	/**
	 * Set the discounted amount
	 * 
	 * @param discountedAmount
	 *            - the amount of shoes to be on discount
	 */
	public void setDiscountedAmount(int discountedAmount) {
		this.discountedAmount = discountedAmount;
	}
}
