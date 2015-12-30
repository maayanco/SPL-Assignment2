package bgu.spl.app.passive;

import bgu.spl.mics.Request;

public class RestockRequest implements Request<Boolean> {
	
	private String shoeType;
	private int amountRequested; //not certain we need this field..
	private int initialRequestTick; //when the purchase order request was issued
	
	public RestockRequest(String shoeType, int amountRequested, int initialRequestTick){
		this.shoeType=shoeType;
		this.amountRequested=amountRequested;
		this.initialRequestTick=initialRequestTick;
	}
	
	public int getAmountRequested() {
		return amountRequested;
	}

	public void setAmountRequested(int amountRequested) {
		this.amountRequested = amountRequested;
	}

	public String getShoeType() {
		return shoeType;
	}
	
	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}
	
	public int getInitialRequestTick(){
		return initialRequestTick;
	}
}
