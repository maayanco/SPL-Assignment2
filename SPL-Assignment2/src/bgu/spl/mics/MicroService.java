package bgu.spl.mics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.mics.impl.MessageBusImpl;

/**
 * The MicroService is an abstract class that any micro-service in the system
 * must extend. The abstract MicroService class is responsible to get and
 * manipulate the singleton {@link MessageBus} instance.
 * <p>
 * Derived classes of MicroService should never directly touch the message-bus.
 * Instead, they have a set of internal protected wrapping methods (e.g.,
 * {@link #sendBroadcast(bgu.spl.mics.Broadcast)}, {@link #sendBroadcast(bgu.spl.mics.Broadcast)},
 * etc.) they can use . When subscribing to message-types,
 * the derived class also supplies a {@link Callback} that should be called when
 * a message of the subscribed type was taken from the micro-service
 * message-queue (see {@link MessageBus#register(bgu.spl.mics.MicroService)}
 * method). The abstract MicroService stores this callback together with the
 * type of the
 * message is related to.
 * <p>
 */
public abstract class MicroService implements Runnable {

	private static final Logger log = Logger.getLogger( MessageBusImpl.class.getName() );
	private static final MessageBus messageBusInstance = MessageBusImpl.getInstance();
	private Map<Class<? extends Message>, Callback> mapMessageTypesToCallbacks = new HashMap<Class<? extends Message>, Callback>();
	private Map<Request<?>,Callback> mapRequestsToCallbacks = new HashMap<Request<?>,Callback>();
	private boolean terminated = false;
	private final String name;

	/**
	 * @param name the micro-service name (used mainly for debugging purposes -
	 *             does not have to be unique)
	 */
	public  MicroService(String name) {
		messageBusInstance.register(this);
		this.name = name;
	}

	/**
	 * subscribes to requests of type {@code type} with the callback
	 * {@code callback}. This means two things:
	 * 1. subscribe to requests in the singleton event-bus using the supplied
	 * {@code type}
	 * 2. store the {@code callback} so that when requests of type {@code type}
	 * received it will be called.
	 * <p>
	 * for a received message {@code m} of type {@code type = m.getClass()}
	 * calling the callback {@code callback} means running the method
	 * {@link Callback#call(java.lang.Object)} by calling
	 * {@code callback.call(m)}.
	 * <p>
	 * @param <R>      the type of request to subscribe to
	 * @param type     the {@link Class} representing the type of request to
	 *                 subscribe to.
	 * @param callback the callback that should be called when messages of type
	 *                 {@code type} are taken from this micro-service message
	 *                 queue.
	 */
	protected final <R extends Request<?>> void subscribeRequest(Class<R> type, Callback<R> callback){
		messageBusInstance.subscribeRequest(type, this);
		mapMessageTypesToCallbacks.put(type, callback);
	}

	/**
	 * subscribes to broadcast message of type {@code type} with the callback
	 * {@code callback}. This means two things:
	 * 1. subscribe to broadcast messages in the singleton event-bus using the
	 * supplied {@code type}
	 * 2. store the {@code callback} so that when broadcast messages of type
	 * {@code type} received it will be called.
	 * <p>
	 * for a received message {@code m} of type {@code type = m.getClass()}
	 * calling the callback {@code callback} means running the method
	 * {@link Callback#call(java.lang.Object)} by calling
	 * {@code callback.call(m)}.
	 * <p>
	 * @param <B>      the type of broadcast message to subscribe to
	 * @param type     the {@link Class} representing the type of broadcast
	 *                 message to
	 *                 subscribe to.
	 * @param callback the callback that should be called when messages of type
	 *                 {@code type} are taken from this micro-service message
	 *                 queue.
	 */
	protected  final <B extends Broadcast> void subscribeBroadcast(Class<B> type, Callback<B> callback) {
		messageBusInstance.subscribeBroadcast(type, this);
		mapMessageTypesToCallbacks.put(type, callback);
	}

	/**
	 * send the request {@code r} using the message-bus and storing the
	 * {@code onComplete} callback so that it will be executed <b> in this
	 * micro-service event loop </b> once the request is complete.
	 * <p>
	 * @param <T>        the type of the expected result of the request
	 *                   {@code r}
	 * @param r          the request to send
	 * @param onComplete the callback to call when {@code r} is completed. This
	 *                   callback expects to receive (i.e., in the
	 *                   {@link Callback#call(java.lang.Object)} first argument)
	 *                   the result provided when the micro-service receiving {@code r} completes
	 *                   it.
	 * @return true if there was at least one micro-service subscribed to
	 *         {@code r.getClass()} and false otherwise.
	 */
	protected  final <T> boolean sendRequest(Request<T> r, Callback<T> onComplete) {
		Boolean response = messageBusInstance.sendRequest(r, this);
		if(response) //we are not sure that this should happen only when the response is true
			mapRequestsToCallbacks.put(r, onComplete); 
		
		return response; 
	}

	/**
	 * send the broadcast message {@code b} using the message-bus.
	 * <p>
	 * @param b the broadcast message to send
	 */
	protected  final void sendBroadcast(Broadcast b) {
		messageBusInstance.sendBroadcast(b);
	}

	/**
	 * complete the received request {@code r} with the result {@code result}
	 * using the message-bus.
	 * <p>
	 * @param <T>    the type of the expected result of the received request
	 *               {@code r}
	 * @param r      the request to complete
	 * @param result the result to provide to the micro-service requesting
	 *               {@code r}.
	 */
	protected final <T> void complete(Request<T> r, T result) {
		messageBusInstance.complete(r, result);
	}

	/**
	 * this method is called once when the event loop starts.
	 */
	protected abstract void initialize();

	/**
	 * signal the event loop that it must terminate after handling the current
	 * message.
	 */
	protected  final void terminate() {
		this.terminated = true;
	}

	/**
	 * @return the name of the service - the service name is given to it in the
	 *         construction time and is used mainly for debugging purposes.
	 */
	public  final String getName() {
		return name;
	}

	/**
	 * the entry point of the micro-service. 
	 * otherwise you will end up in an infinite loop.
	 */
	@Override
	public  final void run(){
		
		initialize();
		
		while (!terminated) {
			
				try {
					Message receivedMessage = messageBusInstance.awaitMessage(this);
				
					if(receivedMessage.getClass().getName().equals("bgu.spl.mics.RequestCompleted")){ 
						//If message is of type RequestCompleted
						RequestCompleted r = (RequestCompleted)receivedMessage;	
						Callback callback = mapRequestsToCallbacks.get(r.getCompletedRequest());
						callback.call(r.getResult());
					}
					else{
						//If message is of type Broadcast or Request
						if(!mapMessageTypesToCallbacks.containsKey(receivedMessage.getClass()))
							log.log(Level.WARNING, "Callback was not found for the message: "+receivedMessage);
						else{
							Callback callback = mapMessageTypesToCallbacks.get(receivedMessage.getClass());
							callback.call(receivedMessage);
						}
					}
				} catch (InterruptedException e) {
					log.log(Level.SEVERE, "MicroService - run - Exception! InterruptedException was received by awaitMessage");
				}
		}
		
		messageBusInstance.unregister(this); 
		
	}

}
