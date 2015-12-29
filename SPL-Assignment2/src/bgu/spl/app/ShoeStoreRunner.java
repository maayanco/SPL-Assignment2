package bgu.spl.app;

import com.google.gson.*;
import com.google.gson.GsonBuilder;

import bgu.spl.app.ManagementService;
import bgu.spl.app.ShoeStorageInfo;
import bgu.spl.app.Store;
import bgu.spl.app.TimeService;
import bgu.spl.app.*;
import bgu.spl.app.SellingService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class ShoeStoreRunner {
	
	private static final Store storeInstance = Store.getInstance();
	
	public static void main(String[] args){
		
		String jsonPath=args[0];

		//here we can get exceptions thrown from the MicroService.. 
		//what should we do with them??
		//
			try {
				
				Gson gson = new Gson();
				
				BufferedReader br = new BufferedReader(new FileReader(jsonPath));

				//convert the json string back to object
				StoreConfiguration obj = gson.fromJson(br, StoreConfiguration.class);
				
				//Load the ShoeStorageInfo onto the store
				Storage[] storage = obj.getInitialStorage();
				ShoeStorageInfo[] shoeStorageInfo = new ShoeStorageInfo[storage.length];
				for(int i=0; i<storage.length; i++){
					shoeStorageInfo[i]=new ShoeStorageInfo(storage[i].getShoeType(), storage[i].getAmount(), 0);
				}
				
				
				storeInstance.load(shoeStorageInfo);
				
				//Get service params
				Service service = obj.getServices();
				Time timeParams = service.getTime();
				Manager managerParams = service.getManager();
				int factoriesNum = service.getFactories();
				int sellingServicesNum = service.getSellers();
				Customer[] customers = service.getCustomers();
				int clientsNum=customers.length;
				int numberOfThreads = 2+sellingServicesNum+factoriesNum+clientsNum;
				
				ExecutorService e = Executors.newFixedThreadPool(numberOfThreads);
				CountDownLatch startLatchObject = new CountDownLatch(numberOfThreads-1);
				CountDownLatch endLatchObject = new CountDownLatch(numberOfThreads);
				
				//Creating the time service
				TimeService timeService = new TimeService(timeParams.getSpeed(), timeParams.getDuration(), startLatchObject, endLatchObject);
				e.execute(timeService);
				
				//creating the managment service
				Discount[] discountArr = managerParams.getDiscountSchedule();
				LinkedList<DiscountSchedule> discountScheduleLst = new LinkedList<DiscountSchedule>();
				for(int i=0; i<discountArr.length; i++){
					DiscountSchedule sch = new DiscountSchedule(discountArr[i].getShoeType(), discountArr[i].getTick(),discountArr[i].getAmount());
					discountScheduleLst.add(sch);
				}
				ManagementService managmentService = new ManagementService(discountScheduleLst, startLatchObject, endLatchObject);
				e.execute(managmentService);
				
				//Creating the factories
				for(int i=1; i<=factoriesNum; i++){
					ShoeFactoryService factory = new ShoeFactoryService("factory "+i, startLatchObject, endLatchObject);
					e.execute(factory);
				}
				
				//Creating the sellers
				for(int i=1; i<=sellingServicesNum; i++){
					SellingService sellingService = new SellingService("seller "+i, startLatchObject, endLatchObject);
					e.execute(sellingService);
				}
				
				//creating the clients
				for(Customer item : customers){
					PurchaseSchedule[] purchaseScheduleArr = item.getPurchaseSchedule();
					List<PurchaseSchedule> purchaseScheduleList = new LinkedList<PurchaseSchedule>();
					for(PurchaseSchedule sche : purchaseScheduleArr){
						purchaseScheduleList.add(sche);
					}
					
					String[] wishArr= item.getWishList();
					Set<String> wishList = new LinkedHashSet<String>();
					for(String str : wishArr){
						wishList.add(str);
					}
					 					
					WebsiteClientService client = new WebsiteClientService(item.getName(), purchaseScheduleList, wishList, startLatchObject, endLatchObject);
					e.execute(client);
				}
				
				
				//Await the end
				endLatchObject.await();
				
				//Terminate all threads
				e.shutdown();
				e.awaitTermination(2, TimeUnit.MINUTES);
				
				synchronized (Logger.class) {
					storeInstance.printReceiptsInHtmlFormat();
				}
				
			
				
				
				//e.shutdownNow();

			} catch (IOException e) {
				e.printStackTrace();
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}

		 
	    }
}
