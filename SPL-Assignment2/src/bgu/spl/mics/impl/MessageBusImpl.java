package bgu.spl.mics.impl;

import java.util.LinkedList;
import java.util.Queue;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;

public class MessageBusImpl implements MessageBus{

	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void complete(Request<T> r, T result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void register(MicroService m) {
		LinkedList<Message> microServiceQueue = new LinkedList<Message>();
		
	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
	

}
