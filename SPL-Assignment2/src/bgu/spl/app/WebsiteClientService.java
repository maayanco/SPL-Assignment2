package bgu.spl.app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class WebsiteClientService extends MicroService {
	
	private Map<Integer, PurchaseSchedule> purchaseSchedule;
	private Set<String> wishList;
	private int currentTick;
	private CountDownLatch latchObject;
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseScheduleList, Set<String> wishListItems, CountDownLatch latchObject) {
		
		super(name);
		

		log.log(Level.INFO, "");
		
		//Initialize the purchaseSchedule map and clone the received list
		purchaseSchedule = new HashMap<Integer, PurchaseSchedule>();
		for(PurchaseSchedule item : purchaseScheduleList){
			this.purchaseSchedule.put(item.getTick(), item);
		}
		
		//Initialize the wishList and clone it
		wishList = new HashSet<String>();
		for(String item : wishListItems){
			this.wishList.add(item);
		}
		
		this.latchObject=latchObject;
		latchObject.countDown();
		
	}
	
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick=req.getTick();
			
			if(purchaseSchedule.containsKey(currentTick)){//then we need to send a purchaseOrder
				PurchaseSchedule purchaseRequestInfo = purchaseSchedule.get(currentTick);
				purchaseSchedule.remove(purchaseRequestInfo); 
				PurchaseOrderRequest purchaseRequest = new PurchaseOrderRequest(this.getName(), purchaseRequestInfo.getShoeType(),false,currentTick, 1);
				sendRequest(purchaseRequest, reqq -> {//should i check the content of the reqq??? check for null
					complete(purchaseRequest,reqq); //TODO: WHAT???? Is this what supposed to happen??
				});
			}
			if(wishList.isEmpty() && purchaseSchedule.isEmpty()){
				latchObject.countDown();
				terminate();
			}
		});
		
		subscribeBroadcast(NewDiscountBroadcast.class, req -> {
			log.log(Level.INFO, "in client, subscribeBroadcast NewDiscount!!!");
			if(wishList.contains(req.getShoeType())){
				PurchaseOrderRequest purchaseRequest = new PurchaseOrderRequest(this.getName() , req.getShoeType(), true ,currentTick, 1);
				sendRequest(purchaseRequest, reqq -> {
					complete(purchaseRequest, reqq); //TODO: WHAT?? IS THIS WHAT SUPPOSED TO HAPPEN?
				});
				wishList.remove(req.getShoeType());
			}
			
			if(wishList.isEmpty() && purchaseSchedule.isEmpty()){
				latchObject.countDown();
				terminate();
			}
			
		});
		
		subscribeBroadcast(TerminationBroadcast.class, req -> {
			if(req.getToTerminate()==true){
				latchObject.countDown();
				terminate();
			}
		});
		
	}

}
