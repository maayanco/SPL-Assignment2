package bgu.spl.app;

import bgu.spl.app.DiscountSchedule;

public class StoreConfiguration {
	private Storage[] initialStorage;
	private Service services;
	
	public Service getServices(){
		return services;
	}
	
	public Storage[] getInitialStorage(){
		return initialStorage;
	}


	 @Override
	    public String toString() {
	        return "services:"+services+"initial storage:"+initialStorage;
	    }
}


