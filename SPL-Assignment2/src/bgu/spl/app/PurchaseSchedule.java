package bgu.spl.app;

import bgu.spl.mics.Request;

//lital

//should not be changed, fields were provided like this..

public class PurchaseSchedule {
	private  String shoeType;
	private int tick;
	
	public PurchaseSchedule(String shoeType,int tick) {
		this.shoeType = shoeType;
		this.tick = tick;

	}

	public String getShoeType() {
		return shoeType;
	}

	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}

	public int getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}
	

}
