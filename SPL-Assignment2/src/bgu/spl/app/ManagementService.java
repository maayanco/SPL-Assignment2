package bgu.spl.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class ManagementService extends MicroService{
	
	private static final Store storeInstance = Store.getInstance();
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
	private int currentTick;
	/*private Map<Integer,DiscountSchedule> mapTicksToDiscountSchedules;*/
	private Map<Integer, LinkedList<DiscountSchedule>> mapTicksToDiscountSchedules;
	private Map<String, LinkedList<ManufacturingOrderRequest>> mapShoeTypesToManufacturingOrders;
	private Map<ManufacturingOrderRequest,LinkedList<RestockRequest>> mapManufacturingOrderRequestsToRestockRequests; 
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;
	
	public ManagementService(List<DiscountSchedule> discountScheduleList, CountDownLatch startLatchObject, CountDownLatch endLatchObject) {
		super("manager");
		
		//Initialize map and clone the received list
		/*mapTicksToDiscountSchedules = new ConcurrentHashMap<Integer,DiscountSchedule>();
		for(DiscountSchedule item : discountScheduleList){
			mapTicksToDiscountSchedules.put(item.getTick(), item);
		}*/
		
		mapTicksToDiscountSchedules = new ConcurrentHashMap<Integer, LinkedList<DiscountSchedule>>();
		for(DiscountSchedule item: discountScheduleList){
			if(mapTicksToDiscountSchedules.containsKey(item.getTick())){
				LinkedList<DiscountSchedule> currentListOfDiscounts = mapTicksToDiscountSchedules.get(item.getTick());
				currentListOfDiscounts.add(item);
				mapTicksToDiscountSchedules.replace(item.getTick(), currentListOfDiscounts);
			}
			else{
				LinkedList<DiscountSchedule> newListOfDiscounts = new LinkedList<DiscountSchedule>();
				newListOfDiscounts.add(item);
				mapTicksToDiscountSchedules.put(item.getTick(), newListOfDiscounts);
			}
		}
		
		//DEBUUUUG - DELETE THIS CODE:
		/*System.out.println("we are going to print the mapTicksToDiscountSchedules");
		mapTicksToDiscountSchedules.*/
		//END OF CODE TO BE DELETED
		
		mapShoeTypesToManufacturingOrders= new ConcurrentHashMap<String, LinkedList<ManufacturingOrderRequest>>();
		
		mapManufacturingOrderRequestsToRestockRequests = new ConcurrentHashMap<ManufacturingOrderRequest,LinkedList<RestockRequest>>();
		
		log.log(Level.INFO, getName()+" service was initialized");
		this.startLatchObject=startLatchObject;
		/*startLatchObject.countDown();*/
		this.endLatchObject=endLatchObject;
	}


	private void subscribeToTickBroadcast(){
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
			if(mapTicksToDiscountSchedules.containsKey(currentTick)){
				LinkedList<DiscountSchedule> listOfDiscounts = mapTicksToDiscountSchedules.get(currentTick);
				for(DiscountSchedule discount: listOfDiscounts){
					System.out.println("debugging a discount! currentTick: "+currentTick+" the type of the items to be on discount "+discount.getShoeType()+" the amount to be on discount: "+discount.getAmount()+" the tick on which the discount is to be performed: "+discount.getTick());
					NewDiscountBroadcast b = new NewDiscountBroadcast(discount.getShoeType(), discount.getAmount());
					sendBroadcast(b);
					log.log(Level.INFO, "The manager has sent a newDiscountBroadcast for "+b.getAmountOnSale()+" shoes of type "+b.getShoeType()+" the tick of sending: "+currentTick);
					storeInstance.addDiscount(discount.getShoeType(), discount.getAmount());
				}
				
			}
		});
		

	}
	
	private void subscribeToRestockRequest(){
	subscribeRequest(RestockRequest.class, req -> {
			
			//boolean shouldOrderShoes=true;
			boolean foundManufacturingRequest=false;
			String shoeType=req.getShoeType();

			if(mapShoeTypesToManufacturingOrders.containsKey(shoeType)){ //This shoe type is already on order..  


				//we need to check that there are enough shoes ordered
				LinkedList<ManufacturingOrderRequest> listOfManufacturingOrdersPerShoeType =  mapShoeTypesToManufacturingOrders.get(shoeType);
				if(!listOfManufacturingOrdersPerShoeType.isEmpty()){
					for(ManufacturingOrderRequest manufacturingOrder : listOfManufacturingOrdersPerShoeType){

						if(manufacturingOrder==null) //debuuug
							System.out.println(" BUG BUG BUG BUG BUG BUG BUG BUG BUG BUG - manufacturing order is null - line 71 managmentservice");

						LinkedList<RestockRequest> listOfRestockRequestsHandledByManufacturingOrder = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingOrder);
						if(!foundManufacturingRequest && listOfRestockRequestsHandledByManufacturingOrder.size()<manufacturingOrder.getAmount()){ 
							listOfRestockRequestsHandledByManufacturingOrder.add(req);
							log.log(Level.INFO, getName()+" has handled a restock request by adding it to an existing manufacturing request for "+manufacturingOrder.getAmount()+" shoes of type "+manufacturingOrder.getShoeType()+" which was issued originally at tick "+manufacturingOrder.getInitialRequestTick());
							foundManufacturingRequest=true;
						}
					}
				}

			}
			
			if(!foundManufacturingRequest){
				int amountToOrder =(currentTick%5)+1;
				
				//We create the new manufacturing request
				log.log(Level.INFO, getName()+" has created a new manufacturing request in order to deal with the purchase request for "+req.getAmountRequested()+" shoes of type "+req.getShoeType()+" which was originaly issued at the tick "+req.getInitialRequestTick());
				ManufacturingOrderRequest manufacturingRequest = new ManufacturingOrderRequest(shoeType, (currentTick%5)+1, req.getInitialRequestTick() );
				
				//we check if the shoeType exists in the map, if so we retreive it's list and add the new manufacturing order to the list and the map
				if(mapShoeTypesToManufacturingOrders.containsKey(shoeType)){
					
					if(shoeType==null)//debuuug
							System.out.println(" BUG BUG BUG BUG BUG BUG BUG BUG BUG BUG - shoeType is null - line 103 managmentservice");
						
					LinkedList<ManufacturingOrderRequest> listOfManufacturingRequestsMappedToTheShoeType = mapShoeTypesToManufacturingOrders.get(shoeType);
					listOfManufacturingRequestsMappedToTheShoeType.add(manufacturingRequest);
					LinkedList<RestockRequest> listOfRestockRequests = new LinkedList<RestockRequest>();
					listOfRestockRequests.add(req);
					mapManufacturingOrderRequestsToRestockRequests.put(manufacturingRequest,listOfRestockRequests );
				}
				else{
					LinkedList<ManufacturingOrderRequest> listOfManufacturingRequests = new LinkedList<ManufacturingOrderRequest>();
					listOfManufacturingRequests.add(manufacturingRequest);
					mapShoeTypesToManufacturingOrders.put(shoeType, listOfManufacturingRequests);
					LinkedList<RestockRequest> listOfRestockRequests = new LinkedList<RestockRequest>();
					listOfRestockRequests.add(req);
					
					//Next Line NullPointerException
					
					mapManufacturingOrderRequestsToRestockRequests.put(manufacturingRequest,listOfRestockRequests);
					
					
				}
					
				boolean response = sendRequest(manufacturingRequest, resultReceipt -> {

					LinkedList<RestockRequest> listOfRestockRequestsHandledByManufacturingOrder = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingRequest);
					int freeAmount = amountToOrder-listOfRestockRequestsHandledByManufacturingOrder.size();
					storeInstance.add(shoeType, freeAmount);
					//storeInstance.file(resultReceipt);
					LinkedList<RestockRequest> list = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingRequest);
					for(RestockRequest item : list){
						log.log(Level.INFO, getName()+" has completed a restock request for "+item.getAmountRequested()+" shoes of type "+item.getShoeType()+" which was originaly issued at tick "+item.getInitialRequestTick());
						complete(item, true); //we complete all the restock requests that were handled by this manufactring order..
					}
				});

				if(!response){ //meaning there were no factories that could handle this request..
					complete(req,false);
				}
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
		subscribeToRestockRequest();
		subscribeToTerminationBroadcast();
		startLatchObject.countDown();
	}

}
