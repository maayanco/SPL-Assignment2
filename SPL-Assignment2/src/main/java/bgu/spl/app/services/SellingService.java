package bgu.spl.app.services;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.passive.BuyResult;
import bgu.spl.app.passive.PurchaseOrderRequest;
import bgu.spl.app.passive.Receipt;
import bgu.spl.app.passive.RestockRequest;
import bgu.spl.app.passive.Store;
import bgu.spl.app.passive.TerminationBroadcast;
import bgu.spl.app.passive.TickBroadcast;
import bgu.spl.mics.MicroService;

/**
 * This micro-service handles PurchaseOrderRequests. When the SellingService
 * receives a PurchaseOrderRequest, it handles it by trying to take the required
 * shoe from the storage. If it succeeded it creates a receipt, file it in the
 * store and pass it to the client (as the result of completing the
 * PurchaseOrderRequest). If there were no shoes on the requested type on stock,
 * the selling service will send RestockRequest, if the request completed with
 * the value false (see ManagementService) the SellingService will complete
 * the PurchaseOrderRequest with the value of null (to indicate to the client
 * that the purchase was unsuccessful). If the client indicates in the order
 * that he wish to get this shoe only on discount and no more discounted shoes
 * are left then it will complete the client request with null result (to
 * indicate to the client that the purchase was unsuccessful.
 */
public class SellingService extends MicroService {

	private static final Store storeInstance = Store.getInstance();
	private static final Logger log = Logger.getLogger(SellingService.class.getName());
	private int currentTick;
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;

	/**
	 * @param name
	 *            - the name of the seller
	 * @param startLatchObject
	 *            - CountDownLatch object which is used in the initialization
	 * @param endLatchObject
	 *            - CountDownLatch which is used in termination
	 */
	public SellingService(String name, CountDownLatch startLatchObject, CountDownLatch endLatchObject) {
		super(name);
		this.startLatchObject = startLatchObject;
		this.endLatchObject = endLatchObject;
		log.log(Level.INFO, getName() + " selling service was initialized");
	}

	/**
	 * This method handles TickBroadcasts, PurchaseOrderRequests and
	 * TerminationBroadcasts. each Message is handled by a dedicated helper
	 * method.
	 */
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();
		subscribeToPurchaseOrderRequest();
		subscribeToTerminationBroadcast();
		startLatchObject.countDown();

	}

	/**
	 * This method handles the TickBroadcasts every new TickBroadcast it
	 * receives - it updates the current tick of the selling service.
	 */
	private void subscribeToTickBroadcast() {
		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			currentTick = tickBroadcast.getTick();
		});
	}

	/**
	 * This method handles the PurchaseOrderRequests received from the
	 * WebsiteClients. It attempts to take the shoes from the store. If It
	 * succeded, it
	 * 
	 */
	private void subscribeToPurchaseOrderRequest() {
		subscribeRequest(PurchaseOrderRequest.class, purchaseRequest -> {
			log.log(Level.INFO, " has received a purchase request for " + purchaseRequest.getAmountSold()
					+ " shoes of type " + purchaseRequest.getShoeType());

			/* Attempt to take the required shoes from the store */
			BuyResult res = storeInstance.take(purchaseRequest.getShoeType(), purchaseRequest.isDiscount());

			if (res.equals(BuyResult.DISCOUNTED_PRICE) || res.equals(BuyResult.REGULAR_PRICE)) {
				/*
				 * Create a new receipt for purchasing from the seller and
				 * complete the purchase request with it as it's result
				 */
				boolean retreivedWithDiscount;
				if (res.equals(BuyResult.DISCOUNTED_PRICE))
					retreivedWithDiscount = true;
				else
					retreivedWithDiscount = false;
				Receipt receipt = new Receipt(this.getName(), purchaseRequest.getCustomer(),
						purchaseRequest.getShoeType(), retreivedWithDiscount, currentTick,
						purchaseRequest.getRequestTick(), purchaseRequest.getAmountSold());
				storeInstance.file(receipt);
				log.log(Level.INFO, getName() + " has completed a purchase order by: " + purchaseRequest.getCustomer()
						+ " by taking sucsessfully from the store ");
				complete(purchaseRequest, receipt);
			} else if (res.equals(BuyResult.NOT_ON_DISCOUNT)) {
				/*
				 * complete the purchase request with null to indicate failure
				 */
				log.log(Level.INFO, getName()
						+ " couldn't complete the purchase request because there were no discounted items on stock as requested ");
				complete(purchaseRequest, null);
			} else if (res.equals(BuyResult.NOT_IN_STOCK)) {
				/*
				 * There weren't enough shoes in the store, send a RetockRequest
				 * to the manager
				 */
				RestockRequest requestToRestock = new RestockRequest(purchaseRequest.getShoeType(), 1,
						purchaseRequest.getRequestTick());
				sendRequest(requestToRestock, isRestockRequestSuccessful -> {
					log.log(Level.INFO, getName() + " has sent a restock request");
					/*
					 * If the restock request was successful, create a new
					 * receipt and complete the original purchase request with
					 * it. Otherwise complete it with null to indicate failure
					 */
					if (isRestockRequestSuccessful) {
						Receipt receipt = new Receipt(this.getName(), purchaseRequest.getCustomer(),
								purchaseRequest.getShoeType(), purchaseRequest.isDiscount(), currentTick,
								purchaseRequest.getRequestTick(), purchaseRequest.getAmountSold());
						storeInstance.file(receipt);
						log.log(Level.INFO,
								getName() + " has completed the purchase order for " + purchaseRequest.getAmountSold()
										+ " shoes of type " + purchaseRequest.getShoeType()
										+ " which was issued originaly at tick: " + purchaseRequest.getRequestTick()
										+ " by the customer " + purchaseRequest.getCustomer());
						complete(purchaseRequest, receipt);
					} else {
						log.log(Level.WARNING,
								getName() + " couldn't complete the purchase order for "
										+ purchaseRequest.getAmountSold() + " shoes of type "
										+ purchaseRequest.getShoeType() + " which was issued originally at tick:"
										+ purchaseRequest.getRequestTick() + " by the customer "
										+ purchaseRequest.getCustomer() + " becasue the restock request failed");
						complete(purchaseRequest, null);
					}
				});
			}
		});
	}

	/**
	 * This method handles TerminationBroadcasts, by starting a graceful
	 * termination of the SellingService. Firstly It subscribes to
	 * TerminationBroadcasts. When a new termination broadcast is received, we
	 * invoke the end latch countDown to indicate the SellingService is
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

}
