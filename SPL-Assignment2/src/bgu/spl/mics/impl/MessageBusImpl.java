package bgu.spl.mics.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
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

import java.util.Iterator;

public class MessageBusImpl implements MessageBus{
	
	private final String MESSAGE_OF_TYPE_REQUEST="request"; //should be deprecated?
	private final String MESSAGE_OF_TYPE_BROADCAST="broadcast"; //should be deprecated?
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	
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
		log.log(Level.INFO, "MessageBusImpl Constructor was invoked"); //Logger
		
		//Remember this should be a thread safe singleton!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		mapMicroServicesToQueues = new HashMap<MicroService, LinkedBlockingQueue<Message>>();
		mapRequestTypesToMicroServices = new HashMap<Class<? extends Request>, RoundRobinList>();
		mapBroadcastTypesToMicroServices = new HashMap<Class<? extends Broadcast>,RoundRobinList>();
		mapRequestsToMicroServices = new HashMap<Request<?>, MicroService>();
		
		log.log(Level.INFO, " 'MessageBusImpl' has been initialized"); //Logger
	}
	
	@Override
	/**
     * subscribes {@code m} to receive {@link Request}s of type {@code type}.
     * <p>
     * @param type the type to subscribe to
     * @param m    the subscribing micro-service
     */
	//we map the requests types! to the micro services
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		log.log(Level.INFO, "subscribeRequest method was invoked with parameters: "+type+", "+m); //Logger
		
		
		// Here we need to use only the mapBroadcastTypesToMicroServices
		if(mapRequestTypesToMicroServices.containsKey(type)){ //if the type already exists in the map
			RoundRobinList microServicesSubscribedToTypeList = mapRequestTypesToMicroServices.get(type); //get the linkedList
			if(!microServicesSubscribedToTypeList.contains(m)){
				microServicesSubscribedToTypeList.add(m);
				log.log(Level.INFO, "The microService "+m+" sucsessfully subscribed to the request type "+type);
			}
			else
				log.log(Level.WARNING, "Attempt to subscribe the MicroService "+m.toString()+" to the request type "+type.toString()+" aborted,"+m+" is already subscribed");
		}
		else{ // if the type doesn't exist in the map
			RoundRobinList list = new RoundRobinList();
			list.add(m);
			mapRequestTypesToMicroServices.put(type, list);
			
			log.log(Level.INFO, "The microService "+m+" sucsessfully subscribed to the request type "+type);
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
		log.log(Level.INFO, "subscribeBroadcast method was invoked with parameters: "+type+", "+m); //Logger
		
		
		// Here we need to use only the mapBroadcastTypesToMicroServices
		if(mapBroadcastTypesToMicroServices.containsKey(type)){ //if the type already exists in the map
			RoundRobinList microServicesSubscribedToTypeList = mapBroadcastTypesToMicroServices.get(type); //get the linkedList
			if(!microServicesSubscribedToTypeList.contains(m)){
				microServicesSubscribedToTypeList.add(m);
				log.log(Level.INFO, "The microService "+m+" sucsessfully subscribed to the broadcast type "+type.toString());
			}
			else
				log.log(Level.WARNING, "Attempt to subscribe the MicroService "+m.toString()+" to the Broadcast type "+type.toString()+" aborted,"+m+" is already subscribed");
		
		}
		else{ // if the type doesn't exist in the map
			RoundRobinList list = new RoundRobinList();
			list.add(m);
			mapBroadcastTypesToMicroServices.put(type, list);
			log.log(Level.INFO, "The microService "+m+" sucsessfully subscribed to the broadcast type "+type.toString());
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
		log.log(Level.INFO, "complete method was invoked with parameters: "+r+", "+result);
		
		
		MicroService m = mapRequestsToMicroServices.get(r);
		LinkedBlockingQueue mQueue = mapMicroServicesToQueues.get(m);
		mQueue.add(new RequestCompleted<T>(r, result));
		
		log.log(Level.INFO, "RequestCompleted message was sucsessfully added to the queue of the "+m+" MicroService who sent the request");
 	}

	
	@Override
	/**
     * add the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b the message to add to the queues.
     */
	public void sendBroadcast(Broadcast b) {
		log.log(Level.INFO, "sendBroadcast method was invoked with parameter: "+b); //Logger
		
		if(mapBroadcastTypesToMicroServices.containsKey(b.getClass())){ //Check that the 
			RoundRobinList list = mapBroadcastTypesToMicroServices.get(b.getClass());
			Iterator it = list.iterator();
			while(it.hasNext()){
				MicroService m = (MicroService)it.next();
				LinkedBlockingQueue<Message> mQueue = getQueueByMicroService(m);
				mQueue.add(b);
				log.log(Level.INFO, "The broadcast "+b+" has been succsessfully added to the queue of the MicroService "+m);
			}
		}
		else
			log.log(Level.WARNING, "The type "+b.getClass()+" wasn't found in the mapBroadcastTypesToMicroServices - unable to send broadcast");
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
		log.log(Level.INFO, "sendRequest method was invoked with parameters: "+r+", "+requester); //Logger
		
		RoundRobinList list = mapRequestTypesToMicroServices.get(r.getClass());
		
		if(list.isEmpty()){
			log.log(Level.INFO," No MicroServices are subscribed to the request "+r+" sending was aborted");
			return false;
		}
		
		MicroService m = (MicroService)list.getNext();
		LinkedBlockingQueue<Message> mQueue = mapMicroServicesToQueues.get(m);
		mQueue.add(r);
		
		log.log(Level.INFO, "");
		
		//map the request to the microService
		if(!mapRequestsToMicroServices.containsKey(r)){
			mapRequestsToMicroServices.put(r, requester);
			log.log(Level.INFO, "MicroService "+m+" was succsessfully mapped to the request "+r);
			return true;
		}
		else{
			log.log(Level.WARNING, "MicroService "+m+" is already mapped to the request "+r);
			return false; //should we return false in this scenario?
		}
		
		
	}

	@Override
	/**
     * allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to create a queue for.
     */
	public void register(MicroService m) {
		log.log(Level.INFO, "register method was invoked with parameters: "+m); //Logger
		
		LinkedBlockingQueue<Message> microServiceQueue = new LinkedBlockingQueue<Message>(); //creating the queue
		mapMicroServicesToQueues.put(m, microServiceQueue);
		
		log.log(Level.INFO, "Queue was succsessfully created and mapped to the MicroService "+m); //Logger
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
		log.log(Level.INFO, "unregister method was invoked with parameters: "+m); //Logger
		
		//if the map contains an entry whose key is the m.name, then remove that entry
		if(mapMicroServicesToQueues.containsKey(m)){
			mapMicroServicesToQueues.remove(m);
			log.log(Level.INFO, "MicroService "+m+" was sucsessfully removed from the mapMicroServicesToQueues");
		}
		else
			log.log(Level.WARNING, "MicroService "+m+"was not found in the mapMicroServicesToQueues");
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
		log.log(Level.INFO, "awaitMessage method was invoked with parameters: "+m); //Logger
		
		if(!mapMicroServicesToQueues.containsKey(m)){
			log.log(Level.SEVERE, "No queue was found matching the MicroService "+m, new IllegalStateException()); //should there be the exception?? or the line after?? am i doing the same thing twice??
			throw new IllegalStateException();
		}
		
		LinkedBlockingQueue<Message> mQueue = getQueueByMicroService(m);
		Message message = mQueue.take();
		log.log(Level.INFO, "Message was received from the queue of the MicroService "+m );
		return message;
	}
	
	//Currently not used
	//Go over the mapTypesToMicroServices and delete the microService "m"
	private void removeMicroServiceFromBroadcasts(MicroService m){
		log.log(Level.INFO, "removeMicroServiceFromBroadcasts method was invoked with parameters: "+m); //Logger
		
		Boolean isFound=false;
		Iterator it = mapBroadcastTypesToMicroServices.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			LinkedList<MicroService> lst = (LinkedList<MicroService>) pair.getValue();
			if(lst.contains(m)){
				lst.remove(m);
				isFound=true;
			}
		}
		
		if(isFound)
			log.log(Level.INFO, "MicroService "+m+" was succsessfully unsubscribed from all Broadcast types"); //Logger
		else
			log.log(Level.INFO, "Attempt to unsubscribe the MicroService "+m+" from all broadcast types aborted - the microservice was not subscribed to a broadcast type");
			
	}
	
	private void removeMicroServiceFromRequests(MicroService m){
		log.log(Level.INFO, "removeMicroServiceFromRequests method was invoked with parameters: "+m); //Logger
		
		Boolean isFound=false;
		Iterator it = mapRequestTypesToMicroServices.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			LinkedList<MicroService> lst = (LinkedList<MicroService>) pair.getValue();
			if(lst.contains(m)){
				lst.remove(m);
				isFound=true;
			}
		}
		
		if(isFound)
			log.log(Level.INFO, "MicroService "+m+" was succsessfully unsubscribed from all request types"); //Logger
		else
			log.log(Level.INFO, "Attempt to unsubscribe the MicroService "+m+" from all request types aborted - the microservice was not subscribed to a request type");
	}
	
	//Go over the mapMicroServiceToQueue, find the entry whose key is the provided microservice, and then return it's value (the queue)
	private LinkedBlockingQueue<Message> getQueueByMicroService(MicroService m){
		log.log(Level.INFO, "getQueueByMicroService method was invoked with parameters: "+m); //Logger
		
		Iterator it = mapMicroServicesToQueues.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			if(pair.getKey().equals(m)){
				log.log(Level.INFO, "The queue of the MicroService "+m+" has been located and returned");
				return (LinkedBlockingQueue<Message>) pair.getValue();
			}
		}
		log.log(Level.WARNING, "The queue of the MicroService "+m+" was not found");
		return null;
	}

}
