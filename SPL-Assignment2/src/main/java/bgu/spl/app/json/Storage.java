package bgu.spl.app.json;

/**
 * Describes a storage type. Used for the conversion from a json file to a java
 * object
 *
 */
public class Storage {

	private String shoeType;
	private int amount;

	/**
	 * 
	 * @return the type of the shoe
	 */
	public String getShoeType() {
		return shoeType;
	}

	/**
	 * 
	 * @return - the amount of shoes in the storage
	 */
	public int getAmount() {
		return amount;
	}
}
