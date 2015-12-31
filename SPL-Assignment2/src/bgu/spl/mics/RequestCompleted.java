package bgu.spl.mics;

/**
 * A message type that indicates a completion of a request with a certain
 * result.
 * 
 * @param <T>
 *            - the type of the result
 */
public class RequestCompleted<T> implements Message {

	private Request<T> completed;
	private T result;

	/**
	 * 
	 * @param completed
	 *            - the request that was completed
	 * @param result
	 *            - the result of the request that was completed
	 */
	public RequestCompleted(Request<T> completed, T result) {
		this.completed = completed;
		this.result = result;
	}

	/**
	 * 
	 * @return the request that was completed
	 */
	@SuppressWarnings("rawtypes")
	public Request getCompletedRequest() {
		return completed;
	}

	/**
	 * 
	 * @return the result of the current RequestCompleted
	 */
	public T getResult() {
		return result;
	}
}
