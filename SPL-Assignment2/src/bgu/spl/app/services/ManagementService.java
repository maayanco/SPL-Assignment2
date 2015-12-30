package bgu.spl.app.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.passive.DiscountSchedule;
import bgu.spl.app.passive.ManufacturingOrderRequest;
import bgu.spl.app.passive.NewDiscountBroadcast;
import bgu.spl.app.passive.RestockRequest;
import bgu.spl.app.passive.Store;
import bgu.spl.app.passive.TerminationBroadcast;
import bgu.spl.app.passive.TickBroadcast;
import bgu.spl.mics.MicroService;

/**
 * This micro-service can add discount to shoes in the store and send NewDiscountBroadcast to notify
 * clients about them. In order to do so, this service expects to get a list of DiscountSchedule as
 * argument to its constructor
 * <p>
 * In addition, the ManagementService handles RestockRequests that is being sent by the SellingService.
 * Whenever a RestockRequest of a specific shoe type received the service first check that
 * this shoe type is not already on order (and if it does, it checks that there are enough ordered to
 * give one to the seller) if it doesn't (or the ordered amount was not enough) it will send a ManufacturingOrderRequest
 * for (current-tick%5) + 1 shoes of this type, when this order completes - it
 * update the store stock, file the receipt and only then complete the RestockRequest (and not before)
 * with the result of true. If there were no one that can handle the ManufacturingOrderRequest (i.e.,
 * no factories are available) it will complete the RestockRequest with the result false
 *
 */
public class ManagementService extends MicroService{

	private static final Store storeInstance = Store.getInstance();
	private static final Logger log = Logger.getLogger(ManagementService.class.getName() );

	private int currentTick;
	private Map<Integer, LinkedList<DiscountSchedule>> mapTicksToDiscountSchedules;
	private Map<String, LinkedList<ManufacturingOrderRequest>> mapShoeTypesToManufacturingOrders;
	private Map<ManufacturingOrderRequest,LinkedList<RestockRequest>> mapManufacturingOrderRequestsToRestockRequests; 
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;

	/**
	 * @param discountScheduleList - List of discounts 
	 * @param startLatchObject - CountDownLatch object which is used in the initialization   
	 * @param endLatchObject - CountDownLatch which is used in termination 
	 */
	public ManagementService(List<DiscountSchedule> discountScheduleList, CountDownLatch startLatchObject, CountDownLatch endLatchObject) {
		super("manager");

		/* Initializing the mapTicksToDiscountSchedules from the discountScheduleList */
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

		/* Initialize the maps and CountDownLatches*/
		mapShoeTypesToManufacturingOrders= new ConcurrentHashMap<String, LinkedList<ManufacturingOrderRequest>>();
		mapManufacturingOrderRequestsToRestockRequests = new ConcurrentHashMap<ManufacturingOrderRequest,LinkedList<RestockRequest>>();
		this.startLatchObject=startLatchObject;
		this.endLatchObject=endLatchObject;
		
		log.log(Level.INFO, getName()+" service was initialized");
	}


	/**
	 * This method handles TickBroadcasts, RestockRequests and TerminationBroadcasts.
	 * each Message is handled by a dedicated helper method.
	 */
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();
		subscribeToRestockRequest();
		subscribeToTerminationBroadcast();
		startLatchObject.countDown();
	}

	/**
	 * This method handles new TickBroadcasts, first by subscribing to them.
	 * For every new TickBroadcast it updates the currentTick member and checks if there is a discount that
	 * should occur at that tick in the mapTicksToDiscountSchedules. If so it sends sends a newDiscountBroadcast
	 * It also informs the Store of the discount by invoking the addDiscount method.
	 */
	private void subscribeToTickBroadcast(){
		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			currentTick = tickBroadcast.getTick();
			if(mapTicksToDiscountSchedules.containsKey(currentTick)){
				LinkedList<DiscountSchedule> listOfDiscounts = mapTicksToDiscountSchedules.get(currentTick);
				for(DiscountSchedule discountItem: listOfDiscounts){
					NewDiscountBroadcast discountBroadcast = new NewDiscountBroadcast(discountItem.getShoeType(), discountItem.getAmount());
					sendBroadcast(discountBroadcast);
					log.log(Level.INFO, "The manager has sent a newDiscountBroadcast for "+discountBroadcast.getAmountOnSale()+" shoes of type "+discountBroadcast.getShoeType());
					storeInstance.addDiscount(discountItem.getShoeType(), discountItem.getAmount());
				}
			}
		});
	}
	
	/**
	 * This method handles new RestockRequests, received from a SellingService.
	 * Firstly it subscribes to such requests. Every new restock request that is received 
	 */
	private void subscribeToRestockRequest(){
		subscribeRequest(RestockRequest.class, restockRequest -> {
			
			boolean foundManufacturingRequest=false;
			String shoeType=restockRequest.getShoeType();
			
			/* Attempt to add the restock request to an existing manufacturing order*/
			if(mapShoeTypesToManufacturingOrders.containsKey(shoeType)){
				foundManufacturingRequest = addRestockRequestToExistingManufacturingOrder(shoeType, restockRequest);
			}
			
			if(!foundManufacturingRequest){
				 /* Create and send a new manufacturing order, update all related maps */
				int amountOfShoesInOrder =(currentTick%5)+1;
				ManufacturingOrderRequest manufacturingRequest = new ManufacturingOrderRequest(shoeType, (currentTick%5)+1, restockRequest.getInitialRequestTick() );
				updaetMapManufacturingOrderRequestsToRestockRequests(manufacturingRequest, restockRequest);
				
				/* Send the new manufacturing request, If completed with a receipt we go over
				 * all the restock requests that are 'subscribed' to this manufacturing order and complete them
				 * with the receipt we received. If there were no factories that could handle the request -
				 * complete it with result false */
				log.log(Level.INFO, getName()+" has sent a new manufacturing request in order to deal with the purchase request for "+restockRequest.getAmountRequested()+" shoes of type "+restockRequest.getShoeType()+" which was originaly issued at the tick "+restockRequest.getInitialRequestTick());
				boolean response = sendManufacturingRequest(manufacturingRequest, amountOfShoesInOrder, shoeType);
				if(!response){ 
					complete(restockRequest,false);
				}
			}

		});
	}

	/**
	 * Updates the mapShoeTypesToManufacturingOrders and mapManufacturingOrdersToRestockRequests map
	 * to include the provided manufacturingRequest and restockRequest. 
	 * @param manufacturingRequest - the manufacturing request itself
	 * @param restockRequest - the restock request
	 */
	private void updaetMapManufacturingOrderRequestsToRestockRequests(ManufacturingOrderRequest manufacturingRequest, RestockRequest restockRequest){
		String shoeType = manufacturingRequest.getShoeType();
		if(mapShoeTypesToManufacturingOrders.containsKey(shoeType)){
			/* There are already other manufacturing requests mapped to the shoeType, we will add our
			   manufacturing request to them */
			LinkedList<ManufacturingOrderRequest> listOfManufacturingRequestsMappedToTheShoeType = mapShoeTypesToManufacturingOrders.get(shoeType);
			listOfManufacturingRequestsMappedToTheShoeType.add(manufacturingRequest);
			LinkedList<RestockRequest> listOfRestockRequests = new LinkedList<RestockRequest>();
			listOfRestockRequests.add(restockRequest);
			mapManufacturingOrderRequestsToRestockRequests.put(manufacturingRequest,listOfRestockRequests );
		}
		else{
			/* There are no manufacturing requests currently mapped to the shoe type, 
			 * we will create a new entry in the mapManufacturingOrderRequestsToRestockRequests 
			 * with our manufacturing order */
			LinkedList<ManufacturingOrderRequest> listOfManufacturingRequests = new LinkedList<ManufacturingOrderRequest>();
			listOfManufacturingRequests.add(manufacturingRequest);
			mapShoeTypesToManufacturingOrders.put(shoeType, listOfManufacturingRequests);
			LinkedList<RestockRequest> listOfRestockRequests = new LinkedList<RestockRequest>();
			listOfRestockRequests.add(restockRequest);
			mapManufacturingOrderRequestsToRestockRequests.put(manufacturingRequest,listOfRestockRequests);
		}
	}
	
	/**
	 * This method handles sending the provided manufacturing order request.
	 * When this manufacturing request is completed with a receipt we go over all the restock requests 
	 * which are 'subscribed' to this manufacturing request and complete them with the result true. 
	 * If the receipt we received is null we complete all the subscribed restock requests with false.
	 * @param manufacturingRequest - the manufacturing request that should be sent
	 * @param amountOfShoesInOrder - the amount of shoes ordered
	 * @param shoeType
	 * @return
	 */
	private boolean sendManufacturingRequest(ManufacturingOrderRequest manufacturingRequest, int amountOfShoesInOrder, String shoeType){
		return sendRequest(manufacturingRequest, resultReceipt -> {
			boolean restockResult=false;
			if(resultReceipt!=null){
				restockResult=true;
			}
			LinkedList<RestockRequest> listOfRestockRequestsHandledByManufacturingOrder = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingRequest);
			
			/* Adding the free shoes to the store */
			int freeAmount = amountOfShoesInOrder-listOfRestockRequestsHandledByManufacturingOrder.size();
			storeInstance.add(shoeType, freeAmount);
			
			/* Completing the restock requests */
			LinkedList<RestockRequest> list = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingRequest);
			for(RestockRequest item : list){
				log.log(Level.INFO, getName()+" has completed a restock request for "+item.getAmountRequested()+" shoes of type "+item.getShoeType()+" which was originaly issued at tick "+item.getInitialRequestTick());
				complete(item, restockResult); 
			}
		});
	}
	
	/**
	 * This refers to the case where there is already at least one manufacturing order for
	 * this shoe type. We search for a manufacturing order which has at least 1 free shoe to 
	 * give to the restockRequest. if found - we add the restockRequest to the list of 
	 * Restock requests which will be supplied by this manufacturing order
	 * @return
	 */
	private boolean addRestockRequestToExistingManufacturingOrder(String shoeType, RestockRequest restockRequest){
		LinkedList<ManufacturingOrderRequest> listOfManufacturingOrdersPerShoeType =  mapShoeTypesToManufacturingOrders.get(shoeType);
		if(!listOfManufacturingOrdersPerShoeType.isEmpty()){
			for(ManufacturingOrderRequest manufacturingOrder : listOfManufacturingOrdersPerShoeType){
				LinkedList<RestockRequest> listOfRestockRequestsHandledByManufacturingOrder = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingOrder);
				if(listOfRestockRequestsHandledByManufacturingOrder.size()<manufacturingOrder.getAmount()){ 
					listOfRestockRequestsHandledByManufacturingOrder.add(restockRequest);
					log.log(Level.INFO, getName()+" has handled a restock request by adding it to an existing manufacturing request for "+manufacturingOrder.getAmount()+" shoes of type "+manufacturingOrder.getShoeType()+" which was issued originally at tick "+manufacturingOrder.getInitialRequestTick());
					return true;
				}
			}
		}
		return false;
	}
	
	/** 
	 * This method handles TerminationBroadcasts, by starting a graceful termination of the ManagmentService. 
	 * Firstly It subscribes to TerminationBroadcasts. When a new termination broadcast is received,
	 * we invoke the end latch countDown to indicate the ManagmentService is terminating,
	 * and call the terminate method in order to gracefully finish running the service.
	 */
	private void subscribeToTerminationBroadcast(){
		subscribeBroadcast(TerminationBroadcast.class, terminationBroadcast -> {
			if(terminationBroadcast.getTerminationStatus()==true){
				log.log(Level.INFO, getName()+" has received a termination broadcast and is terminating..");
				endLatchObject.countDown();
				terminate();
			}
		});
	}


}
