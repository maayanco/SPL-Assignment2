package bgu.spl.app;

import bgu.spl.app.PurchaseSchedule;

public class Customer {
	private String name;
	private String[] wishList;
	private PurchaseSchedule[] purchaseSchedule;
	
	
	public String getName(){
		return name;
	}
	
	public String[] getWishList(){
		return wishList;
	}
	
	public PurchaseSchedule[] getPurchaseSchedule(){
		return purchaseSchedule;
	}
}
