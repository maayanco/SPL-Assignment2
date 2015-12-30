package bgu.spl.app.passive;

public class ShoeStorageInfo {
	private String shoeType;
	private int amountOnStorage;
	private int discountedAmount;
	
	public ShoeStorageInfo(String shoeType, int amountOnStorage, int discountedAmount){
		this.shoeType=shoeType;
		this.amountOnStorage=amountOnStorage;
		this.discountedAmount=discountedAmount; 
	}
	
	public String getShoeType(){
		return this.shoeType;
	}
	
	public int getAmountOnStorage(){
		return this.amountOnStorage;
	}
	
	public int getDiscountedAmount(){
		return this.discountedAmount;
	}
	
	public void setShoeType(String shoeType){
		this.shoeType=shoeType;
	}
	
	public void setAmountOnStorage(int amountOnStorage){
		this.amountOnStorage=amountOnStorage;
	}
	
	public void setDiscountedAmount(int discountedAmount){
		this.discountedAmount=discountedAmount;
	}
}
