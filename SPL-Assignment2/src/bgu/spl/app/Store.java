package bgu.spl.app;

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
		for(ShoeStorageInfo item : storage){
			shoeStorageList.add(item);
		}
	}
	
	
	public BuyResult take(String shoeType, boolean onlyDiscount){
		//Go over the shoStorageArr
		ShoeStorageInfo locatedShoe = null;
		for(ShoeStorageInfo item : shoeStorageList){
			if(item.getShoeType().equals(shoeType))
				locatedShoe = item;
		}
		 
		if(locatedShoe==null) //There was no shoe found of the requested type
			return BuyResult.NOT_IN_STOCK;
		
		else if(onlyDiscount && (locatedShoe.getDiscountedAmount()==0))
			return BuyResult.NOT_ON_DISCOUNT;
		
		else if(onlyDiscount && (locatedShoe.getDiscountedAmount()>1)){
			int currentDiscountedAmount = locatedShoe.getDiscountedAmount();
			locatedShoe.setDiscountedAmount(currentDiscountedAmount-1);
			return BuyResult.DISCOUNTED_PRICE;
		}
		
		else if(!onlyDiscount && (locatedShoe.getAmountOnStorage()>1)){
			int currentAmount = locatedShoe.getAmountOnStorage();
			locatedShoe.setAmountOnStorage(currentAmount-1);
			return BuyResult.REGULAR_PRICE;
		}
		
		return null;
			
	}
	
	public void add(String shoeType, int amount){
		updateAmount(shoeType,amount,TYPE_REGULAR_AMOUNT);
	}
	
	public void addDiscount(String shoeType, int amount){
		updateAmount(shoeType,amount,TYPE_DISCOUNT);
	}
	
	private void updateAmount(String shoeType,int amountToAdd, String type){
		ShoeStorageInfo locatedShoe=null;
		for(ShoeStorageInfo item : shoeStorageList){
			if(item.getShoeType().equals(shoeType)){
				locatedShoe=item;
			}
		}
		
		if(locatedShoe==null){
			shoeStorageList.add(new ShoeStorageInfo(shoeType, 0, 0));
		}
		else if(type.equals(TYPE_REGULAR_AMOUNT)){
			int currentAmount = locatedShoe.getAmountOnStorage();
			locatedShoe.setAmountOnStorage(currentAmount+amountToAdd);
		}
		else if(type.equals(TYPE_DISCOUNT)){
			int currentDiscountedAmount=locatedShoe.getDiscountedAmount();
			locatedShoe.setDiscountedAmount(currentDiscountedAmount+amountToAdd);
		}
	}
	
	public void file(Receipt receipt){
		receiptList.add(receipt);
	}
	
	public void print(){
		System.out.println("Shoes:");
		for(ShoeStorageInfo item : shoeStorageList){
			System.out.println("item: type- "+item.getShoeType()+" amount: "
											 +item.getAmountOnStorage()+" discounted amount: "
											 +item.getDiscountedAmount());
		}
		
		System.out.println("Receipts:");
		for(Receipt item : receiptList){
			System.out.println("item:"+"seller: "+item.getSeller()
												 +"customer: "+item.getCustomer()
												 +"shoe type: "+item.getShoeType()
												 +"discount: "+item.isDiscount()
												 +"issued tick: "+item.getIssuedTick()
												 +"request tick: "+item.getRequestTick()
												 +"amount sold: "+item.getAmountSold());
		}
	}
	
	
}
