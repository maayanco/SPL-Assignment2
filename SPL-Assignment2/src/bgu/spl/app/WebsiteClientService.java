package bgu.spl.app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class WebsiteClientService extends MicroService {
	
	private Map<Integer, LinkedList<PurchaseSchedule>> purchaseScheduleWithList;
	private Set<String> wishList;
	private int currentTick;
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;
	
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseScheduleList, Set<String> wishListItems, CountDownLatch startLatchObject, CountDownLatch endLatchObject) {
		
		super(name);
		
		//Initialize the purchaseSchedule map and clone the received list
		purchaseScheduleWithList = new HashMap<Integer, LinkedList<PurchaseSchedule>>();
		for(PurchaseSchedule item : purchaseScheduleList){
			LinkedList<PurchaseSchedule> listOfPurchasesAtTick;
			
			if(purchaseScheduleWithList.containsKey(item.getTick())){
				listOfPurchasesAtTick = purchaseScheduleWithList.get(item.getTick());
			}
			else{
				listOfPurchasesAtTick = new LinkedList<PurchaseSchedule>();
			}
			
			listOfPurchasesAtTick.add(item);
			purchaseScheduleWithList.put(item.getTick(), listOfPurchasesAtTick);
		}
		
		//Initialize the wishList and clone it
		wishList = new HashSet<String>();
		for(String item : wishListItems){
			this.wishList.add(item);
		}
		
		this.startLatchObject=startLatchObject;
		startLatchObject.countDown();
		this.endLatchObject=endLatchObject;
		
		log.log(Level.INFO, getName()+" client service was initialized");
	}
	
	private void subscribeToTickBroadcast(){
		
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick=req.getTick();
			
			if(purchaseScheduleWithList.containsKey(currentTick)){
				LinkedList<PurchaseSchedule> listOfPurchasesAtCurrentTick = purchaseScheduleWithList.get(currentTick);
				for(PurchaseSchedule futurePurchase : listOfPurchasesAtCurrentTick){
					PurchaseOrderRequest purchaseRequest = new PurchaseOrderRequest(this.getName(), futurePurchase.getShoeType(),false,currentTick, 1);
					log.log(Level.INFO, getName()+" has sent a purchase request for "+purchaseRequest.getAmountSold()+" shoes of type "+purchaseRequest.getShoeType()+" at tick "+purchaseRequest.getRequestTick());
					sendRequest(purchaseRequest, reqq -> {//should i check the content of the reqq??? check for null
						// maybe this code should happen only if reqq!=null
							//Delete from data structs
							if(purchaseScheduleWithList.containsKey(currentTick)){
								LinkedList<PurchaseSchedule> listOfPurchasesAtTick = purchaseScheduleWithList.get(currentTick);
								for(PurchaseSchedule item: listOfPurchasesAtTick){
									wishList.remove(item.getShoeType());
								}
								listOfPurchasesAtTick.remove(futurePurchase);
								if(listOfPurchasesAtTick.isEmpty())
									purchaseScheduleWithList.remove(currentTick);
							}
							
						
						//complete(purchaseRequest,reqq); // this seems not good! //TODO: SEEMS LIKE THERE'S A BUG HERE
					});
					
				}
			}
			
			if(wishList.isEmpty() && purchaseScheduleWithList.isEmpty()){
				log.log(Level.INFO, getName() +" has completed all it's purchases and wishList and is now terminating..");
				System.out.println("CountDownLatch - counted down at "+getName());//debuuuug
				endLatchObject.countDown();
				terminate();
			}
		});
	}
	
	
	
	private void subscribeToNewDiscountBroadcast(){
		
		subscribeBroadcast(NewDiscountBroadcast.class, req -> {
			log.log(Level.INFO, getName()+" has received a new Discount Broadcast for "+req.getAmountOnSale()+" shoes of type "+req.getShoeType());
			if(wishList.contains(req.getShoeType())){
				PurchaseOrderRequest purchaseRequest = new PurchaseOrderRequest(this.getName() , req.getShoeType(), true ,currentTick, 1);
				sendRequest(purchaseRequest, reqq -> {
					log.log(Level.INFO, getName()+ " has sent a new purchase order for 1 shoe of type "+req.getShoeType());
					//if(reqq!=null){
					wishList.remove(req.getShoeType()); //TODO: Maybe this should be after the sendRequest
					//}
					//complete(purchaseRequest, reqq); //TODO: WHAT?? IS THIS WHAT SUPPOSED TO HAPPEN?
				});
				
			}
			
			if(wishList.isEmpty() && purchaseScheduleWithList.isEmpty()){
				log.log(Level.INFO, getName() +" has completed all it's purchases and wishList and is now terminating..");
				System.out.println("CountDownLatch - counted down at "+getName());//debuuuug
				endLatchObject.countDown();
				terminate();
			}
			
		});
	}
	
	private void subscribeToTerminationBroadcast(){
		subscribeBroadcast(TerminationBroadcast.class, req -> {
			if(req.getToTerminate()==true){
				log.log(Level.INFO, getName()+" has received a TerminationBroadcast and is terminating");
				System.out.println("CountDownLatch - counted down at "+getName());//debuuuug
				endLatchObject.countDown();
				terminate();
			}
		});
	}
	
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();
		subscribeToNewDiscountBroadcast();
		subscribeToTerminationBroadcast();
	}

}
