package bgu.spl.mics.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LoggingPermission;

import javax.swing.text.StyledEditorKit.StyledTextAction;

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
	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName());
	
	private Map<MicroService, LinkedBlockingQueue<Message>> mapMicroServicesToQueues;
	private Map<Class<? extends Message>,RoundRobinList> mapRequestTypesToMicroServices;//
	private Map<Class<? extends Message>,RoundRobinList> mapBroadcastTypesToMicroServices;
	private Map<Request<?>,MicroService> mapRequestsToMicroServices;
	
	
	private static class SingletonHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }
	
	public static MessageBusImpl getInstance() {
        return SingletonHolder.instance;
    }
	
	public MessageBusImpl(){
		
		//setup logger
		System.setProperty("java.util.logging.SimpleFormatter.format","%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
	
		mapMicroServicesToQueues = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		mapRequestTypesToMicroServices = new HashMap<Class<? extends Message>, RoundRobinList>();
		mapBroadcastTypesToMicroServices = new ConcurrentHashMap<Class<? extends Message>,RoundRobinList>();
		mapRequestsToMicroServices = new HashMap<Request<?>, MicroService>();
		
	}
	
	
	@Override
	/**
     * subscribes {@code m} to receive {@link Request}s of type {@code type}.
     * <p>
     * @param type the type to subscribe to
     * @param m    the subscribing micro-service
     */
	public void subscribeRequest(Class<? extends Request<?>> type, MicroService m){
		subscribeMessage(type, m, MESSAGE_OF_TYPE_REQUEST);
	}
	
	@Override
	/**
     * subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @param type the type to subscribe to
     * @param m    the subscribing micro-service
     */
	public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribeMessage(type, m, "broadcast");
	}
	
	public  void subscribeMessage(Class<? extends Message> type, MicroService m, String typeOfMessage){
		Map<Class<? extends Message>,RoundRobinList> map;
		
		if(typeOfMessage.equals(MESSAGE_OF_TYPE_REQUEST)){
			map = mapRequestTypesToMicroServices;
		}
		else{
			map= mapBroadcastTypesToMicroServices;
		}
		
		//If the type already exists in the map
		if(map.containsKey(type)){ 
			RoundRobinList microServicesSubscribedToTypeList = map.get(type); 
			if(!microServicesSubscribedToTypeList.contains(m)){
				microServicesSubscribedToTypeList.add(m);
			}
			else{
				log.log(Level.WARNING, "MessageBusImpl - subscribeMessage - Attempt to subscribe the MicroService "+m.getName()+" to the request type aborted,"+m.getName()+" is already subscribed");
			}
		}
		else{
			RoundRobinList list = new RoundRobinList();
			list.add(m);
			map.put(type, list);
		}	
	}
	
	


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
	public synchronized <T> void complete(Request<T> r, T result) {
		if(r==null)
			log.log(Level.SEVERE, " complete method has received a null parameter: r");
		else if(result==null)
			log.log(Level.SEVERE," complete method has received a null parameter: result");
		else{
			MicroService m = mapRequestsToMicroServices.get(r);
			LinkedBlockingQueue mQueue = mapMicroServicesToQueues.get(m);
			if(mQueue==null)
				log.log(Level.SEVERE, "!!!!3333 mQueue is nulllll,the microservice m is: "+m.getName());
			mQueue.add(new RequestCompleted<T>(r, result)); //NullPointerException here!!!
		}
 	}


	
	@Override
	/**
     * add the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b the message to add to the queues.
     */
	public synchronized void sendBroadcast(Broadcast b) {
		
		if(mapBroadcastTypesToMicroServices.containsKey(b.getClass())){ 
			RoundRobinList roundRobinList = mapBroadcastTypesToMicroServices.get(b.getClass());
			Iterator it = roundRobinList.iterator();
			while(it.hasNext()){
				MicroService m = (MicroService)it.next();
				LinkedBlockingQueue<Message> mQueue = getQueueByMicroService(m);
				mQueue.add(b);
			}
		}
		else{
			log.log(Level.WARNING, "There was no MicroService found which is subscribed to the message type: "+b.getClass());
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
	public synchronized boolean sendRequest(Request<?> r, MicroService requester) {
		
		if(!mapRequestTypesToMicroServices.containsKey(r.getClass())){
			log.log(Level.WARNING, "MessageBusImpl - sendRequest - couldn't send request, the type of the request wasn't found");
			return false;
		}
		
		RoundRobinList roundRobinList = mapRequestTypesToMicroServices.get(r.getClass());
		
		if(roundRobinList.isEmpty()){
			log.log(Level.INFO," MessageBusImpl - sendRequest - No MicroServices are subscribed to the request "+r+" sending was aborted");
			return false;
		}
		
		//Take the next MicroService from the RoundRobinList, retreive it's allocated queue and add the request r to it's queue
		MicroService m = (MicroService)roundRobinList.getNext();
		LinkedBlockingQueue<Message> mQueue = mapMicroServicesToQueues.get(m);
		mQueue.add(r);
		
		
		//Map the request r to the microService
		if(!mapRequestsToMicroServices.containsKey(r)){
			mapRequestsToMicroServices.put(r, requester);
			return true;
		}
		else{
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

		LinkedBlockingQueue<Message> mQueue = new LinkedBlockingQueue<Message>(); 
		mapMicroServicesToQueues.put(m, mQueue);
		
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
		
		//if the map contains an entry whose key m, then remove that entry
		if(mapMicroServicesToQueues.containsKey(m)){
			mapMicroServicesToQueues.remove(m);
		}
		else{
			log.log(Level.WARNING, "MessageBusImpl - unregister - MicroService "+m.getName()+" was not found in the mapMicroServicesToQueues");
		}
		
		//Remove the MicroService from the mapBroadcastTypeToMicroServices map and the mapRequestTypesToMicroServices map
		removeMicroServiceFromBroadcasts(m); 
		removeMicroServiceFromRequests(m); 
		
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
	public  Message awaitMessage(MicroService m) throws InterruptedException {
				
		if(!mapMicroServicesToQueues.containsKey(m)){
			log.log(Level.SEVERE, "MessageBusImpl - awaitMessage - No queue was found matching the MicroService "+m.getName()); 
			throw new IllegalStateException();
		}
		
		LinkedBlockingQueue<Message> mQueue = getQueueByMicroService(m);
		Message message = mQueue.take();
		
		return message;
	}
	
	/**
	 * Go over the mapBroadcastTypesToMicroServices map and remove every instance of the microservice {@code m}
	 * @param m the MicroService requesting to be removed 
	 */
	private  void removeMicroServiceFromBroadcasts(MicroService m){
		removeMicroServiceFromMessageType(m,MESSAGE_OF_TYPE_BROADCAST);
	}
	
	/**
	 * Go over the mapRequestTypesToMicroServices map and remove every instance of the microservice {@code m}
	 * @param m the MicroService requesting to be removed 
	 */
	private void removeMicroServiceFromRequests(MicroService m){
		removeMicroServiceFromMessageType(m,MESSAGE_OF_TYPE_REQUEST);
	}
	
	/**
	 * Go over the map required map and remove every instance of the MicroService {@code m} from it
	 * @param m - the MicroService requesting to be removed
	 * @param typeOfMessage - String indicating whether the removal should be from the 
	 * 						  mapBroadcastTypesToMicroServices map or the mapRequestTypesToMicroServices map
	 */
	private void removeMicroServiceFromMessageType(MicroService m, String typeOfMessage){
		
		Map<Class<? extends Message>,RoundRobinList> mapMessageTypesToMicroServices;
		
		if(typeOfMessage.equals(MESSAGE_OF_TYPE_REQUEST)){
			mapMessageTypesToMicroServices = mapRequestTypesToMicroServices;
		}
		else{
			mapMessageTypesToMicroServices= mapBroadcastTypesToMicroServices;
		}
		
		Iterator it = mapMessageTypesToMicroServices.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			RoundRobinList lst = (RoundRobinList)pair.getValue();
			if(lst.contains(m)){
				lst.remove(m);
			}
		}
		
	}
	
	

	/**
	 * locates and returns the message queue allocated to {@code m}
	 * @param m the MicroService requesting it's queue 
	 * @return the queue which is mapped to {@code m}
 	 */
	private LinkedBlockingQueue<Message> getQueueByMicroService(MicroService m){
		
		Iterator it = mapMicroServicesToQueues.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			if(pair.getKey().equals(m)){
				return (LinkedBlockingQueue<Message>) pair.getValue();
			}
		}
		
		return null;
	}

}
