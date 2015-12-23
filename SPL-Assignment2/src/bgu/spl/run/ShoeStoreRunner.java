package bgu.spl.run;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class ShoeStoreRunner {
	public static void main(String[] args){
	
		try{
			BufferedReader br = new BufferedReader(new FileReader("c:\file.json"));
			Gson gson = new Gson();
			StoreConfiguration p = gson.fromJson(br, StoreConfiguration.class);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
