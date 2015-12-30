package bgu.spl.app.passive;

import bgu.spl.mics.Request;

public class ManufacturingOrderRequest implements Request<Receipt> {
	
	private String shoeType;
	private int amount;
	private int initialRequestTick;
	
	public ManufacturingOrderRequest(String shoeType, int amount, int initialRequestTick){
		this.shoeType=shoeType;
		this.amount=amount;
		this.initialRequestTick=initialRequestTick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public int getInitialRequestTick(){
		return initialRequestTick;
	}
}
