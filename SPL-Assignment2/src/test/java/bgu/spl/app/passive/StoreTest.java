package bgu.spl.app.passive;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import bgu.spl.app.passive.Store;

public class StoreTest {

	private static Store store;

	@BeforeClass
	/**
	 * create an instance of the Store
	 * @throws Exception
	 */
	public static void setUpBeforeClass() throws Exception {
		store = Store.getInstance();
	}

	@Test
	/**
	 * Make sure the instance of the store is not null (meaning the Store was created successfully)
	 */
	public void testGetInstance() {
		assertNotNull(" store is not null", Store.getInstance());
	}

	@Test
	/**
	 * Load an array of ShoeStorageInfo which contains 3 "blue-sandals", 
	 * Successfully attempt to take 3 "blue-sandals" from the store
	 * Then attempt again to take a blue sandal from the store and make sure we receive a 'NOT-IN-STOCK' response
	 */
	public void testLoad() {
		ShoeStorageInfo[] storage = { new ShoeStorageInfo("blue-sandals", 5, 0), };
		store.load(storage);
		BuyResult takeResponse;
		for (int i = 1; i <= 5; i++) {
			takeResponse = store.take("blue-sandals", false);
			if (!takeResponse.equals(BuyResult.REGULAR_PRICE)) {
				fail("testLoad faild");
			}
		}
		takeResponse = store.take("blue-sandals", false);
		if (!takeResponse.equals(BuyResult.NOT_IN_STOCK)) {
			fail("testLoad faild");
		}
	}

	@Test
	/**
	 * Add a shoe of type "green-shoe" which wasn't previously on stock
	 * make sure an attempt to take one "Green-shoe" from stock is successful
	 */
	public void testAdd() {
		store.add("green-shoe", 1);
		BuyResult response = store.take("green-shoe", false);
		if (!response.equals(BuyResult.REGULAR_PRICE)) {
			fail("testAdd faild");
		}
	}

	@Test
	/**
	 * add a discount on a shoe, attempt to take a discounted shoe and make sure it is successful 
	 */
	public void testAddDiscount() {
		ShoeStorageInfo[] storage = { new ShoeStorageInfo("black", 1, 0), };
		store.load(storage);
		store.addDiscount("black", 1);
		BuyResult response = store.take("black", true);
		if (!response.equals(BuyResult.DISCOUNTED_PRICE)) {
			fail("testAddDiscount faild");
		}
	}

	@Test
	/**
	 * Attempt to take from storage:
	 * - When there is a discounted item on stock - receive "BuyResult.DISCOUNTED_PRICE"
	 * - When there is no shoes on discount on stock - receive "BuyResult.NOT_ON_DISCOUNT"
	 * - When there is a shoe in storage - receive "BuyResult.REGULAR_PRICE"
	 * - When there are no shoes on storage - receive "BuyResult.Not_IN_Stock"
	 */
	public void testTake() {
		ShoeStorageInfo[] storage = { new ShoeStorageInfo("red-shoe", 2, 0), };
		store.load(storage);
		store.addDiscount("red-shoe", 1);
		BuyResult response;
		response = store.take("red-shoe", true);
		if (!response.equals(BuyResult.DISCOUNTED_PRICE)) {
			fail("testTake faild");
		}
		response = store.take("red-shoe", true);
		if (!response.equals(BuyResult.NOT_ON_DISCOUNT)) {
			fail("testTake faild");
		}
		response = store.take("red-shoe", false);
		if (!response.equals(BuyResult.REGULAR_PRICE)) {
			fail("testTake faild");
		}
		response= store.take("red-shoe", false);
		if (!response.equals(BuyResult.NOT_IN_STOCK)) {
			fail("testTake faild");
		}
	}

}
