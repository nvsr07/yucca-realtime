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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.yucca.realtime.adaptor.output.mongodb.internal.util.MongoDBOutEventAdaptorConstants;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;


public final class MongoDBOutEventAdaptorType extends AbstractOutputEventAdaptor {

	private static final Log log = LogFactory.getLog(MongoDBOutEventAdaptorType.class);
    private ResourceBundle resourceBundle;

    private Map<ServerAddress, MongoClient> mongoClients = null;
    
    @Override
    protected String getName() {
        return MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_TYPE_MONGODB;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        return new ArrayList<String>(Arrays.asList(new String[]{"json"}));  
    }

    @Override
    protected void init() {
        this.resourceBundle = ResourceBundle.getBundle("org.yucca.realtime.adaptor.output.mongodb.i18n.Resources", Locale.getDefault());
        mongoClients = new HashMap<ServerAddress, MongoClient>();
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
    	
    	MongoClient mongoClient =null;
    	ServerAddress reqMongo = null;
    	
    	String mongodbIPAddress=outputEventAdaptorConfiguration.getOutputProperties().get(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBURL);
        String mongodbPort=outputEventAdaptorConfiguration.getOutputProperties().get(MongoDBOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_DBPORT);

        String mongodbDatabase=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_DATABASE);
        String mongodbUsername=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_USERNAME);
        String mongodbPassword=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PASSWORD);
        String mongodbCollection=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MongoDBOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_COLLECTION);

        log.info("Configurations -"+mongodbIPAddress+":"+mongodbPort+":"+mongodbDatabase+":"+mongodbCollection);
        log.info("Output -"+o.toString());
        log.info("Output Type-"+o.getClass().getName());    	
    	
    	
    	
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
				mongoClient = new MongoClient(reqMongo);
				mongoClients.put(reqMongo, mongoClient);
    	}
    	
    	DB database  = createDB(mongoClient, mongodbDatabase, mongodbUsername, mongodbPassword);
    	DBCollection coll = createCollection(database, mongodbCollection);
    	
    	
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
    
    
	public DB createDB(MongoClient mongo, String DBname, String username, String password){
		/**** Get database ****/
		// if database doesn't exists, MongoDB will create it for you
		DB db = mongo.getDB(DBname);		
		if(username!=null && password!=null){
			if(db.authenticate(username, password.toCharArray()))
				return db;	
			else{
				System.out.println("Cant create DB, authorization needed!");
				return null;
			}
		}else{
			return db;
		}

	}

	public DBCollection createCollection(DB db,String collectionName){
		if(db!=null){
			/**** Get collection / table from 'db' ****/
			// if collection doesn't exists, MongoDB will create it for you
			//db.command("db."+collectionName+".insert({"+"nome:pippo})");
			return db.getCollection(collectionName);
		}
		System.out.println("Select Database!!");
		return null;
	}
    
    
}
