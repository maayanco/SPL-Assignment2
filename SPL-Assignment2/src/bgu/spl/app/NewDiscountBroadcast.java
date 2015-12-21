package bgu.spl.app;

import bgu.spl.mics.Broadcast;

//lital

public class NewDiscountBroadcast implements Broadcast{
	private String shoeType;
	public  NewDiscountBroadcast(String shoeType){
		this.shoeType=shoeType;
		
	}
	public String getShoeType() {
		return shoeType;
	}
	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}

}
