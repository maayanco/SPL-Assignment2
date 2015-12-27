package bgu.spl.app;

public class Service {
	private Time time;
	private Manager manager;
	private int factories;
	private int sellers;
	private Customer[] customers;
	
	public Time getTime(){
		return time;
	}
	
	public Manager getManager(){
		return manager;
	}
	
	public int getFactories(){
		return factories;
	}
	
	public int getSellers(){
		return sellers;
	}
	
	public Customer[] getCustomers(){
		return customers;
	}
}
