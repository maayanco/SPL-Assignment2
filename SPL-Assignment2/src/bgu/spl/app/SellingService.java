package bgu.spl.app;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;

public class SellingService extends MicroService{

	private Store storeInstance = Store.getInstance();
	private int currentTick;
	
	public SellingService(String name) {
		super(name);
		
		currentTick=1; //should i? 
	}

	@Override
	protected void initialize() {
		
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
		});
		
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			
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
	}

}
