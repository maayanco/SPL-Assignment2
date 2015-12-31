package bgu.spl.mics.impl;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;

public class MessageBusImplTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		class RequestImpl implements Request<String> {

		}

		class BroadcastImpl implements Broadcast {

		}

		Request<?> r = new RequestImpl();
		Broadcast b = new BroadcastImpl();
	}

	/*
	 * @AfterClass public static void tearDownAfterClass() throws Exception { }
	 */

	/*
	 * @Before public void setUp() throws Exception { }
	 * 
	 * @After public void tearDown() throws Exception { }
	 * 
	 * @Test public void testGetInstance() { fail("Not yet implemented"); }
	 */
	/*
	 * @Test public void testMessageBusImpl() { fail("Not yet implemented"); }
	 */

	@Test
	public void testSubscribeRequest() {
		class MicroServiceImpl extends MicroService {

			public MicroServiceImpl(String name) {
				super(name);
				// TODO Auto-generated constructor stub
			}

			@Override
			protected void initialize() {
				subscribeBroadcast();
			}

		}

		MicroServiceImpl m = new MicroServiceImpl("ma");

		MessageBusImpl messageBusInstance = MessageBusImpl.getInstance();
		try {
			System.out.println("yooo lital how are you today, this is meee ");

			Message message = messageBusInstance.awaitMessage(m);

			message.wait(1);

			if (message == null)
				fail("yooooooooo Litalllllllllll what's upppppppppppppp");
			else
				fail("hhhhhhhh");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testSubscribeBroadcast() {
		fail("Not yet implemented");
	}

	@Test
	public void testComplete() {
		fail("Not yet implemented");
	}

	@Test
	public void testSendBroadcast() {
		fail("Not yet implemented");
	}

	@Test
	public void testSendRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testRegister() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnregister() {
		fail("Not yet implemented");
	}

	@Test
	public void testAwaitMessage() {
		fail("Not yet implemented");
	}

}
