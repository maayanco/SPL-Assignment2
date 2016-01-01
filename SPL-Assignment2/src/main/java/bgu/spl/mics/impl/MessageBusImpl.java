package bgu.spl.mics.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.RoundRobinList;

/**
 * The message-bus is a shared object used for communication between
 * micro-services. It should be implemented as a thread-safe singleton. The
 * message-bus implementation must be thread-safe as it is shared between all
 * the micro-services in the system.
 */
public class MessageBusImpl implements MessageBus {

	private final String MESSAGE_OF_TYPE_REQUEST = "request";
	private final String MESSAGE_OF_TYPE_BROADCAST = "broadcast";
	private static final Logger log = Logger.getLogger(MessageBusImpl.class.getName());

	private Map<MicroService, LinkedBlockingQueue<Message>> mapMicroServicesToQueues;
	private Map<Class<? extends Message>, RoundRobinList> mapRequestTypesToMicroServices;
	private Map<Class<? extends Message>, RoundRobinList> mapBroadcastTypesToMicroServices;
	private Map<Request<?>, MicroService> mapRequestsToMicroServices;

	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	/**
	 * @return instance of the MessageBusImpl
	 */
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Initializes fields and constructs the MessagBusImpl
	 */
	public MessageBusImpl() {
		mapMicroServicesToQueues = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		mapRequestTypesToMicroServices = new ConcurrentHashMap<Class<? extends Message>, RoundRobinList>();
		mapBroadcastTypesToMicroServices = new ConcurrentHashMap<Class<? extends Message>, RoundRobinList>();
		mapRequestsToMicroServices = new ConcurrentHashMap<Request<?>, MicroService>();
	}

	/**
	 * subscribes {@code m} to receive {@link Request}s of type {@code type}.
	 * <p>
	 * 
	 * @param type
	 *            the type to subscribe to
	 * @param m
	 *            the subscribing micro-service
	 */
	@Override
	public void subscribeRequest(Class<? extends Request<?>> type, MicroService m) {
		subscribeMessage(type, m, MESSAGE_OF_TYPE_REQUEST);
	}

	/**
	 * subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
	 * <p>
	 * 
	 * @param type
	 *            the type to subscribe to
	 * @param m
	 *            the subscribing micro-service
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribeMessage(type, m, "broadcast");
	}

	/**
	 * 
	 * @param type
	 *            the type to subscribe to
	 * @param m
	 *            the subscribing micro-service
	 * @param typeOfMessage
	 *            indicate whether the message is a request or a broadcast
	 */
	private void subscribeMessage(Class<? extends Message> type, MicroService m, String typeOfMessage) {
		Map<Class<? extends Message>, RoundRobinList> mapMessageTypesToMicroServices;

		/* Determine which map to use - broadcasts or requests */
		if (typeOfMessage.equals(MESSAGE_OF_TYPE_REQUEST)) {
			mapMessageTypesToMicroServices = mapRequestTypesToMicroServices;
		} else {
			mapMessageTypesToMicroServices = mapBroadcastTypesToMicroServices;
		}

		synchronized (mapMessageTypesToMicroServices) {
			/* If the type already exists in the map */
			if (mapMessageTypesToMicroServices.containsKey(type)) {
				RoundRobinList microServicesSubscribedToTypeList = mapMessageTypesToMicroServices.get(type);
				if (!microServicesSubscribedToTypeList.contains(m)) {
					microServicesSubscribedToTypeList.add(m);
				} else {
					log.log(Level.WARNING, "MessageBusImpl - subscribeMessage - Attempt to subscribe the MicroService "
							+ m.getName() + " to the request type aborted," + m.getName() + " is already subscribed");
				}
			} else {
				RoundRobinList roundRobinListOfMicroServices = new RoundRobinList();
				roundRobinListOfMicroServices.add(m);
				mapMessageTypesToMicroServices.put(type, roundRobinListOfMicroServices);
			}
		}

	}

	/**
	 * Notifying the MessageBus that the request {@code r} is completed and its
	 * result was {@code result}. When this method is called, the message-bus
	 * will implicitly add the special {@link RequestCompleted} message to the
	 * queue of the requesting micro-service, the RequestCompleted message will
	 * also contain the result of the request ({@code result}).
	 * <p>
	 * 
	 * @param <T>
	 *            the type of the result expected by the completed request
	 * @param r
	 *            the completed request
	 * @param result
	 *            the result of the completed request
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> void complete(Request<T> r, T result) {
		if (r != null || result != null) {
			MicroService m = null;
			synchronized (mapRequestsToMicroServices) {
				m = mapRequestsToMicroServices.get(r);
			}
			if (m != null) {
				synchronized (mapMicroServicesToQueues) {
					LinkedBlockingQueue mQueue = mapMicroServicesToQueues.get(m);
					if (mQueue == null)
						log.log(Level.SEVERE, " complete aborted - there was no queue registered to the micro service "
								+ m.getName());
					else
						mQueue.add(new RequestCompleted<T>(r, result));
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	/**
	 * add the {@link Broadcast} {@code b} to the message queues of all the
	 * micro-services subscribed to {@code b.getClass()}.
	 * <p>
	 * 
	 * @param b
	 *            the message to add to the queues.
	 */
	public void sendBroadcast(Broadcast b) {
		RoundRobinList roundRobinListOfMicroServices = null;
		synchronized (mapBroadcastTypesToMicroServices) {
			if (!mapBroadcastTypesToMicroServices.containsKey(b.getClass())) {
				log.log(Level.WARNING,
						"There was no MicroService found which is subscribed to the message type: " + b.getClass());
				return;
			}
		}

		synchronized (mapBroadcastTypesToMicroServices) {
			roundRobinListOfMicroServices = mapBroadcastTypesToMicroServices.get(b.getClass());
		}

		synchronized (roundRobinListOfMicroServices) {
			Iterator it = roundRobinListOfMicroServices.iterator();
			while (it.hasNext()) {
				MicroService m = (MicroService) it.next();
				LinkedBlockingQueue<Message> mQueue = getQueueByMicroService(m);
				mQueue.add(b);
			}
		}

	}

	/**
	 * add the {@link Request} {@code r} to the message queue of one of the
	 * micro-services subscribed to {@code r.getClass()} in a round-robin
	 * fashion.
	 * <p>
	 * 
	 * @param r
	 *            the request to add to the queue.
	 * @param requester
	 *            the {@link MicroService} sending {@code r}.
	 * @return true if there was at least one micro-service subscribed to
	 *         {@code r.getClass()} and false otherwise.
	 */
	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		RoundRobinList roundRobinList = null;

		synchronized (mapRequestTypesToMicroServices) {
			if (!mapRequestTypesToMicroServices.containsKey(r.getClass())) {
				log.log(Level.WARNING,
						"MessageBusImpl - sendRequest - couldn't send request, the type of the request wasn't found");
				return false;
			}

			roundRobinList = mapRequestTypesToMicroServices.get(r.getClass());
		}
		if (roundRobinList == null)
			return false;

		if (roundRobinList.isEmpty()) {
			log.log(Level.WARNING, " MessageBusImpl - sendRequest - No MicroServices are subscribed to the request " + r
					+ " sending was aborted");
			return false;
		}

		/*
		 * Take the next MicroService from the RoundRobinList, retreive it's
		 * allocated queue and add the request r to it's queue
		 */
		MicroService m = (MicroService) roundRobinList.getNext();
		synchronized (mapMicroServicesToQueues) {
			LinkedBlockingQueue<Message> mQueue = mapMicroServicesToQueues.get(m);
			mQueue.add(r);
		}

		synchronized (mapRequestsToMicroServices) {
			/* Map the request r to the microService */
			if (!mapRequestsToMicroServices.containsKey(r)) {
				mapRequestsToMicroServices.put(r, requester);
				return true;
			} else {
				return false;
			}
		}

	}

	/**
	 * allocates a message-queue for the {@link MicroService} {@code m}.
	 * <p>
	 * 
	 * @param m
	 *            the micro-service to create a queue for.
	 */
	@Override
	public void register(MicroService m) {
		LinkedBlockingQueue<Message> mQueue = new LinkedBlockingQueue<Message>();
		synchronized (mapMicroServicesToQueues) {
			mapMicroServicesToQueues.put(m, mQueue);
		}
	}

	/**
	 * remove the message queue allocated to {@code m} via the call to
	 * {@link #register(bgu.spl.mics.MicroService)} and clean all references
	 * related to {@code m} in this message-bus. If {@code m} was not
	 * registered, nothing should happen.
	 * <p>
	 * 
	 * @param m
	 *            the micro-service to unregister.
	 */
	@Override
	public void unregister(MicroService m) {
		boolean mapContainsKey = false;

		synchronized (mapMicroServicesToQueues) {
			mapContainsKey = mapMicroServicesToQueues.containsKey(m);
			if (mapContainsKey) {
				mapMicroServicesToQueues.remove(m);
			}
		}

		if (!mapContainsKey) {
			log.log(Level.WARNING, "MessageBusImpl - unregister - MicroService " + m.getName()
					+ " was not found in the mapMicroServicesToQueues");
		}

		/*
		 * Remove the MicroService from the mapBroadcastTypeToMicroServices map
		 * and the mapRequestTypesToMicroServices map
		 */
		removeMicroServiceFromBroadcasts(m);
		removeMicroServiceFromRequests(m);

	}

	/**
	 * using this method, a <b>registered</b> micro-service can take message
	 * from its allocated queue. This method is blocking -meaning that if no
	 * messages are available in the micro-service queue it should wait until a
	 * message became available. The method should throw the
	 * {@link IllegalStateException} in the case where {@code m} was never
	 * registered.
	 * <p>
	 * 
	 * @param m
	 *            the micro-service requesting to take a message from its
	 *            message queue
	 * @return the next message in the {@code m}'s queue (blocking)
	 * @throws InterruptedException
	 *             if interrupted while waiting for a message to became
	 *             available.
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!mapMicroServicesToQueues.containsKey(m)) {
			log.log(Level.SEVERE,
					"MessageBusImpl - awaitMessage - No queue was found matching the MicroService " + m.getName());
			throw new IllegalStateException();
		}

		LinkedBlockingQueue<Message> mQueue = getQueueByMicroService(m);
		Message message = mQueue.take();

		return message;
	}

	/**
	 * Go over the mapBroadcastTypesToMicroServices map and remove every
	 * instance of the microservice {@code m}
	 * 
	 * @param m
	 *            the MicroService requesting to be removed
	 */
	private void removeMicroServiceFromBroadcasts(MicroService m) {
		removeMicroServiceFromMessageType(m, MESSAGE_OF_TYPE_BROADCAST);
	}

	/**
	 * Go over the mapRequestTypesToMicroServices map and remove every instance
	 * of the microservice {@code m}
	 * 
	 * @param m
	 *            the MicroService requesting to be removed
	 */
	private void removeMicroServiceFromRequests(MicroService m) {
		removeMicroServiceFromMessageType(m, MESSAGE_OF_TYPE_REQUEST);
	}

	/**
	 * Go over the map required map and remove every instance of the
	 * MicroService {@code m} from it
	 * 
	 * @param m
	 *            - the MicroService requesting to be removed
	 * @param typeOfMessage
	 *            - String indicating whether the removal should be from the
	 *            mapBroadcastTypesToMicroServices map or the
	 *            mapRequestTypesToMicroServices map
	 */
	@SuppressWarnings("rawtypes")
	private void removeMicroServiceFromMessageType(MicroService m, String typeOfMessage) {

		Map<Class<? extends Message>, RoundRobinList> mapMessageTypesToMicroServices;

		/* determine which map to use - requests or broadcasts */
		if (typeOfMessage.equals(MESSAGE_OF_TYPE_REQUEST)) {
			mapMessageTypesToMicroServices = mapRequestTypesToMicroServices;
		} else {
			mapMessageTypesToMicroServices = mapBroadcastTypesToMicroServices;
		}

		/*
		 * Iterate over the map and remove every instance of the micro service m
		 */
		synchronized (mapMessageTypesToMicroServices) {
			Iterator it = mapMessageTypesToMicroServices.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				RoundRobinList lst = (RoundRobinList) pair.getValue();
				if (lst.contains(m)) {
					lst.remove(m);
				}
			}
		}

	}

	/**
	 * locates and returns the message queue allocated to {@code m}
	 * 
	 * @param m
	 *            the MicroService requesting it's queue
	 * @return the queue which is mapped to {@code m}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private LinkedBlockingQueue<Message> getQueueByMicroService(MicroService m) {
		/*
		 * Iterate over the map of micro services to queues, queue mapped to the
		 * micro service m if exists
		 */
		synchronized (mapMicroServicesToQueues) {
			Iterator it = mapMicroServicesToQueues.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				if (pair.getKey().equals(m)) {
					return (LinkedBlockingQueue<Message>) pair.getValue();
				}
			}
		}
		return null;
	}

}
