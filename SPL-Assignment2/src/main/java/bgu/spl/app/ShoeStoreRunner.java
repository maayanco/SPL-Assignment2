package bgu.spl.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import bgu.spl.app.json.Customer;
import bgu.spl.app.json.Discount;
import bgu.spl.app.json.Manager;
import bgu.spl.app.json.Service;
import bgu.spl.app.json.Storage;
import bgu.spl.app.json.StoreConfiguration;
import bgu.spl.app.json.Time;
import bgu.spl.app.passive.DiscountSchedule;
import bgu.spl.app.passive.PurchaseSchedule;
import bgu.spl.app.passive.ShoeStorageInfo;
import bgu.spl.app.passive.Store;
import bgu.spl.app.services.ManagementService;
import bgu.spl.app.services.SellingService;
import bgu.spl.app.services.ShoeFactoryService;
import bgu.spl.app.services.TimeService;
import bgu.spl.app.services.WebsiteClientService;

public class ShoeStoreRunner {

	private static final Store storeInstance = Store.getInstance();
	private static final Logger log = Logger.getLogger(ShoeStoreRunner.class.getName());

	public static void main(String[] args) {
		// Format the logs to be more readable
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

		String jsonPath = args[0];

		StoreConfiguration jsonObject = null;

		try {
			/* convert the json string back to object */
			Gson gson = new Gson();
			BufferedReader br = new BufferedReader(new FileReader(jsonPath+".json"));
			jsonObject = gson.fromJson(br, StoreConfiguration.class);

		} catch (FileNotFoundException e2) {
			log.log(Level.SEVERE, " Failed to load file");
		}

		if (jsonObject != null) {
			/* Load the ShoeStorageInfo onto the store */
			Storage[] storage = jsonObject.getInitialStorage();
			ShoeStorageInfo[] shoeStorageInfo = new ShoeStorageInfo[storage.length];
			for (int i = 0; i < storage.length; i++) {
				shoeStorageInfo[i] = new ShoeStorageInfo(storage[i].getShoeType(), storage[i].getAmount(), 0);
			}

			storeInstance.load(shoeStorageInfo);

			/* Get service params */
			Service service = jsonObject.getServices();
			Time timeParams = service.getTime();
			Manager managerParams = service.getManager();
			int factoriesNum = service.getFactories();
			int sellingServicesNum = service.getSellers();
			Customer[] customers = service.getCustomers();
			int clientsNum = customers.length;
			int numberOfThreads = 2 + sellingServicesNum + factoriesNum + clientsNum;

			ExecutorService e = Executors.newFixedThreadPool(numberOfThreads);
			CountDownLatch startLatchObject = new CountDownLatch(numberOfThreads - 1);
			CountDownLatch endLatchObject = new CountDownLatch(numberOfThreads);

			/* Creating the time service */
			TimeService timeService = new TimeService(timeParams.getSpeed(), timeParams.getDuration(), startLatchObject,
					endLatchObject);
			e.execute(timeService);

			/* creating the managment service */
			Discount[] discountArr = managerParams.getDiscountSchedule();
			LinkedList<DiscountSchedule> discountScheduleLst = new LinkedList<DiscountSchedule>();
			for (int i = 0; i < discountArr.length; i++) {
				DiscountSchedule sch = new DiscountSchedule(discountArr[i].getShoeType(), discountArr[i].getTick(),
						discountArr[i].getAmount());
				discountScheduleLst.add(sch);
			}
			ManagementService managmentService = new ManagementService(discountScheduleLst, startLatchObject,
					endLatchObject);
			e.execute(managmentService);

			/* Creating the factories */
			for (int i = 1; i <= factoriesNum; i++) {
				ShoeFactoryService factory = new ShoeFactoryService("factory " + i, startLatchObject, endLatchObject);
				e.execute(factory);
			}

			/* Creating the sellers */
			for (int i = 1; i <= sellingServicesNum; i++) {
				SellingService sellingService = new SellingService("seller " + i, startLatchObject, endLatchObject);
				e.execute(sellingService);
			}

			/* creating the clients */
			for (Customer item : customers) {
				PurchaseSchedule[] purchaseScheduleArr = item.getPurchaseSchedule();
				List<PurchaseSchedule> purchaseScheduleList = new LinkedList<PurchaseSchedule>();
				for (PurchaseSchedule sche : purchaseScheduleArr) {
					purchaseScheduleList.add(sche);
				}

				String[] wishArr = item.getWishList();
				Set<String> wishList = new LinkedHashSet<String>();
				for (String str : wishArr) {
					wishList.add(str);
				}

				WebsiteClientService client = new WebsiteClientService(item.getName(), purchaseScheduleList, wishList,
						startLatchObject, endLatchObject);
				e.execute(client);
			}

			/*
			 * Wait Until all threads finish, print the store stock and receipts
			 */
			try {
				endLatchObject.await();
				e.shutdown();
				storeInstance.print();
			} catch (InterruptedException e1) {
				log.log(Level.WARNING, " ShoeStoreRunner received InterruptedException");
			}
		}

	}
}
