package org.csi.yucca.twitterpoller.mongo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static final Log log=LogFactory.getLog("org.csi.yucca.twitterpoller");

	public Long retrieveLastTweetId(String streamCode,String tenantCode,String virtualEntityCode,int streamVersion,boolean reset) throws Exception{
		Long ret = null;
		DBCursor  cursor=null;
		try {


			log.info("[YuccaTwitterPoller::retrieveLastTweetId] BEGIN ");

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
				String versioneStream = takeNvlValues(obj.get("streamVersion"));
				try {
					ret = new Long(id);
				} catch (NumberFormatException e) {

				}


				log.info("[YuccaTwitterPoller::retrieveLastTweetId] streamVersion:"+streamVersion+"----versioneStream:"+versioneStream);
				if (!((""+streamVersion).equals(versioneStream))) {
					log.info("[YuccaTwitterPoller::retrieveLastTweetId] CAMBIO VERSIONE ");

					BasicDBList arrQueryUpd = new BasicDBList();

					arrQueryUpd.add(new BasicDBObject("streamCode",streamCode));
					arrQueryUpd.add(new BasicDBObject("tenantcode",tenantCode));
					arrQueryUpd.add(new BasicDBObject("virtualEntityCode",virtualEntityCode));


					BasicDBObject queryUpd= new BasicDBObject("$and",arrQuery);
					BasicDBObject set = new BasicDBObject();
					BasicDBObject doc = new BasicDBObject();
					doc.put("streamVersion",streamVersion);
					if (reset) {
						log.info("[YuccaTwitterPoller::retrieveLastTweetId] RESET");
						ret=null;
						doc.put("tweetLastId", -1);
					}

					set.append("$set", doc);
					
					WriteResult res = coll.update(queryUpd,set);
				}

			} else {
				BasicDBObject doc = new BasicDBObject("streamCode",streamCode);
				doc.append("tenantcode",tenantCode);
				doc.append("virtualEntityCode",virtualEntityCode);
				doc.append("streamVersion",streamVersion);
				WriteResult res = coll.insert(doc);

			}

			cursor.close();

			log.info("[YuccaTwitterPoller::retrieveLastTweetId] END ");

		} catch (Exception e) {
			log.error( "[YuccaTwitterPoller::retrieveLastTweetId]  "+e);
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


			log.info("[YuccaTwitterPoller::updateLastId] BEGIN ");

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
			log.error( "[YuccaTwitterPoller::updateLastId]  "+e);
			throw e;
		} finally {
			log.info("[YuccaTwitterPoller::updateLastId] END ");
		}
		return true;


	}
	private static String takeNvlValues(Object obj) {
		if (null==obj) return null;
		else return obj.toString();
	}

}
