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
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;
	private Store storeInstance = Store.getInstance();
	
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	public ShoeFactoryService(String name, CountDownLatch startLatchObject, CountDownLatch endLatchObject) {
		super(name);
		
		queueManufacturingOrders = new LinkedList<ManufacturingOrderRequest>();
		mapManufacturingOrdersToShoesNumber = new HashMap<ManufacturingOrderRequest,Integer>();
		
		log.log(Level.INFO, getName()+" factory was initialized");
		this.startLatchObject=startLatchObject;
		startLatchObject.countDown();
		this.endLatchObject=endLatchObject;
	}

	
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();
		subscribeToManufacturingOrderRequest();
		subscribeToTerminationBroadcast();
	}
	
	private void subscribeToTickBroadcast(){
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
			makeShoeIfNeeded();
		});
		
	}
	
	private void subscribeToManufacturingOrderRequest(){
		subscribeRequest(ManufacturingOrderRequest.class, req -> {
			log.log(Level.INFO, getName()+" has received a manufacturing request for "+req.getAmount()+" shoes of type "+req.getShoeType()+" which was issued at the tick: "+req.getInitialRequestTick());
			queueManufacturingOrders.add(req); 
			mapManufacturingOrdersToShoesNumber.put(req, req.getAmount());
			
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
	
	private void makeShoeIfNeeded(){
		
		if(!queueManufacturingOrders.isEmpty()){
			ManufacturingOrderRequest manufactringOrder = queueManufacturingOrders.getFirst();
			if(!mapManufacturingOrdersToShoesNumber.containsKey(manufactringOrder)){
				log.log(Level.SEVERE, getName()+" has encountered a problem - couldn't manufacture shoe ");
			}
			int numberOfShoesLeftToProduce = mapManufacturingOrdersToShoesNumber.get(manufactringOrder);
			numberOfShoesLeftToProduce=numberOfShoesLeftToProduce-1;
			log.log(Level.INFO, getName()+" has created a new shoe of type "+manufactringOrder.getShoeType());
			if(numberOfShoesLeftToProduce==0){
				Receipt receipt = new Receipt(this.getName(),"store", manufactringOrder.getShoeType(),false, currentTick, manufactringOrder.getInitialRequestTick(), manufactringOrder.getAmount() );
				storeInstance.file(receipt);
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
