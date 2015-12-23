package bgu.spl.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import bgu.spl.mics.MicroService;

public class ShoeFactoryService extends MicroService{
	
	private int currentTick;
	private LinkedList<ManufacturingOrderRequest> queueManufacturingOrders;
	private Map<ManufacturingOrderRequest,Integer> mapManufacturingOrdersToShoesNumber; 
	
	
	public ShoeFactoryService(String name) {
		super(name);
		
		queueManufacturingOrders = new LinkedList<ManufacturingOrderRequest>();
		mapManufacturingOrdersToShoesNumber = new HashMap<ManufacturingOrderRequest,Integer>();
		
	}

	
	@Override
	protected void initialize() {
		
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
			makeShoeIfNeeded();
		});
		
		subscribeRequest(ManufacturingOrderRequest.class, req -> {
			queueManufacturingOrders.add(req); // here we added the request
			mapManufacturingOrdersToShoesNumber.put(req, req.getAmount());
			
		});
		
	}
	
	private void makeShoeIfNeeded(){
		
		if(!queueManufacturingOrders.isEmpty()){
			ManufacturingOrderRequest manufactringOrder = queueManufacturingOrders.getFirst();
			if(!mapManufacturingOrdersToShoesNumber.containsKey(manufactringOrder)){
				System.out.println("damn!!");
				//we should log this as really bad..
			}
			int numberOfShoesLeftToProduce = mapManufacturingOrdersToShoesNumber.get(manufactringOrder);
			numberOfShoesLeftToProduce=numberOfShoesLeftToProduce-1;
			if(numberOfShoesLeftToProduce==0){
				Receipt receipt = new Receipt(this.getName(),"store", manufactringOrder.getShoeType(),false, currentTick, manufactringOrder.getInitialRequestTick(), manufactringOrder.getAmount() );
				complete(manufactringOrder, receipt);
				mapManufacturingOrdersToShoesNumber.remove(manufactringOrder);
				queueManufacturingOrders.remove(manufactringOrder);
			}
			else{
				mapManufacturingOrdersToShoesNumber.replace(manufactringOrder, numberOfShoesLeftToProduce-1);
			}
		}
	}
	
	
}
