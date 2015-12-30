package bgu.spl.app.services;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.passive.BuyResult;
import bgu.spl.app.passive.PurchaseOrderRequest;
import bgu.spl.app.passive.Receipt;
import bgu.spl.app.passive.RestockRequest;
import bgu.spl.app.passive.Store;
import bgu.spl.app.passive.TerminationBroadcast;
import bgu.spl.app.passive.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class SellingService extends MicroService{

	private Store storeInstance = Store.getInstance();
	private int currentTick;
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;
	
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	public SellingService(String name, CountDownLatch startLatchObject, CountDownLatch endLatchObject) {
		super(name);
		
		log.log(Level.INFO, getName()+" selling service was initialized");
		
		currentTick=1; 
		this.startLatchObject=startLatchObject;
		/*startLatchObject.countDown();*/
		this.endLatchObject=endLatchObject;
	}

	
	private void subscribeToTickBroadcast(){
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
		});

	}
	
	
	private void subscribeToPurchaseOrderRequest(){
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			log.log(Level.INFO, " has received a purchase request for "+req.getAmountSold()+" shoes of type "+req.getShoeType());
			BuyResult res = storeInstance.take(req.getShoeType(), req.isDiscount()); //try to take from store
			if(res.equals(BuyResult.DISCOUNTED_PRICE) || res.equals(BuyResult.REGULAR_PRICE)){ //EXCEPTION HERE
				log.log(Level.INFO, getName()+" has completed a purchase order by taking sucsessfully from the store ");
				Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), req.isDiscount(), currentTick, req.getRequestTick(), req.getAmountSold());
				storeInstance.file(receipt);
				complete(req,receipt);
			}
			else if(res.equals(BuyResult.NOT_ON_DISCOUNT)){
				System.out.println(getName()+"debuuug - Got Not In Stock");
				log.log(Level.INFO, getName()+" couldn't complete the purchase request because there were no items on discount in the stock as requested ");
				complete(req,null);
			}
			else if(res.equals(BuyResult.NOT_IN_STOCK)){
				System.out.println(getName()+"debuuug - Got Not In Stock");
				RestockRequest requestToRestock = new RestockRequest(req.getShoeType(),1, req.getRequestTick());
				sendRequest(requestToRestock, reqq -> {
					log.log(Level.INFO, getName()+" sent a request to restock ");
					if(reqq){
						Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), req.isDiscount(), currentTick, req.getRequestTick(), req.getAmountSold());
						storeInstance.file(receipt);
						log.log(Level.INFO, getName()+" has completed the purchase order for "+req.getAmountSold()+" shoe of type "+req.getShoeType()+" which was issued originaly at tick: "+req.getRequestTick()+" by the customer "+req.getCustomer());
						complete(req, receipt); ///Causes NullPointerExceptionnn
					}
					else{
						complete(req, null);
					}
				});
			}
				
        });
		
	}
	
	private void subscribeToTerminationBroadcast(){
		subscribeBroadcast(TerminationBroadcast.class, req -> {
			if(req.getToTerminate()==true){
				log.log(Level.INFO, getName()+" has received a termination broadcast and is terminating..");
				System.out.println("CountDownLatch - counted down at "+getName());//debuuuug
				endLatchObject.countDown();
				terminate();
			}
		});
	}
	
	
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();		
		subscribeToPurchaseOrderRequest();
		subscribeToTerminationBroadcast();
		startLatchObject.countDown();
		
	}

}
