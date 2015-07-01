package org.csi.yucca.twitterpoller.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.csi.yucca.twitterpoller.constants.SDPTwitterConfig;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;



public class MongoConnectionSingleton {
	private static int anno_init = 0;
	private static int mese_init = 0;
	private static int giorno_init = 0;
	public static MongoConnectionSingleton instance=null;
	private MongoClient mongoConnection=null;
	
	
	private static boolean singletonToRefresh() {
		int curAnno = Calendar.getInstance().get(Calendar.YEAR);
		int curMese = Calendar.getInstance().get(Calendar.MONTH);
		int curGiorno = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		if (curAnno > anno_init) return true;
		else if (curMese > mese_init) return true;
		else if (curGiorno > giorno_init)return true;
		return false;
	}
	public synchronized static MongoConnectionSingleton getInstance() throws Exception{
		if(instance == null || singletonToRefresh()) {
			instance = new MongoConnectionSingleton();
			anno_init = Calendar.getInstance().get(Calendar.YEAR);
			mese_init = Calendar.getInstance().get(Calendar.MONTH);
			giorno_init = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		}
		return instance;
	}
	private MongoConnectionSingleton() throws Exception{
		
		String host=SDPTwitterConfig.getInstance().getHost();
		int port=SDPTwitterConfig.getInstance().getPort();
		
		mongoConnection=getMongoClientLocal(host,port);
		
	}
	public MongoClient getMongoConnection() {
		return mongoConnection;
	}
	private MongoClient getMongoClientLocal(String host, int port) throws Exception{
		StringTokenizer st= new StringTokenizer(host,";",false);
		ArrayList<ServerAddress> arrServerAddr=new ArrayList<ServerAddress>();
		while (st.hasMoreTokens()) {
			String newHost=st.nextToken();
			ServerAddress serverAddr=new ServerAddress(newHost,port);
			arrServerAddr.add(serverAddr);
		}
		MongoCredential credential = MongoCredential.createMongoCRCredential(SDPTwitterConfig.getInstance().getUsr(), 
				"admin", 
				SDPTwitterConfig.getInstance().getPwd().toCharArray());
		MongoClient mongoClient = null;
		mongoClient = new MongoClient(arrServerAddr,Arrays.asList(credential));
		return mongoClient;


	}

}
