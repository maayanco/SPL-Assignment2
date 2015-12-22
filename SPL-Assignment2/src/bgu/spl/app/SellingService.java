package bgu.spl.app;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;

public class SellingService extends MicroService{

	private Store storeInstance = Store.getInstance();
	
	public SellingService(String name) {
		super(name);
		
	}

	@Override
	protected void initialize() {

		subscribeRequest(PurchaseOrderRequest.class, req -> {
			BuyResult res = storeInstance.take(req.getShoeType(), req.isDiscount());
			if(res.equals(BuyResult.DISCOUNTED_PRICE) || res.equals(BuyResult.REGULAR_PRICE))
				Receipt receipt = new Receipt();
			/*if(req.getShoeType().equals())
				System.out.println("hi");*/
         //code that should happen in the callback   
        });
	}

}
