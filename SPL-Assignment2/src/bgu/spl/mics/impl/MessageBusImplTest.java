package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.Broadcast;

public class MessageBusImplTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		class MicroServiceImpl extends MicroService{

			public MicroServiceImpl(String name) {
				super(name);
				// TODO Auto-generated constructor stub
			}

			@Override
			protected void initialize() {
				//here i will write to what types of messages i am subscribed to 
			}
			
		}
		
		class RequestImpl implements Request<String>{
			
		}
		
		class BroadcastImpl implements Broadcast{
			
		}
		
		
		Request<?> r = new RequestImpl();
		Broadcast b = new BroadcastImpl();
	}

	/*@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}*/

	/*@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetInstance() {
		fail("Not yet implemented");
	}
*/
	/*@Test
	public void testMessageBusImpl() {
		fail("Not yet implemented");
	}
*/
	
	
	
	@Test
	public void testSubscribeRequest() {
		
		fail("Not yet implemented");
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
