package bgu.spl.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bgu.spl.mics.MicroService;

public class ManagementService extends MicroService{
	
	private static final Store storeInstance = Store.getInstance();
	
	//SHould probably removve the manufacturing order from the map after i finissh it!!!!!
	
	private int currentTick;
	private Map<Integer,DiscountSchedule> mapTicksToDiscountSchedules;
	private Map<String, ManufacturingOrderRequest> mapShoeTypesToManufacturingOrders;/////fuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuck
	private Map<ManufacturingOrderRequest,LinkedList<RestockRequest>> mapManufacturingOrderRequestsToRestockRequests; 
	
	public ManagementService(List<DiscountSchedule> discountScheduleList) {
		super("manager");
		
		//Initialize map and clone the received list
		mapTicksToDiscountSchedules = new HashMap<Integer,DiscountSchedule>();
		for(DiscountSchedule item : discountScheduleList){
			mapTicksToDiscountSchedules.put(item.getTick(), item);
		}
		
		mapShoeTypesToManufacturingOrders = new HashMap<String, ManufacturingOrderRequest>();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, req -> {
			currentTick = req.getTick();
			if(mapTicksToDiscountSchedules.containsKey(currentTick)){
				DiscountSchedule discount = mapTicksToDiscountSchedules.get(currentTick);
				NewDiscountBroadcast b = new NewDiscountBroadcast(discount.getShoeType(), discount.getAmount());
				sendBroadcast(b);
			}
		});
		
		subscribeRequest(RestockRequest.class, req -> {
			
			boolean shouldOrderShoes=true;
			//we now need to check if this type is already on an existing order
			String shoeType=req.getShoeType();
			if(mapShoeTypesToManufacturingOrders.containsKey(shoeType)){ //This shoe type is already on order..  
				//we need to check that there are enough shoes ordered
				ManufacturingOrderRequest manufacturingOrder = mapShoeTypesToManufacturingOrders.get(shoeType);
				LinkedList<RestockRequest> listOfRestockRequestsHandledByManufacturingOrder = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingOrder);
				if(listOfRestockRequestsHandledByManufacturingOrder.size()<manufacturingOrder.getAmount()){ 
					listOfRestockRequestsHandledByManufacturingOrder.add(req);
					shouldOrderShoes=false;
				}
			}
			
			if(shouldOrderShoes){
				
				int amountToOrder= (currentTick%5)+1;
				ManufacturingOrderRequest manufacturingRequest = new ManufacturingOrderRequest(shoeType, (currentTick%5)+1, req.getInitialRequestTick() );
				boolean response = sendRequest(manufacturingRequest, resultReceipt -> {
					LinkedList<RestockRequest> listOfRestockRequestsHandledByManufacturingOrder = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingRequest);
					int freeAmount = amountToOrder-listOfRestockRequestsHandledByManufacturingOrder.size();
					storeInstance.add(shoeType, freeAmount);
					storeInstance.file(resultReceipt);
					LinkedList<RestockRequest> list = mapManufacturingOrderRequestsToRestockRequests.get(manufacturingRequest);
					for(RestockRequest item : list){
						complete(item, true); //we complete all the restock requests that were handled by this manufactring order..
					}
				});
				
				if(!response){ //meaning there were no factories that could handle this request..
					complete(req,false);
				}
			}
			
		});
		
	}

}
