package bgu.spl.mics.impl;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.test.TestBroadcast;
import bgu.spl.test.TestMicroService;
import bgu.spl.test.TestRequest;

/**
 * Junit Test Class for MessageBusImpl
 */
public class MessageBusImplTest {

	private static MessageBusImpl messageBus;
	private static MicroService m1;
	private static MicroService m2;
	private static MicroService m3;
	private static Request<String> r;
	private static Broadcast b;

	@Before
	/**
	 * 'Reset' and initialize the microservices, broadcast of TestBroadcast type and request of type TestRequest.
	 * Used for testing the methods of MessageBusImpl
	 * @throws Exception
	 */
	public void setUp() throws Exception {
		m1 = new TestMicroService("m1");
		m2 = new TestMicroService("m2");
		m3 = new TestMicroService("m3");
		r = new TestRequest("m2");
		b = new TestBroadcast("m2");
		messageBus = MessageBusImpl.getInstance();
	}

	@After
	/**
	 * Unregister all the microservices
	 * deleting all their queues 
	 * @throws Exception
	 */
	public void tearDown() throws Exception {
		messageBus.unregister(m1);
		messageBus.unregister(m2);
		messageBus.unregister(m3);	
	}

	@Test
	/**
	 * Make sure an instance of the messageBus was created
	 */
	public void testGetInstance() {
		assertNotNull(messageBus);
	}

	@Test
	/**
	 * m1 subscribes to messages of type TestRequest m1 sends a TestRequest, r
	 * we make sure m1 receives the request r
	 */
	public void testSubscribeRequest() {
		messageBus.subscribeRequest(TestRequest.class, m1);
		messageBus.sendRequest(r, m1);

		try {
			if (messageBus.awaitMessage(m1) == null)
				fail("testSubscribeRequest failed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	/**
	 * m1 and m3 subscribes to messages of type TestBroadcast, we send a
	 * TestBroadcast message, b, We make sure m1 and m3 both receive the message
	 * b
	 */
	public void testSubscribeBroadcast() {
		messageBus.subscribeBroadcast(TestBroadcast.class, m1);
		messageBus.subscribeBroadcast(TestBroadcast.class, m3);
		messageBus.sendBroadcast(b);
		try {
			if ((messageBus.awaitMessage(m1) == null) || (messageBus.awaitMessage(m3) == null))
				fail("testSubscribeRequest failed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	/**
	 * m3 subscribes to requests of type TestRequest, m1 sends a request of type
	 * TestRequest which m3 intercepts we perform complete on that request and
	 * check that m1 receives a RequestCompleted message
	 */
	public void testComplete() {
		messageBus.subscribeRequest(TestRequest.class, m3);
		messageBus.sendRequest(r, m1);
		messageBus.complete(r, "done");
		try {
			Message receivedSecondMessage = messageBus.awaitMessage(m1);
			if (!receivedSecondMessage.getClass().getName().equals("bgu.spl.mics.RequestCompleted"))
				fail("testComplete failed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Test
	/**
	 * m1 and m3 subscribe to receive broadcasts 
	 * we send a broadcast and make sure that both m1 and m2 receive it
	 */
	public void testSendBroadcast() {
		messageBus.subscribeBroadcast(TestBroadcast.class, m1);
		messageBus.subscribeBroadcast(TestBroadcast.class, m3);
		messageBus.sendBroadcast(b);
		try {
			if ((!messageBus.awaitMessage(m1).equals(b) || (!messageBus.awaitMessage(m3).equals(b))))
				fail("testSendBroadcast failed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	/**
	 * m1 subscribes to requests of type TestRequest, and m2 sends a request of
	 * type TestRequest (r) we check that m1 receives the r message
	 */
	public void testSendRequest() {
		messageBus.subscribeRequest(TestRequest.class, m1);
		messageBus.sendRequest(r, m2);
		try {
			if (!messageBus.awaitMessage(m1).equals(r))
				fail("testSendRequest failed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	/**
	 * m3 subscribes to receive messages of type TestRequest m3 unregisters. We
	 * send a request of type TestRequest, which m3 should receive if it was
	 * registered We make sure that the sendRequest method returns false -
	 * meaning there was no one subscribed to this message type (And since m3
	 * did subscribe it means it unregistered)
	 */
	public void testRegister() {
		messageBus.subscribeRequest(TestRequest.class, m3);
		messageBus.unregister(m3);
		boolean responseFromSendRequest;
		responseFromSendRequest = messageBus.sendRequest(r, m1);
		if (responseFromSendRequest == true)
			fail("testRegister failed");
	}

	@Test
	/**
	 * m3 unregisters We attempt to send a request (which m3 should have
	 * received if it was registered) We check that the sending fails (meaning
	 * m3 unregistered sucsessfully)
	 */
	public void testUnregister() {
		messageBus.unregister(m3);

		if (messageBus.sendRequest(r, m1) == true)
			fail("testUnregister failed");

	}

	@Test
	/**
	 * m3 subscribes to requests of type TestRequest m1 sends a request of type
	 * TestRequest (r) We make sure the m3 receives the message we complete the
	 * request r we make sure that m1 receives a message (RequestCompleted)
	 */
	public void testAwaitMessage() {
		messageBus.subscribeRequest(TestRequest.class, m3);
		messageBus.sendRequest(r, m1);
		try {
			if (messageBus.awaitMessage(m3) == null)
				fail("testUnregister failed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		messageBus.complete(r, "done");
		try {
			if (messageBus.awaitMessage(m1) == null)
				fail("testUnregister failed");
		} catch (InterruptedException e) {
			e.printStackTrace();

		}

	}

}
