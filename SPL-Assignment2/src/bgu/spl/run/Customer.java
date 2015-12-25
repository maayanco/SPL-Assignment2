package bgu.spl.run;

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
