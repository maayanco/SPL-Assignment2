package bgu.spl.app.passive;

import java.util.LinkedList;

public class Store {
	
	private static final String TYPE_DISCOUNT="discount";
	private static final String TYPE_REGULAR_AMOUNT="regular";
	
	private LinkedList<ShoeStorageInfo> shoeStorageList;
	private LinkedList<Receipt> receiptList;
	
	private static class SingletonHolder {
        private static Store instance = new Store();
    }
	
	public static Store getInstance() {
        return SingletonHolder.instance;
    }
	
	public Store(){
		shoeStorageList = new LinkedList<ShoeStorageInfo>();
		receiptList = new LinkedList<Receipt>();
	}
	
	public void load (ShoeStorageInfo[] storage){
		shoeStorageList.clear();
		for(ShoeStorageInfo item : storage){
			shoeStorageList.add(item);
		}
	}
	
	
	/*public BuyResult take(String shoeType, boolean onlyDiscount){
		
		//Go over the shoStorageArr
		ShoeStorageInfo locatedShoe = null;
		for(ShoeStorageInfo item : shoeStorageList){
			if(item.getShoeType().equals(shoeType))
				locatedShoe = item;
		}
		
		
		//DEBUUUG TO FIND OUT WHY ARE WE RETURNING NULL
		System.out.println("HELLLLO THIS IS THE STORE - WE ARE ABOUT TO RETURN NULL. You wanted show type: "+shoeType+" discount status: "+onlyDiscount);
		if(locatedShoe==null)
			System.out.println("we haven't located your shoeType");
		else{
			System.out.println(" the params of the located shoe are: amountOnStorage:"+locatedShoe.getAmountOnStorage()+" type: "+locatedShoe.getShoeType()+" discountedAmount :"+locatedShoe.getDiscountedAmount());
		}
		System.out.println("what we have is:");
		
		printShoes();
		return null;
			
	}*/
	
	public void add(String shoeType, int amount){
		updateAmount(shoeType,amount,TYPE_REGULAR_AMOUNT);
	}
	
	public void addDiscount(String shoeType, int amount){
		updateAmount(shoeType,amount,TYPE_DISCOUNT);
	}
	
	private ShoeStorageInfo locateShoeInStorage(String shoeType){
		for(ShoeStorageInfo item : shoeStorageList){
			if(item.getShoeType().equals(shoeType))
				return item;
		}
		return null;
	}
	
	public BuyResult take(String shoeType, boolean onlyDiscount){
		ShoeStorageInfo locatedShoe=locateShoeInStorage(shoeType);
		
		if(locatedShoe==null){
			shoeStorageList.add(new ShoeStorageInfo(shoeType, 0, 0));
			locatedShoe=locateShoeInStorage(shoeType);
		}
		
		 //locatedShoe can't be null now!
		if(onlyDiscount && locatedShoe.getDiscountedAmount()==0){ 
			return BuyResult.NOT_ON_DISCOUNT;
		}
		else if(locatedShoe.getAmountOnStorage()==0){
			System.out.println("debuuug- store is returning Not In Stock, the shoe you wanted: "+shoeType+" the discount status you wanted: "+onlyDiscount+" do we have this shoe: "+locatedShoe+" and the amount we have: "+locatedShoe.getAmountOnStorage()+" and the discounted amount we have of it: "+locatedShoe.getDiscountedAmount());
			return BuyResult.NOT_IN_STOCK;
		}
		else if(onlyDiscount && locatedShoe.getDiscountedAmount()>0){
			int currentDiscountedAmount = locatedShoe.getDiscountedAmount();
			int currentAmount = locatedShoe.getAmountOnStorage();
			locatedShoe.setDiscountedAmount(currentDiscountedAmount-1);
			locatedShoe.setAmountOnStorage(currentAmount-1);
			return BuyResult.DISCOUNTED_PRICE;
		}
		
		else if(!onlyDiscount && locatedShoe.getAmountOnStorage()>0){
			int currentDiscountedAmount = locatedShoe.getDiscountedAmount();
			int currentAmount = locatedShoe.getAmountOnStorage();
			if(currentDiscountedAmount>0)
				locatedShoe.setDiscountedAmount(currentDiscountedAmount-1);
			locatedShoe.setAmountOnStorage(currentAmount-1);
			return BuyResult.REGULAR_PRICE;
		}
		
		 
		return null;
		
	}
	
	private void updateAmount(String shoeType,int amountToAdd, String type){
		
		ShoeStorageInfo locatedShoe=locateShoeInStorage(shoeType);
		
		if(locatedShoe==null){
			shoeStorageList.add(new ShoeStorageInfo(shoeType, 0, 0));
			locatedShoe=locateShoeInStorage(shoeType);
		}
		
		if(type.equals(TYPE_REGULAR_AMOUNT)){
			int currentAmount = locatedShoe.getAmountOnStorage();
			locatedShoe.setAmountOnStorage(currentAmount+amountToAdd);
		}
		else if(type.equals(TYPE_DISCOUNT)){
			int currentAmount=locatedShoe.getAmountOnStorage();
			int currentDiscountedAmount = locatedShoe.getDiscountedAmount();
			int amountOfDiscountAfterAddition = currentDiscountedAmount+amountToAdd;
			locatedShoe.setDiscountedAmount(Math.min(amountOfDiscountAfterAddition, currentAmount));
		}
	}
	
	public void file(Receipt receipt){
		receiptList.add(receipt);
	}
	
	private void printShoes(){
		System.out.println(" ");
		System.out.println("-------------------Shoes In Stock:-------------------");
		for(ShoeStorageInfo item : shoeStorageList){
			System.out.println("--------- Type: "+item.getShoeType());
			System.out.println("--------- Amount: "+item.getAmountOnStorage());
			System.out.println("--------- Discounted amount: "+item.getDiscountedAmount());
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		}
		
		System.out.println(" ");
		
	}
	
	public void printReceiptsInHtmlFormat(){
		System.out.println("-------------------Receipts:-------------------");
		System.out.println("<table border='1'>");
		int index=1;
		for(Receipt item : receiptList){
			System.out.println("<tr><td>");
			System.out.println("--------- receipt num: "+index+" <br>");
			System.out.println("--------- seller: "+item.getSeller()+" <br>");
			System.out.println("--------- customer: "+item.getCustomer()+" <br>");
			System.out.println("--------- shoe type: "+item.getShoeType()+" <br>");
			System.out.println("--------- discount: "+item.isDiscount()+" <br>");
			System.out.println("--------- issued tick: "+item.getIssuedTick()+" <br>");
			System.out.println("--------- Request tick: "+item.getRequestTick()+" <br>");
			System.out.println("--------- Amount sold: "+item.getAmountSold()+" <br>");
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			index++;
			System.out.println("</td></tr>");
		}
		System.out.println("</table>");
	}
	
	
	private void printReceipts(){
		System.out.println("-------------------Receipts:-------------------");
		int index=1;
		for(Receipt item : receiptList){
			System.out.println("<tr><td>");
			System.out.println("--------- receipt num: "+index);
			System.out.println("--------- seller: "+item.getSeller());
			System.out.println("--------- customer: "+item.getCustomer());
			System.out.println("--------- shoe type: "+item.getShoeType());
			System.out.println("--------- discount: "+item.isDiscount());
			System.out.println("--------- issued tick: "+item.getIssuedTick());
			System.out.println("--------- Request tick: "+item.getRequestTick());
			System.out.println("--------- Amount sold: "+item.getAmountSold());
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			index++;
		}
	}
	
	public void print(){
		printShoes();
		printReceipts();
	}
	
	
}