package bgu.spl.mics.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.RoundRobinList;

import java.util.Iterator;

public class MessageBusImpl implements MessageBus{
	
	private final String MESSAGE_OF_TYPE_REQUEST="request";
	private final String MESSAGE_OF_TYPE_BROADCAST="broadcast";
	
	private Map<MicroService, LinkedBlockingQueue<Message>> mapMicroServicesToQueues;
	private Map<Class<? extends Request>,RoundRobinList> mapRequestTypesToMicroServices;
	private Map<Class<? extends Broadcast>,RoundRobinList> mapBroadcastTypesToMicroServices;
	private Map<Request<?>,MicroService> mapRequestsToMicroServices;
	
	private static class SingletonHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }
	
	public static MessageBusImpl getInstance() {
        return SingletonHolder.instance;
    }
	
	public MessageBusImpl(){
		//Remember this should be a thread safe singleton!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		mapMicroServicesToQueues = new HashMap<MicroService, LinkedBlockingQueue<Message>>();
		mapRequestTypesToMicroServices = new HashMap<Class<? extends Request>, RoundRobinList>();
		mapBroadcastTypesToMicroServices = new HashMap<Class<? extends Broadcast>,RoundRobinList>();
		mapRequestsToMicroServices = new HashMap<Request<?>, MicroService>();
	}
	
	@Override
	/**
     * subscribes {@code m} to receive {@link Request}s of type {@code type}.
     * <p>
     * @param type the type to subscribe to
     * @param m    the subscribing micro-service
     */
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		// Here we need to use only the mapBroadcastTypesToMicroServices
		if(mapRequestTypesToMicroServices.containsKey(type)){ //if the type already exists in the map
			RoundRobinList microServicesSubscribedToTypeList = mapRequestTypesToMicroServices.get(type); //get the linkedList
			if(!microServicesSubscribedToTypeList.contains(m))
				microServicesSubscribedToTypeList.add(m);
		}
		else{ // if the type doesn't exist in the map
			RoundRobinList list = new RoundRobinList();
			list.add(m);
			mapRequestTypesToMicroServices.put(type, list);
		}	
	}
	
	@Override
	/**
     * subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
w    * <p>
     * @param type the type to subscribe to
     * @param m    the subscribing micro-service
     */
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// Here we need to use only the mapBroadcastTypesToMicroServices
		if(mapBroadcastTypesToMicroServices.containsKey(type)){ //if the type already exists in the map
			RoundRobinList microServicesSubscribedToTypeList = mapBroadcastTypesToMicroServices.get(type); //get the linkedList
			if(!microServicesSubscribedToTypeList.contains(m))
				microServicesSubscribedToTypeList.add(m);
		}
		else{ // if the type doesn't exist in the map
			RoundRobinList list = new RoundRobinList();
			list.add(m);
			mapBroadcastTypesToMicroServices.put(type, list);
		}
	}
	
/*
	private void tester(Class<? extends Message> type, MicroService m, String messageType){
		Map<? extends Class<? extends Message>, RoundRobinList> mapTypesToMicroServices;
		if(messageType.equals("Broadcast"))
			mapTypesToMicroServices= mapBroadcastTypesToMicroServices;
		else
			mapTypesToMicroServices=mapRequestTypesToMicroServices;
		
		// Here we need to use only the mapBroadcastTypesToMicroServices
				if(mapTypesToMicroServices.containsKey(type)){ //if the type already exists in the map
					RoundRobinList microServicesSubscribedToTypeList = mapTypesToMicroServices.get(type); //get the linkedList
					if(!microServicesSubscribedToTypeList.contains(m))
						microServicesSubscribedToTypeList.add(m);
				}
				else{ // if the type doesn't exist in the map
					RoundRobinList list = new RoundRobinList();
					list.add(m);
					mapTypesToMicroServices.put(type, list);
				}
	}*/

	@Override
	/**
     * Notifying the MessageBus that the request {@code r} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will implicitly add the
     * special {@link RequestCompleted} message to the queue
     * of the requesting micro-service, the RequestCompleted message will also
     * contain the result of the request ({@code result}).
     * <p>
     * @param <T>    the type of the result expected by the completed request
     * @param r      the completed request
     * @param result the result of the completed request
     */
	public <T> void complete(Request<T> r, T result) {
		//this method notifies the message bus that the request r is completed and it's result was the result.
		//when this method is called, the message bus will add the requestCompleted message to the queue of 
		//the requesting microService. 
		//we need to save a map that will map between the microServices and the requests
		
		//first thing we need to do is find out who was the microService that requested the request r
		MicroService m = mapRequestsToMicroServices.get(r);
		LinkedBlockingQueue mQueue = mapMicroServicesToQueues.get(m);
		mQueue.add(new RequestCompleted<T>(r, result));
		
		// TODO Auto-generated method stub
 	}

	
	@Override
	/**
     * add the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b the message to add to the queues.
     */
	public void sendBroadcast(Broadcast b) {
		if(mapBroadcastTypesToMicroServices.containsKey(b.getClass())){ //Check that the 
			RoundRobinList list = mapBroadcastTypesToMicroServices.get(b.getClass());
			Iterator it = list.iterator();
			while(it.hasNext()){
				MicroService m = (MicroService)it.next();
				LinkedBlockingQueue<Message> mQueue = getQueueByMicroService(m);
				mQueue.add(b);
			}
		}
	}
	
	@Override
	/**
     * add the {@link Request} {@code r} to the message queue of one of the
     * micro-services subscribed to {@code r.getClass()} in a round-robin
     * fashion.
     * <p>
     * @param r         the request to add to the queue.
     * @param requester the {@link MicroService} sending {@code r}.
     * @return true if there was at least one micro-service subscribed to
     *         {@code r.getClass()} and false otherwise.
     */
	public boolean sendRequest(Request<?> r, MicroService requester) {		
		RoundRobinList list = mapRequestTypesToMicroServices.get(r.getClass());
		
		if(list.isEmpty())
			return false;
		
		MicroService m = (MicroService)list.getNext();
		LinkedBlockingQueue<Message> mQueue = mapMicroServicesToQueues.get(m);
		mQueue.add(r);
		
		//map the request to the microService
		if(!mapRequestsToMicroServices.containsKey(r)){
			mapRequestsToMicroServices.put(r, requester);
			return true;
		}
		else{
			//this is bad!! how can it be that this request is already mapped to this microservice??
			//log this as bad!!
			return false;
		}
		
		
	}

	@Override
	/**
     * allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to create a queue for.
     */
	public void register(MicroService m) {
		LinkedBlockingQueue<Message> microServiceQueue = new LinkedBlockingQueue<Message>(); //creating the queue
		mapMicroServicesToQueues.put(m, microServiceQueue);
	}

	@Override
	/**
     * remove the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and clean all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     * @param m the micro-service to unregister.
     */
	public void unregister(MicroService m) {
		//if the map contains an entry whose key is the m.name, then remove that entry
		if(mapMicroServicesToQueues.containsKey(m))
			mapMicroServicesToQueues.remove(m);
		//need to remove the queue from the mapTypesToMicroServices!!!!!!!!!!!!!!!!!
		
		removeMicroServiceFromBroadcasts(m); ///PROBABLY NOT HEREEEEEEEEEEEEE!!!!!!!!!!!!!!!!!
		removeMicroServiceFromRequests(m); ///PROBABLY NOT HEREEEEEEEEEEEEE!!!!!!!!!!!!!!!!!
		
	}
	
	
	

	@Override
	/**
     * using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking -meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message became available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @param m the micro-service requesting to take a message from its message
     *          queue
     * @return the next message in the {@code m}'s queue (blocking)
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     */
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(!mapMicroServicesToQueues.containsKey(m))
			throw new IllegalStateException();
		
		LinkedBlockingQueue<Message> mQueue = getQueueByMicroService(m);
	
		Message message = mQueue.take();
		return message;
	}
	
	//Currently not used
	//Go over the mapTypesToMicroServices and delete the microService "m"
	private void removeMicroServiceFromBroadcasts(MicroService m){
		
		Iterator it = mapBroadcastTypesToMicroServices.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			LinkedList<MicroService> lst = (LinkedList<MicroService>) pair.getValue();
			if(lst.contains(m))
				lst.remove(m);
		}
	}
	
	private void removeMicroServiceFromRequests(MicroService m){
		
		Iterator it = mapRequestTypesToMicroServices.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			LinkedList<MicroService> lst = (LinkedList<MicroService>) pair.getValue();
			if(lst.contains(m))
				lst.remove(m);
		}
	}
	
	//Go over the mapMicroServiceToQueue, find the entry whose key is the provided microservice, and then return it's value (the queue)
	private LinkedBlockingQueue<Message> getQueueByMicroService(MicroService m){
		Iterator it = mapMicroServicesToQueues.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			if(pair.getKey().equals(m))
				return (LinkedBlockingQueue<Message>) pair.getValue();
		}
		return null;
	}

}
