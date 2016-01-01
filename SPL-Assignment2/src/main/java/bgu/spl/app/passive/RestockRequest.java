package bgu.spl.app.passive;

import bgu.spl.mics.Request;

/**
 * Request that is sent by the selling service to the store manager so that he
 * will know that he need to order new shoes from a factory.
 * <p>
 * Its response type expected to be a boolean where the result: true means that
 * the order is complete and the shoe is reserved for the selling service and
 * the result: false means that the shoe cannot be ordered (because there were
 * no factories available).
 *
 */
public class RestockRequest implements Request<Boolean> {

	private String shoeType;
	private int amountRequested;
	private int initialRequestTick;

	/**
	 * 
	 * @param shoeType
	 *            - the shoe type requested
	 * @param amountRequested
	 *            - the amount of shoes requested
	 * @param initialRequestTick
	 *            - the tick at which the initial purchase request was
	 *            initialized
	 */
	public RestockRequest(String shoeType, int amountRequested, int initialRequestTick) {
		this.shoeType = shoeType;
		this.amountRequested = amountRequested;
		this.initialRequestTick = initialRequestTick;
	}

	/**
	 * 
	 * @return - the shoe type requested
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * 
	 * @return - the amount of shoes requested
	 */
	public int getAmountRequested() {
		return amountRequested;
	}

	/**
	 * 
	 * @return - the tick at which the initial purchase request was initialized
	 */
	public int getInitialRequestTick() {
		return initialRequestTick;
	}
}
