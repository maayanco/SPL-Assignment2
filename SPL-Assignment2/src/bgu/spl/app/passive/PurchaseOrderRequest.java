package bgu.spl.app.passive;

import bgu.spl.mics.Request;

//not real!!

public class PurchaseOrderRequest implements Request<Receipt> {
	
	//private String seller; //i don't think this is something we need!! we need to remove this
	private String customer; // 
	private String shoeType;
	private boolean discount;
	//private int issuedTick;
	private int requestTick;
	private int amountSold;
	
	public PurchaseOrderRequest(String customer,String shoeType,boolean discount,int requestTick, int amountSold) {
		//this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount=discount;
		//this.issuedTick=issuedTick;
		this.requestTick=requestTick;
		this.amountSold=amountSold;

	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getShoeType() {
		return shoeType;
	}

	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}

	public boolean isDiscount() {
		return discount;
	}

	public void setDiscount(boolean discount) {
		this.discount = discount;
	}

/*	public int getIssuedTick() {
		return issuedTick;
	}

	public void setIssuedTick(int issuedTick) {
		this.issuedTick = issuedTick;
	}
*/
	public int getRequestTick() {
		return requestTick;
	}

	public void setRequestTick(int requestTick) {
		this.requestTick = requestTick;
	}

	public int getAmountSold() {
		return amountSold;
	}

	public void setAmountSold(int amountSold) {
		this.amountSold = amountSold;
	}



}
