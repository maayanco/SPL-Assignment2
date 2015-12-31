package bgu.spl.app.json;

/**
 * Describes a Store Configuration type, the main json DataType. Used for the
 * conversion between a json file to a java object.
 */
public class StoreConfiguration {
	private Storage[] initialStorage;
	private Service services;

	/**
	 * 
	 * @return the services object
	 */
	public Service getServices() {
		return services;
	}

	/**
	 * 
	 * @return the initial storage array
	 */
	public Storage[] getInitialStorage() {
		return initialStorage;
	}

}
