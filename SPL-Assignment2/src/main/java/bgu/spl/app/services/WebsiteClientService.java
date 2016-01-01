package bgu.spl.app.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.passive.NewDiscountBroadcast;
import bgu.spl.app.passive.PurchaseOrderRequest;
import bgu.spl.app.passive.PurchaseSchedule;
import bgu.spl.app.passive.TerminationBroadcast;
import bgu.spl.app.passive.TickBroadcast;
import bgu.spl.mics.MicroService;

/**
 * This micro-service describes one client connected to the web-site. The
 * WebsiteClientService expects to get two lists as arguments to its
 * constructor: purchaseSchedule: List<PurchaseSchedule> - contains purchases
 * that the client needs to make (every purchase has a corresponding time tick
 * to send the PurchaseRequest). The list does not guaranteed to be sorted.
 * Important: The WebsiteClientService will make the purchase on the tick
 * specied on the schedule irrelevant of the discount on that item. wishList:
 * Set<String> - The client wish list contains name of shoe types that the
 * client will buy only when there is a discount on them (and immidiatly when he
 * found out of such discount). Once the client bought a shoe from its wishlist
 * - he removes it from the list. In order to get notified when new discount is
 * available, the client should subscribe to the NewDiscountBroadcast message.
 * If the client finish receiving all its purchases and have nothing in its
 * wishList it must immidiatly terminate.
 *
 */
public class WebsiteClientService extends MicroService {

	private static final Logger log = Logger.getLogger(WebsiteClientService.class.getName());
	private Map<Integer, LinkedList<PurchaseSchedule>> mapTicksToPurchaseSchedules;
	private Set<String> wishList;
	private int currentTick;
	private CountDownLatch startLatchObject;
	private CountDownLatch endLatchObject;

	/**
	 * 
	 * @param name
	 * @param purchaseScheduleList
	 * @param wishListItems
	 * @param startLatchObject
	 * @param endLatchObject
	 */
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseScheduleList, Set<String> wishListItems,
			CountDownLatch startLatchObject, CountDownLatch endLatchObject) {

		super(name);

		// Initialize the purchaseSchedule map
		mapTicksToPurchaseSchedules = new HashMap<Integer, LinkedList<PurchaseSchedule>>();
		for (PurchaseSchedule item : purchaseScheduleList) {
			LinkedList<PurchaseSchedule> listOfPurchasesAtTick;
			if (mapTicksToPurchaseSchedules.containsKey(item.getTick())) {
				listOfPurchasesAtTick = mapTicksToPurchaseSchedules.get(item.getTick());
			} else {
				listOfPurchasesAtTick = new LinkedList<PurchaseSchedule>();
			}

			listOfPurchasesAtTick.add(item);
			mapTicksToPurchaseSchedules.put(item.getTick(), listOfPurchasesAtTick);
		}

		// Initialize the wishList
		wishList = new HashSet<String>();
		for (String item : wishListItems) {
			wishList.add(item);
		}

		this.startLatchObject = startLatchObject;
		this.endLatchObject = endLatchObject;
		log.log(Level.INFO, getName() + " client service was initialized");
	}

	/**
	 * This method handles TickBroadcasts, NewDiscountBroadcasts and
	 * TerminationBroadcasts. each Message is handled by a dedicated helper
	 * method.
	 */
	@Override
	protected void initialize() {
		subscribeToTickBroadcast();
		subscribeToNewDiscountBroadcast();
		subscribeToTerminationBroadcast();
		startLatchObject.countDown();
	}

	/**
	 * This method handles the TickBroadcasts every new TickBroadcast it
	 * receives - it updates the current tick of the selling service. In
	 * addition it checks the mapTicksToPurchaseSchedules if there is a purchase
	 * that should occur at the certain tick and if so create and send a
	 * purchase request.
	 */
	private void subscribeToTickBroadcast() {

		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			currentTick = tickBroadcast.getTick();
			/* Check if a purchase should occur at the current tick */
			if (mapTicksToPurchaseSchedules.containsKey(currentTick)) {
				LinkedList<PurchaseSchedule> listOfPurchasesAtCurrentTick = mapTicksToPurchaseSchedules
						.get(currentTick);
				for (PurchaseSchedule futurePurchase : listOfPurchasesAtCurrentTick) {
					PurchaseOrderRequest purchaseRequest = new PurchaseOrderRequest(this.getName(),
							futurePurchase.getShoeType(), false, currentTick, 1);
					log.log(Level.INFO,
							getName() + " has sent a purchase request for " + purchaseRequest.getAmountSold()
									+ " shoes of type " + purchaseRequest.getShoeType() + " at tick "
									+ purchaseRequest.getRequestTick());
					sendRequest(purchaseRequest, purchaseReceipt -> {
						listOfPurchasesAtCurrentTick.remove(futurePurchase);
						if (listOfPurchasesAtCurrentTick.isEmpty())
							mapTicksToPurchaseSchedules.remove(listOfPurchasesAtCurrentTick);
					});
				}
			}
			checkForEmptyLists();

		});
	}

	/**
	 * This method handles NewDiscountBroadcasts. for every new discount
	 * broadcast received it checks if the wishList contains the provided shoe
	 * type if so creates and sends a new purchase request. It removes that shoe
	 * type from the wishList.
	 */
	private void subscribeToNewDiscountBroadcast() {
		subscribeBroadcast(NewDiscountBroadcast.class, discountBroadcast -> {
			log.log(Level.INFO, getName() + " has received a new Discount Broadcast for "
					+ discountBroadcast.getAmountOnSale() + " shoes of type " + discountBroadcast.getShoeType());
			if (wishList.contains(discountBroadcast.getShoeType())) {
				PurchaseOrderRequest purchaseRequest = new PurchaseOrderRequest(this.getName(),
						discountBroadcast.getShoeType(), true, currentTick, 1);
				sendRequest(purchaseRequest, purchaseReceipt -> {
					log.log(Level.INFO, getName() + " has sent a new purchase order for 1 shoe of type "
							+ discountBroadcast.getShoeType());
					wishList.remove(discountBroadcast.getShoeType());
				});
			}
			checkForEmptyLists();
		});
	}

	/**
	 * Checks if the wishList and mapTicksToPurchaseSchedules map are empty. If
	 * so - terminate gracefully.
	 */
	private void checkForEmptyLists() {
		if (wishList.isEmpty() && mapTicksToPurchaseSchedules.isEmpty()) {
			log.log(Level.INFO, getName() + " has completed all it's purchases and wishList and is now terminating..");
			terminate();
			endLatchObject.countDown();
		}
	}

	/**
	 * This method handles TerminationBroadcasts, by starting a graceful
	 * termination of the WebsiteClientService. Firstly It subscribes to
	 * TerminationBroadcasts. When a new termination broadcast is received, we
	 * invoke the end latch countDown to indicate the WebsiteClientService is
	 * terminating, and call the terminate method in order to gracefully finish
	 * running the service.
	 */
	private void subscribeToTerminationBroadcast() {
		subscribeBroadcast(TerminationBroadcast.class, terminationBroadcast -> {
			if (terminationBroadcast.getTerminationStatus() == true) {
				log.log(Level.INFO, getName() + " has received a TerminationBroadcast and is terminating");
				terminate();
				endLatchObject.countDown();
			}
		});
	}

}
