/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yucca.realtime.adaptor.output.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.yucca.realtime.adaptor.output.mongodb.internal.util.JSONCallbackTimeZone;
import org.yucca.realtime.adaptor.output.mongodb.internal.util.MongoDBOutEventAdaptorConstants;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;


public final class MongoDBOutEventAdaptorType extends AbstractOutputEventAdaptor {

	private static final Log log = LogFactory.getLog(MongoDBOutEventAdaptorType.class);
    private ResourceBundle resourceBundle;

    private Map<ServerAddress, MongoClient> mongoClients = new HashMap<ServerAddress, MongoClient>();
    
    private Cache<String, DBObject> datasetCache = null;
    
    public MongoDBOutEventAdaptorType() {
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
    	CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager("CEPManager");       
        CacheBuilder<String, DBObject> cacheBuilder  = cacheManager.<String, DBObject>createCacheBuilder("Dataset") ;
        cacheBuilder.setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, 600)).setStoreByValue(false);
    	this.datasetCache =  cacheBuilder.build();
    	log.info("Cache initialized: "+(datasetCache==null?"false":"true ["+datasetCache+"]"));
	}
    
    @Override
    protected String getName() {
        return MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_TYPE_MONGODB;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        return new ArrayList<String>(Arrays.asList(new String[]{"json","text"}));  
    }

    @Override
    protected void init() {
        this.resourceBundle = ResourceBundle.getBundle("org.yucca.realtime.adaptor.output.mongodb.i18n.Resources", Locale.getDefault());
    }

    
    
    @Override
    protected List<Property> getOutputAdaptorProperties() {
    	List<Property> propertyList = new ArrayList<Property>();

        Property prodDB = new Property(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBURL);
        prodDB.setDisplayName(
                resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBURL));
        prodDB.setRequired(true);
        prodDB.setHint(resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBURL));
        
        Property prodPort = new Property(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBPORT);
        prodPort.setDisplayName(
                resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBPORT));
        prodPort.setRequired(true);
        prodPort.setHint(resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBPORT));

        propertyList.add(prodDB);
        propertyList.add(prodPort);

        return propertyList;
    }

    @Override
    protected List<Property> getOutputMessageProperties() {
   	 List<Property> propertyList = new ArrayList<Property>();

     Property prodDB = new Property(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_DATABASE);
     prodDB.setDisplayName(
             resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_DATABASE));
     prodDB.setRequired(true);
     prodDB.setHint(resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_DATABASE));

     Property prodUser = new Property(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_USERNAME);
     prodUser.setDisplayName(
             resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_USERNAME));
     prodUser.setRequired(true);
     prodUser.setHint(resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_USERNAME));
     
     Property prodPwd = new Property(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PASSWORD);
     prodPwd.setDisplayName(
             resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PASSWORD));
     prodPwd.setRequired(true);
     prodPwd.setHint(resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PASSWORD));

     
     
     Property prodColl = new Property(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_COLLECTION);
     prodColl.setDisplayName(
             resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_COLLECTION));
     prodColl.setRequired(true);
     prodColl.setHint(resourceBundle.getString(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_COLLECTION));
     
     propertyList.add(prodDB);
     propertyList.add(prodUser);
     propertyList.add(prodPwd);
     
     propertyList.add(prodColl);

     return propertyList;

    }

    @Override
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object o, OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
            int tenantId) {
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
    	
    	MongoClient mongoClient =null;
    	ServerAddress reqMongo = null;
    	
    	String mongodbIPAddress=outputEventAdaptorConfiguration.getOutputProperties().get(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBURL);
        String mongodbPort=outputEventAdaptorConfiguration.getOutputProperties().get(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBPORT);

        String mongodbDatabase=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_DATABASE);
        String mongodbUsername=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_USERNAME);
        String mongodbPassword=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PASSWORD);
        String mongodbCollection=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_COLLECTION);

        log.debug("Configurations -"+mongodbIPAddress+":"+mongodbPort+":"+mongodbDatabase+":"+mongodbCollection);
        log.debug("Output -"+o.toString());
        log.debug("Output Type-"+o.getClass().getName());    	
    	
    	
    	try {
    		reqMongo = new ServerAddress(mongodbIPAddress,Integer.parseInt(mongodbPort));
		} catch (UnknownHostException e) {
			log.error("Impossible to connect MongoDB on "+reqMongo.toString());
			return;
		}

    	if (mongoClients.containsKey(reqMongo))
    		mongoClient = mongoClients.get(reqMongo);
    	else
    	{
    		MongoCredential credential = MongoCredential.createMongoCRCredential(mongodbUsername, "admin", mongodbPassword.toCharArray());
			mongoClient = new MongoClient(reqMongo,Arrays.asList(credential));
			mongoClients.put(reqMongo, mongoClient);
    	}
    	
    	// Get info from event (already formatted from CEP)
    	DBObject dbo = ((DBObject)JSON.parse(o.toString(),new JSONCallbackTimeZone()));
    	
    	String tenantCode = (String)dbo.get("tenantCode");
    	String streamCode = (String)dbo.get("streamCode");
    	String virtualEntityCode = (String)dbo.get("virtualEntityCode");
    	
    	log.debug("stream:"+tenantCode+":"+virtualEntityCode+":"+streamCode);   

    	Integer idDataset = -1;
    	Integer datasetVersion = -1;
		String db = mongodbDatabase;
    	
    	
    	DBObject datasetInfo = getDatasetInfo(mongoClient,tenantCode,streamCode,virtualEntityCode);
    	
    	if (datasetInfo!=null)
    	{
    		DBObject configData = ((DBObject)datasetInfo.get("configData"));
    		idDataset = (Integer) configData.get("idDataset");
    		datasetVersion = (Integer) configData.get("datasetVersion");
    		db = "DB_"+( (String) configData.get("tenantCode"));
    	}
    	else {
    		log.error("Dataset non trovato per idStream:"+tenantCode+":"+virtualEntityCode+":"+streamCode+" and object "+ o.toString());
    	}
    	
    	DB database  = mongoClient.getDB(db);
    	DBCollection coll = database.getCollection(mongodbCollection);
    	dbo.put("idDataset", idDataset);
    	dbo.put("datasetVersion", datasetVersion);
    	dbo.removeField("tenantCode");
    	dbo.removeField("virtualEntityCode");
    	coll.insert(dbo);
    	// 
    	
    	
    }


	private DBObject getDatasetInfo(MongoClient mongoClient, String tenantCode,
			String streamCode, String virtualEntityCode) {

		DBObject cached=null; 
		if (datasetCache!=null) {
			cached = datasetCache.get(tenantCode+":"+virtualEntityCode+":"+streamCode);
		}
		
		if (cached!=null)
		{
			log.info("Elemento trovato in cache.");
			return cached;
		}
		else {
			log.info("Elemento NON trovato in cache.");
	    	DB dbSupport = mongoClient.getDB("DB_SUPPORT");
	    	BasicDBObject query = new BasicDBObject("streamCode", streamCode).append("configData.tenantCode", tenantCode).append("streams.stream.virtualEntityCode", virtualEntityCode);
	    	DBCollection metaStreamCollection = dbSupport.getCollection("stream");
	    	
	    	DBCursor cursor = metaStreamCollection.find(query);
	    	DBObject ret = null;
	    	try {
	    	   while(cursor.hasNext()) {
	    		   ret = cursor.next();
	    	   }
	    	} finally {
	    	   cursor.close();
	    	}
	    	if (ret!=null) {
	        	log.info("Is cache initialized? : "+(datasetCache==null?"false":"true ["+datasetCache+"]"));
	        	if (datasetCache!=null)
	        		datasetCache.put(tenantCode+":"+virtualEntityCode+":"+streamCode,ret);
	        	else 
		        	log.info("Cache null!");	        		
	    	}
			return ret;
		}
		
		
	}

	@Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
    	ServerAddress reqMongo = null;
    	try {
    	reqMongo = new ServerAddress(outputEventAdaptorConfiguration.getOutputProperties().get(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBURL),
    			Integer.parseInt(outputEventAdaptorConfiguration.getOutputProperties().get(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBPORT)));
		} catch (UnknownHostException e) {
			log.error("Impossible to connect MongoDB on "+reqMongo.toString());
			throw new RuntimeException(e.getMessage());
		}
    }
    

    
}
