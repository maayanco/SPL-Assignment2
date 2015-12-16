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
import java.util.Iterator;

public class MessageBusImpl implements MessageBus{
	
	private Map<MicroService, LinkedBlockingQueue<Message>> mapMicroServicesToQueues;
	private Map<Class<? extends Message>,LinkedList<MicroService>> mapTypesToMicroServices;
	 
	public MessageBusImpl(){
		//Remember this should be a thread safe singleton!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		mapMicroServicesToQueues = new HashMap<MicroService, LinkedBlockingQueue<Message>>();
		mapTypesToMicroServices = new HashMap<Class<? extends Message>, LinkedList<MicroService>>();
	}
	
	@Override
	/**
     * subscribes {@code m} to receive {@link Request}s of type {@code type}.
     * <p>
     * @param type the type to subscribe to
     * @param m    the subscribing micro-service
     */
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		mapMicroServiceToType(type,m);
	}
	
	private void mapMicroServiceToType(Class<? extends Message> type, MicroService m){
		if(mapTypesToMicroServices.containsKey(type)){ //if the type already exists in the map
			LinkedList<MicroService> microServicesSubscribedToTypeList = mapTypesToMicroServices.get(type); //get the linkedList
			if(!microServicesSubscribedToTypeList.contains(m))
				microServicesSubscribedToTypeList.add(m);
		}
		else{ // if the type doesn't exist in the map
			LinkedList<MicroService> list = new LinkedList<MicroService>();
			list.add(m);
			mapTypesToMicroServices.put(type, list);
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
		// TODO Auto-generated method stub
		mapMicroServiceToType(type,m);
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
	public <T> void complete(Request<T> r, T result) {
		// TODO Auto-generated method stub
		//////////////////////////////////////////////////////////////////////////////TTTTTTTTTTOOOOOOOOOO DOOOOOOOOOOOO
	}

	@Override
	/**
     * add the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b the message to add to the queues.
     */
	public void sendBroadcast(Broadcast b) {
		if(mapTypesToMicroServices.containsKey(b.getClass())){ //Check that the 
			LinkedList<MicroService> list = mapTypesToMicroServices.get(b.getClass());
			for(MicroService m : list){
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
		// TODO Auto-generated method stub
		return false;
		//////////////////////////////////////////////////////////////////////////////TTTTTTTTTTOOOOOOOOOO DOOOOOOOOOOOO
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
		
		removeMicroServiceFromType(m); ///PROBABLY NOT HEREEEEEEEEEEEEE!!!!!!!!!!!!!!!!!
		
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
	private void removeMicroServiceFromType(MicroService m){
		Iterator it = mapTypesToMicroServices.entrySet().iterator();
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
