package bgu.spl.app;

import bgu.spl.mics.Request;

public class RestockRequest implements Request<Boolean> {
	
	private String shoeType;
	private int amountRequested; //not certain we need this field..
	
	public RestockRequest(String shoeType, int amountRequested){
		this.shoeType=shoeType;
		this.amountRequested=amountRequested;
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
}
