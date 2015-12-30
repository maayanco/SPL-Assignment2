package bgu.spl.app.json;

/**
 * Describes a Service type.
 * Used for the conversion from json file to java object.
 *
 */
public class Service {
	
	private Time time;
	private Manager manager;
	private int factories;
	private int sellers;
	private Customer[] customers;
	
	/**
	 * @return - the time object
	 */
	public Time getTime(){
		return time;
	}
	
	/**
	 * @return - the manager object
	 */
	public Manager getManager(){
		return manager;
	}
	
	/**
	 * @return - the amount of factories
	 */
	public int getFactories(){
		return factories;
	}
	
	/**
	 * @return - the amount of sellers
	 */
	public int getSellers(){
		return sellers;
	}
	
	/**
	 * @return - an array of customers
	 */
	public Customer[] getCustomers(){
		return customers;
	}
}
