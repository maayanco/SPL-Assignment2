package bgu.spl.app.passive;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The store is a thread - safe Singleton class. It holds a collection of ShoeStorageInfo:  
 * One for each shoe type the store offers.
 * In addition, it contains a list of receipts issued to and by the store.
 */
public class Store {
	
	private static final String TYPE_DISCOUNT="discount";
	private static final String TYPE_REGULAR_AMOUNT="regular";
	
	private LinkedList<ShoeStorageInfo> shoeStorageList;
	private ConcurrentLinkedQueue<Receipt> receiptList;
	
	private static class SingletonHolder {
        private static Store storeInstance = new Store();
    }
	
	/**
	 * Returns an instance of the class
	 * @return
	 */
	public static Store getInstance() {
        return SingletonHolder.storeInstance;
    }
	
	/**
	 * Constructs a store object
	 */
	public Store(){
		shoeStorageList = new LinkedList<ShoeStorageInfo>();
		receiptList = new ConcurrentLinkedQueue<Receipt>();
	}
	
	/**
	 * Receives an array of ShoeStorageInfo items, and adds them to the shoe storage list
	 * @param storage - array of ShoeStorageInfo
	 */
	public void load (ShoeStorageInfo[] storage){
		shoeStorageList.clear();
		for(ShoeStorageInfo item : storage){
			add(item.getShoeType(), item.getAmountOnStorage());
		}
	}
	
	/**
	 * Adds a new shoe with the shoeType and amount parameters to the shoe storage list
	 * @param shoeType - the type of shoe
	 * @param amount - the amount of shoes to be on stock
	 */
	public void add(String shoeType, int amount){
		updateAmount(shoeType, amount, TYPE_REGULAR_AMOUNT);
	}
	
	/**
	 * Converts shoes of the required shoe type to be on discount.
	 * This method can only convert existing shoes and not add new shoes to the stock.
	 * @param shoeType - the type of shoes to be on discount
	 * @param amount - the amount of shoes to be on discount
	 */
	public void addDiscount(String shoeType, int amount){
		updateAmount(shoeType, amount, TYPE_DISCOUNT);
	}
	
	/**
	 * Updates the amount of shoes - according to typeOfOperation it either adds a new shoe
	 * or converts an existing shoe to be on discount.
	 * @param shoeType - the type of shoe
	 * @param amountToAdd - the amount of shoes to add
	 * @param typeOfOperation - indicate whether the method should add a new shoe to the stock
	 *     or convert a shoe to be on discount 
	 */
	private void updateAmount(String shoeType,int amountToAdd, String typeOfOperation){
		ShoeStorageInfo locatedShoe = locateShoeInStorage(shoeType);
		
			if(locatedShoe==null){
				/* Adding the shoe to the storage, which is synchronized so that
				no other thread will perform a modification at the same time */
				synchronized(shoeStorageList){
					shoeStorageList.add(new ShoeStorageInfo(shoeType, 0, 0));
				}
				locatedShoe=locateShoeInStorage(shoeType);
			}
			
			synchronized(locatedShoe){
				/* We modify the amount of the locatedShoe and therefore we synchronize the
				 * locatedShoe object so that no other thread will modify this same object*/
				if(typeOfOperation.equals(TYPE_REGULAR_AMOUNT)){
					int currentAmount = locatedShoe.getAmountOnStorage();
					locatedShoe.setAmountOnStorage(currentAmount+amountToAdd);
				}
				else if(typeOfOperation.equals(TYPE_DISCOUNT)){
					int currentAmount=locatedShoe.getAmountOnStorage();
					int currentDiscountedAmount = locatedShoe.getDiscountedAmount();
					int amountOfDiscountAfterAddition = currentDiscountedAmount+amountToAdd;
					locatedShoe.setDiscountedAmount(Math.min(amountOfDiscountAfterAddition, currentAmount));
				}
			}
	}
	
	/**
	 * 
	 * @param shoeType - the type of the shoe we are searching
	 * @return the located shoe, or Null if the shoe was not located
	 */
	private ShoeStorageInfo locateShoeInStorage(String shoeType){
		synchronized(shoeStorageList){
			for(ShoeStorageInfo shoeInStorage : shoeStorageList){
				if(shoeInStorage.getShoeType().equals(shoeType))
					return shoeInStorage;
			}
		}
		
		return null;
	}
	
	/**
	 * Attempts to locate and return a shoe of the specified parameters, if not found returns
	 * appropriate status message.
	 * @param shoeType - the type of the shoe to be taken 
	 * @param onlyDiscount - indicates whether the shoe we want to take should be discounted or not
	 * @return - BuyResult object indicating the status of take attempt 
	 */
	public BuyResult take(String shoeType, boolean onlyDiscount){
		ShoeStorageInfo locatedShoe=locateShoeInStorage(shoeType);
		
		if(locatedShoe==null){
			synchronized(shoeStorageList){
				/* Adding the shoe to the storage, which is synchronized so that
				 * no other thread will perform a modification at the same time */
				shoeStorageList.add(new ShoeStorageInfo(shoeType, 0, 0));
			}
			locatedShoe=locateShoeInStorage(shoeType);
		}
		
		synchronized(locatedShoe){
			/* We modify the amount of the locatedShoe and therefore we synchronize the
			 * locatedShoe object so that no other thread will modify this same object*/
			if(onlyDiscount && locatedShoe.getDiscountedAmount()==0){ 
				return BuyResult.NOT_ON_DISCOUNT;
			}
			else if(locatedShoe.getAmountOnStorage()==0){
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
		}
		
		return null;
		
	}
	
	/**
	 * Inserts the provided receipt to the list of receipts
	 * @param receipt - a receipt created after manufacturing or  selling of shoes
	 */
	public void file(Receipt receipt){
		receiptList.add(receipt);
	}
	
	/**
	 * Prints all the shoes in the store
	 */
	private void printShoes(){
		System.out.println(" ");
		System.out.println("-------------------Shoes In Stock:-------------------");
		synchronized(shoeStorageList){
			for(ShoeStorageInfo item : shoeStorageList){
				System.out.println("--------- Type: "+item.getShoeType());
				System.out.println("--------- Amount: "+item.getAmountOnStorage());
				System.out.println("--------- Discounted amount: "+item.getDiscountedAmount());
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			}
		}
		System.out.println(" ");
	}
	
	/**
	 * Prints all the receipts in the store
	 */
	private void printReceipts(){
		System.out.println("-------------------Receipts:-------");
		int index=1;
		synchronized(receiptList){
			for(Receipt receipt : receiptList){
				System.out.println("--------- receipt num: "+index);
				System.out.println("--------- seller: "+receipt.getSeller());
				System.out.println("--------- customer: "+receipt.getCustomer());
				System.out.println("--------- shoe type: "+receipt.getShoeType());
				System.out.println("--------- discount: "+receipt.isDiscount());
				System.out.println("--------- issued tick: "+receipt.getIssuedTick());
				System.out.println("--------- Request tick: "+receipt.getRequestTick());
				System.out.println("--------- Amount sold: "+receipt.getAmountSold());
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				index++;
			}
		}
	}
	
	/**
	 * Prints all the shoes in stock and the receipts
	 */
	public void print(){
		printShoes();
		printReceipts();
	}
	
	
}
