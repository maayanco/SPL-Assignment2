package bgu.spl.app;

import bgu.spl.mics.Broadcast;

//lital

public class NewDiscountBroadcast implements Broadcast{
	private String shoeType;
	private int amountOnSale;
	/*private int issuedTick; //really not sure we need this
*/	
	public  NewDiscountBroadcast(String shoeType, int amountOnSale/*, int issuedTick*/){
		this.shoeType=shoeType;
		this.amountOnSale=amountOnSale;
		/*this.issuedTick=issuedTick;*/
	}
	public String getShoeType() {
		return shoeType;
	}
	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}
	public int getAmountOnSale() {
		return amountOnSale;
	}
	public void setAmountOnSale(int amountOnSale) {
		this.amountOnSale = amountOnSale;
	}
/*	public int getIssuedTick() {
		return issuedTick;
	}
	public void setIssuedTick(int issuedTick) {
		this.issuedTick = issuedTick;
	}
*/
}
