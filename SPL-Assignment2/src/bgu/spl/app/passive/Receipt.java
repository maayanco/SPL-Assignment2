package bgu.spl.app.passive;

public class Receipt {
	private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;
	
	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick, int amountSold){
		this.seller=seller;
		this.customer=customer;
		this.shoeType=shoeType;
		this.discount=discount;
		this.issuedTick=issuedTick;
		this.requestTick=requestTick;
		this.amountSold=amountSold;
		
	}

	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
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

	public int getIssuedTick() {
		return issuedTick;
	}

	public void setIssuedTick(int issuedTick) {
		this.issuedTick = issuedTick;
	}

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
