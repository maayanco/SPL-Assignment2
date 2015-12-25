package bgu.spl.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class ShoeFactoryService extends MicroService{
	
	private int currentTick;
	private LinkedList<ManufacturingOrderRequest> queueManufacturingOrders;
	private Map<ManufacturingOrderRequest,Integer> mapManufacturingOrdersToShoesNumber; 
	private CountDownLatch latchObject;
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	public ShoeFactoryService(String name, CountDownLatch latchObject) {
		super(name);
		
		queueManufacturingOrders = new LinkedList<ManufacturingOrderRequest>();
		mapManufacturingOrdersToShoesNumber = new HashMap<ManufacturingOrderRequest,Integer>();
		
		this.latchObject=latchObject;
		latchObject.countDown();
	}

	
	@Override
	protected void initialize() {
		
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
			makeShoeIfNeeded();
		});
		
		subscribeRequest(ManufacturingOrderRequest.class, req -> {
			log.log(Level.INFO, "i got a manufacturing order");
			queueManufacturingOrders.add(req); // here we added the request
			mapManufacturingOrdersToShoesNumber.put(req, req.getAmount());
			
		});
		
		subscribeBroadcast(TerminationBroadcast.class, req -> {
			log.log(Level.INFO, "i am terminatinnnng");
			if(req.getToTerminate()==true){
				latchObject.countDown();
				terminate();
			}
		});
		
	}
	
	private void makeShoeIfNeeded(){
		
		//log.log(Level.INFO, "we are making a shoe");
		
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
