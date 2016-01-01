package bgu.spl.app.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.passive.ManufacturingOrderRequest;
import bgu.spl.app.passive.Receipt;
import bgu.spl.app.passive.Store;
import bgu.spl.app.passive.TerminationBroadcast;
import bgu.spl.app.passive.TickBroadcast;
import bgu.spl.mics.MicroService;

/**
 * This micro-service describes a shoe factory that manufacture shoes for the
 * store. This micro-service handles the ManufacturingOrderRequest it takes it
 * exactly 1 tick to manufacture a single shoe. When done manufacturing, this
 * micro-service completes the request with a receipt (which has the value
 * store in the customer field and discount = false).
 *
 */
public class ShoeFactoryService extends MicroService {

	private static final Logger log = Logger.getLogger(ShoeFactoryService.class.getName());
	private static final Store storeInstance = Store.getInstance();
	private int currentTick;
	private LinkedList<ManufacturingOrderRequest> queueManufacturingOrders;
	private Map<ManufacturingOrderRequest, Integer> mapManufacturingOrdersToShoesNumber;
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;

	/**
	 * @param name
	 *            - the name of the factory
	 * @param startLatchObject
	 *            - CountDownLatch object which is used in the initialization
	 * @param endLatchObject
	 *            - CountDownLatch which is used in termination
	 */
	public ShoeFactoryService(String name, CountDownLatch startLatchObject, CountDownLatch endLatchObject) {
		super(name);
		queueManufacturingOrders = new LinkedList<ManufacturingOrderRequest>();
		mapManufacturingOrdersToShoesNumber = new HashMap<ManufacturingOrderRequest, Integer>();
		this.startLatchObject = startLatchObject;
		this.endLatchObject = endLatchObject;
		log.log(Level.INFO, getName() + " factory was initialized");
	}

	/**
	 * This method handles TickBroadcasts, ManufacturingOrderRequests and
	 * TerminationBroadcasts. each Message is handled by a dedicated helper
	 * method.
	 */
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();
		subscribeToManufacturingOrderRequest();
		subscribeToTerminationBroadcast();
		startLatchObject.countDown();
	}

	/**
	 * This method handles the TickBroadcasts every new TickBroadcast it
	 * receives - it updates the current tick of the selling service. In
	 * addition it invokes the makeShoeIfNeeded method.
	 */
	private void subscribeToTickBroadcast() {
		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			currentTick = tickBroadcast.getTick();
			makeShoeIfNeeded();
		});

	}

	/**
	 * Handles manufacturing requests, firstly by subscribing to receive them.
	 * Every new manufacturing order received is added to the
	 * queueManufacturingOrders and to the mapManufacturingOrdersToShoesNumber
	 */
	private void subscribeToManufacturingOrderRequest() {
		subscribeRequest(ManufacturingOrderRequest.class, manufacturingOrder -> {
			log.log(Level.INFO,
					getName() + " has received a manufacturing request for " + manufacturingOrder.getAmount()
							+ " shoes of type " + manufacturingOrder.getShoeType() + " which was issued at the tick: "
							+ manufacturingOrder.getInitialRequestTick());
			queueManufacturingOrders.add(manufacturingOrder);
			mapManufacturingOrdersToShoesNumber.put(manufacturingOrder, manufacturingOrder.getAmount());
		});
	}

	/**
	 * This method handles TerminationBroadcasts, by starting a graceful
	 * termination of the ManagmentService. Firstly It subscribes to
	 * TerminationBroadcasts. When a new termination broadcast is received, we
	 * invoke the end latch countDown to indicate the ManagmentService is
	 * terminating, and call the terminate method in order to gracefully finish
	 * running the service.
	 */
	private void subscribeToTerminationBroadcast() {
		subscribeBroadcast(TerminationBroadcast.class, terminationBroadcast -> {
			if (terminationBroadcast.getTerminationStatus() == true) {
				log.log(Level.INFO, getName() + " has received a termination broadcast and is terminating..");
				terminate();
				endLatchObject.countDown();
			}
		});
	}
	
	
	/**
	 * Check if there is a manufacturing order to be manufactured
	 * take the first one to be received, if the number of shoes left to produce in it is 0 - 
	 * then complete the request with a new receipt, take the next manufacturing order (if exists)
	 * and manufacture one shoe and update that new manufacturing order.
	 * If the number of shoes left to produce is not 0 - manufacture one shoe and update the 
	 * manufacturing order
	 */
	private void makeShoeIfNeeded() {
		if (!queueManufacturingOrders.isEmpty()) {
			ManufacturingOrderRequest manufactringOrder = queueManufacturingOrders.getFirst();
			if (!mapManufacturingOrdersToShoesNumber.containsKey(manufactringOrder)) {
				log.log(Level.SEVERE, getName() + " has encountered a problem - couldn't manufacture shoe ");
			}
			int numberOfShoesLeftToProduce = mapManufacturingOrdersToShoesNumber.get(manufactringOrder);
			
			if (numberOfShoesLeftToProduce == 0) {
				/*
				 * Finished manufacturing an order, crate a receipt and complete the
				 * manufacturingOrder with it as it's result. Remove the
				 * manufacturing order from the queue and map
				 */
				Receipt receipt = new Receipt(this.getName(), "store", manufactringOrder.getShoeType(), false,
						currentTick, manufactringOrder.getInitialRequestTick(), manufactringOrder.getAmount());
				storeInstance.file(receipt);
				log.log(Level.INFO,
						getName() + " has completed a manufacturing request for " + manufactringOrder.getAmount()
								+ " shoes of type " + manufactringOrder.getShoeType()
								+ " which was originally issued at tick: " + manufactringOrder.getInitialRequestTick());
				complete(manufactringOrder, receipt);
				mapManufacturingOrdersToShoesNumber.remove(manufactringOrder);
				queueManufacturingOrders.remove(manufactringOrder);
				
				/* Get next order if exists and manufacture a shoe */
				if(!queueManufacturingOrders.isEmpty()){
					manufactringOrder = queueManufacturingOrders.getFirst();
					if (!mapManufacturingOrdersToShoesNumber.containsKey(manufactringOrder)) {
						log.log(Level.SEVERE, getName() + " has encountered a problem - couldn't manufacture shoe ");
					}
					numberOfShoesLeftToProduce = mapManufacturingOrdersToShoesNumber.get(manufactringOrder);
					numberOfShoesLeftToProduce=numberOfShoesLeftToProduce -1;
					log.log(Level.INFO, getName() + " has created a new shoe of type " + manufactringOrder.getShoeType());
					mapManufacturingOrdersToShoesNumber.replace(manufactringOrder, numberOfShoesLeftToProduce);
				}
			} else {
				numberOfShoesLeftToProduce=numberOfShoesLeftToProduce -1;
				log.log(Level.INFO, getName() + " has created a new shoe of type " + manufactringOrder.getShoeType());
				mapManufacturingOrdersToShoesNumber.replace(manufactringOrder, numberOfShoesLeftToProduce);
				
			}

		}
	}

}
