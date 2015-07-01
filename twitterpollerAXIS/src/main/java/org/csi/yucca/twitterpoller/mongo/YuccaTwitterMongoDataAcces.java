package org.csi.yucca.twitterpoller.mongo;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csi.yucca.twitterpoller.constants.SDPTwitterConfig;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class YuccaTwitterMongoDataAcces {

	private static final Logger log=Logger.getLogger("org.csi.yucca.twitterpoller");

	public Long retrieveLastTweetId(String streamCode,String tenantCode,String virtualEntityCode) throws Exception{
		Long ret = null;
		DBCursor  cursor=null;
		try {
		
		
		log.log(Level.INFO, "[YuccaTwitterPoller::retrieveLastTweetId] BEGIN ");
		
		MongoClient mongoClient=MongoConnectionSingleton.getInstance().getMongoConnection();
		DB db = mongoClient.getDB(SDPTwitterConfig.getInstance().getDb());
		DBCollection coll = db.getCollection(SDPTwitterConfig.getInstance().getCollection());
		
		
		BasicDBList arrQuery = new BasicDBList();
		
		arrQuery.add(new BasicDBObject("streamCode",streamCode));
		arrQuery.add(new BasicDBObject("tenantcode",tenantCode));
		arrQuery.add(new BasicDBObject("virtualEntityCode",virtualEntityCode));

		
		BasicDBObject query= new BasicDBObject("$and",arrQuery);
		cursor = coll.find(query);		
		if (cursor.hasNext()) {
			DBObject obj=cursor.next();
			String id=takeNvlValues(obj.get("tweetLastId"));
			ret = new Long(id);
			
		} else {
			BasicDBObject doc = new BasicDBObject("streamCode",streamCode);
			doc.append("tenantcode",tenantCode);
			doc.append("virtualEntityCode",virtualEntityCode);
			WriteResult res = coll.insert(doc);
			
		}
		
		cursor.close();
		
		log.log(Level.INFO, "[YuccaTwitterPoller::retrieveLastTweetId] END ");

		} catch (Exception e) {
			log.log(Level.SEVERE, "[YuccaTwitterPoller::retrieveLastTweetId]  "+e);
			throw e;
		} finally {
			try {
				cursor.close();
			} catch (Exception e ) {}
		}
		return ret;

		
	}
	
	
	public boolean updateLastId(String streamCode,String tenantCode,String virtualEntityCode,Long lastId) throws Exception{
		try {
		
		
		log.log(Level.INFO, "[YuccaTwitterPoller::updateLastId] BEGIN ");
		
		MongoClient mongoClient=MongoConnectionSingleton.getInstance().getMongoConnection();
		DB db = mongoClient.getDB(SDPTwitterConfig.getInstance().getDb());
		DBCollection coll = db.getCollection(SDPTwitterConfig.getInstance().getCollection());
		
		
		BasicDBList arrQuery = new BasicDBList();
		
		arrQuery.add(new BasicDBObject("streamCode",streamCode));
		arrQuery.add(new BasicDBObject("tenantcode",tenantCode));
		arrQuery.add(new BasicDBObject("virtualEntityCode",virtualEntityCode));

		
		BasicDBObject query= new BasicDBObject("$and",arrQuery);
		
		
		
		BasicDBObject doc = new BasicDBObject();
		
		doc.append("$set",new BasicDBObject("tweetLastId", lastId));
		
		WriteResult res = coll.update(query,doc);
		//System.out.println(res.getN());
		if (res.getN()<1) return false;
		
		
		
		
	
		

		} catch (Exception e) {
			log.log(Level.SEVERE, "[YuccaTwitterPoller::updateLastId]  "+e);
			throw e;
		} finally {
			log.log(Level.INFO, "[YuccaTwitterPoller::updateLastId] END ");
		}
		return true;

		
	}
	private static String takeNvlValues(Object obj) {
		if (null==obj) return null;
		else return obj.toString();
	}

}
