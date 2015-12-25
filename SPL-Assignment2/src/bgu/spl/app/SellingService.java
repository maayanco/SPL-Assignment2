package bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class SellingService extends MicroService{

	private Store storeInstance = Store.getInstance();
	private int currentTick;
	private CountDownLatch latchObject;
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	public SellingService(String name, CountDownLatch latchObject) {
		super(name);
		
		log.log(Level.INFO, "in selling service");
		
		currentTick=1; //should i? 
		this.latchObject=latchObject;
		latchObject.countDown();
	}

	@Override
	protected void initialize() {
		
		log.log(Level.INFO, "in selling service");
		
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
		});
		
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			log.log(Level.INFO, "i got a purchase order!! ");
			BuyResult res = storeInstance.take(req.getShoeType(), req.isDiscount()); //try to take from store
			if(res.equals(BuyResult.DISCOUNTED_PRICE) || res.equals(BuyResult.REGULAR_PRICE)){
			Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), req.isDiscount(), currentTick, req.getRequestTick(), req.getAmountSold());
				storeInstance.file(receipt);
				complete(req,receipt);
			}
			else if(res.equals(BuyResult.NOT_ON_DISCOUNT)){
				complete(req,null);
			}
			else if(res.equals(BuyResult.NOT_IN_STOCK)){
				RestockRequest requestToRestock = new RestockRequest(req.getShoeType(),1, req.getRequestTick());
				sendRequest(requestToRestock, reqq -> {
					if(reqq){
						//*********** not sure about this receipt thing... was not explicitly written in the assignment guidlines
						Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), req.isDiscount(), currentTick, req.getRequestTick(), req.getAmountSold());
						complete(req, receipt);
					}
					else{
						complete(req, null);
					}
				});
			}
				
        });
		
		subscribeBroadcast(TerminationBroadcast.class, req -> {
			log.log(Level.INFO, "terminating mee ");
			if(req.getToTerminate()==true){
				latchObject.countDown();
				terminate();
			}
		});
		
	}

}
