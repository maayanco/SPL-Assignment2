package bgu.spl.app;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;

public class SellingService extends MicroService{

	private Store storeInstance = Store.getInstance();
	private int currentTick;
	
	public SellingService(String name) {
		super(name);
		
		currentTick=1; //should i 
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
		});
		
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			BuyResult res = storeInstance.take(req.getShoeType(), req.isDiscount());
			if(res.equals(BuyResult.DISCOUNTED_PRICE) || res.equals(BuyResult.REGULAR_PRICE)){
			Receipt receipt = new Receipt(req.getSeller(), req.getCustomer(), req.getShoeType(), req.isDiscount(), currentTick, req.getRequestTick(), req.getAmountSold());
				storeInstance.file(receipt);
				//now i need to send this receipt to the client.. - who is the client anyways????
				//i'm not sure what i'm doing here.. but yes i think this shit should work
				complete(req,receipt);
			}
			if(res.equals(BuyResult.NOT_IN_STOCK)){
				RestockRequest requestToRestock = new RestockRequest(req.getShoeType());
				Boolean resultOfRequest = sendRequest(requestToRestock, reqq -> {
					/* WTFF Should i write here*/
					//what should happen when the restock request is performed?
					//complete(req, resultOfRequest);
					});
				if(resultOfRequest==true){
					//in this case i think the restock was succsessfull then now  
					
				}
				else{
					complete(req,null);
				}
			}
				
        });
	}

}
