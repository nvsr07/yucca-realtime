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

package org.yucca.realtime.adaptor.output.phoenix;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.phoenix.queryserver.client.ThinClientUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.yucca.realtime.adaptor.output.phoenix.internal.util.JSONCallbackTimeZone;
import org.yucca.realtime.adaptor.output.phoenix.internal.util.PhoenixOutEventAdaptorConstants;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;


public final class PhoenixOutEventAdaptorType extends AbstractOutputEventAdaptor {

	private static final String CACHE_NAME_DATASET = "Dataset";
	private static final Log log = LogFactory.getLog(PhoenixOutEventAdaptorType.class);
	private ResourceBundle resourceBundle;

	private Map<ServerAddress, MongoClient> mongoClients = new HashMap<ServerAddress, MongoClient>();
	private BaseCache<String, DBObject> datasetCache = null;


	public PhoenixOutEventAdaptorType() {
		PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
		privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
		this.datasetCache = new BaseCache<String, DBObject>(CACHE_NAME_DATASET,60*5);
		log.info("Cache initialized: "+(datasetCache==null?"false":"true ["+datasetCache+"]"));

	}


	@Override
	protected String getName() {
		return PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_TYPE_PHOENIX;
	}

	@Override
	protected List<String> getSupportedOutputMessageTypes() {
		return new ArrayList<String>(Arrays.asList(new String[]{"json","text"}));  
	}

	@Override
	protected void init() {
		this.resourceBundle = ResourceBundle.getBundle("org.yucca.realtime.adaptor.output.phoenix.i18n.Resources", Locale.getDefault());
	}



	@Override
	protected List<Property> getOutputAdaptorProperties() {
		List<Property> propertyList = new ArrayList<Property>();

		Property prodDB = new Property(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBURL);
		prodDB.setDisplayName(
				resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBURL));
		prodDB.setRequired(true);
		prodDB.setHint(resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBURL));

		Property prodPort = new Property(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBPORT);
		prodPort.setDisplayName(
				resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBPORT));
		prodPort.setRequired(true);
		prodPort.setHint(resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBPORT));

		propertyList.add(prodDB);
		propertyList.add(prodPort);

		return propertyList;
	}

	@Override
	protected List<Property> getOutputMessageProperties() {
		List<Property> propertyList = new ArrayList<Property>();


		Property prodUser = new Property(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_MONGODBUSERNAME);
		prodUser.setDisplayName(
				resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_MONGODBUSERNAME));
		prodUser.setRequired(true);
		prodUser.setHint(resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_MONGODBUSERNAME));

		Property prodPwd = new Property(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_MONGODBPASSWORD);
		prodPwd.setDisplayName(
				resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_MONGODBPASSWORD));
		prodPwd.setRequired(true);
		prodPwd.setHint(resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_MONGODBPASSWORD));



		Property prodTable = new Property(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PHOENIXTABLE);
		prodTable.setDisplayName(
				resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PHOENIXTABLE));
		prodTable.setRequired(true);
		prodTable.setHint(resourceBundle.getString(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PHOENIXTABLE));

		propertyList.add(prodUser);
		propertyList.add(prodPwd);

		propertyList.add(prodTable);

		return propertyList;

	}

	@Override
	public void removeConnectionInfo(
			OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
			OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
		log.info("Nothing to do in removeConnectionInfo");
	}



	@Override
	public void publish(
			OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
			Object message, OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
			int tenantId) {
		PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
		privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

		MongoClient mongoClient =null;
		ServerAddress reqMongo = null;

		Connection conn = null;
		PreparedStatement stmt = null;
		String mongodbIPAddress=outputEventAdaptorConfiguration.getOutputProperties().get(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBURL);
		String mongodbPort=outputEventAdaptorConfiguration.getOutputProperties().get(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBPORT);

		String mongodbDatabase=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get("DB_SUPPORT");
		String mongodbUsername=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_MONGODBUSERNAME);
		String mongodbPassword=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_MONGODBPASSWORD);
		String phoenixTable=outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(PhoenixOutEventAdaptorConstants.EVENT_MESSAGE_CONF_FIELD_PHOENIXTABLE);

		log.debug("Configurations -"+mongodbIPAddress+":"+mongodbPort+":"+mongodbDatabase+":"+phoenixTable);
		log.debug("Output -"+message.toString());
		log.debug("Output Type-"+message.getClass().getName());    	


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
			log.info("MongoClient not cached. Created new istance.");   
		}

		try {

			Class.forName("org.apache.phoenix.queryserver.client.Driver");

			conn = DriverManager.getConnection(ThinClientUtil.getConnectionUrl("sdnet-edge1.sdp.csi.it", 8765)+";serialization=PROTOBUF");

			// Get info from event (already formatted from CEP)
			DBObject dbo = ((DBObject)JSON.parse(message.toString(),new JSONCallbackTimeZone()));
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
				log.error("Dataset non trovato per idStream:"+tenantCode+":"+virtualEntityCode+":"+streamCode+" and object "+ message.toString());
			}

			String sql = "UPSERT INTO " +phoenixTable+ "  VALUES (?, ?, ?, ?)";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, idDataset);
			stmt.setInt(2, datasetVersion);
			stmt.setString(3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbo.get("time")));
			stmt.setDouble(4, Double.parseDouble((String)dbo.get("value")));
			stmt.executeUpdate();
			conn.commit();
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			cleanupConnections(stmt, conn);
		}
		//    	DB database  = mongoClient.getDB(db);
		//    	DBCollection coll = database.getCollection(mongodbCollection);
		//    	dbo.put("idDataset", idDataset);
		//    	dbo.put("datasetVersion", datasetVersion);
		//    	dbo.removeField("tenantCode");
		//    	dbo.removeField("virtualEntityCode");
		//		coll.insert(dbo, WriteConcern.UNACKNOWLEDGED);
		// 


	}

	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;
		MongoClient mongoClient =null;
		ServerAddress reqMongo = null;
		try {
			conn = DriverManager.getConnection("jdbc:phoenix:thin:url=http://sdnet-edge1.sdp.csi.it:8765;serialization=PROTOBUF");

			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(new Date()));
			conn.setAutoCommit(true);


			try {
				reqMongo = new ServerAddress("int-sdnet-up1.sdp.csi.it",27017);
			} catch (UnknownHostException e) {
				log.error("Impossible to connect MongoDB on "+reqMongo.toString());
				return;
			}
			MongoCredential credential = MongoCredential.createMongoCRCredential("discovery", "admin", "mypass".toCharArray());
			mongoClient = new MongoClient(reqMongo,Arrays.asList(credential));

			DB dbSupport = mongoClient.getDB("DB_SUPPORT");
			DB dbArpaRumore = mongoClient.getDB("DB_tst_arpa_rumore");
			DBCollection measures = dbSupport.getCollection("measures");

			BasicDBObject query = new BasicDBObject("configData.tenantCode", "tst_arpa_rumore");
			DBCollection metadataCollection = dbSupport.getCollection("metadata");

			DBCursor cursor = metadataCollection.find(query).limit(1);
			BasicDBObject ret = null;
			try {
				while(cursor.hasNext()) {
					ret=(BasicDBObject) cursor.next();
					System.out.println("Starting dataset "+ ret.get("datasetCode"));

					int idds=((Integer)ret.get("idDataset")).intValue();
					int version=((Integer)ret.get("datasetVersion")).intValue();

					BasicDBList campi = (BasicDBList) ret.get("info.fields");
					insertDataset(conn, measures, idds, version, campi);
					
					//RECUPERO I DATI 
				}
			} finally {
				cursor.close();
			}




			Class.forName("org.apache.phoenix.queryserver.client.Driver");


			String sql = "select * from s02_immutable where time_event = TO_DATE('2016-02-16 00:00:00', 'yyyy-MM-dd HH:mm:ss') limit 1 ";

			stmt = conn.createStatement();

			ResultSet res = stmt.executeQuery(sql);

			while (res.next()) {
				System.out.println(res.getString(1));
			}

			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(new Date()));

			// ResultSet rs = conn.createStatement().executeQuery("select * from TRFL");
			// while (rs.next())
			// System.out.println(rs.getInt(1));

		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
			try {
				System.out.println("rollback!");
				conn.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			cleanupConnections(stmt, conn);
		}

	}


	
	private static void insertDataset(Connection conn,DBCollection measures,  int idds, int datasetVersion, BasicDBList campi ) throws Exception{
		BasicDBList elementiquery= new BasicDBList();
		elementiquery.add(new BasicDBObject("idDataset", idds));
		elementiquery.add(new BasicDBObject("datasetVersion", idds));
		
		BasicDBObject queryMeasure = new BasicDBObject("$and",elementiquery);
		DBCursor misureCur=measures.find(queryMeasure).limit(2);
		String campiSQL="iddataset, datasetversion, tempo, ids, objectid, sensor, streamcode";
		String valuesSql= "(?, ?, ? ,? ,? ,? ,? ";
		for (int i=0;i<campi.size();i++) {
			String nome=((BasicDBObject)campi.get(i)).getString("fieldName");
			String tipo=((BasicDBObject)campi.get(i)).getString("dataType");
			campiSQL+=","+nome;
			
			if ("int".equalsIgnoreCase(tipo)) {
				campiSQL+=" INTEGER";
			} else if ("long".equalsIgnoreCase(tipo)) {
				campiSQL+=" BIGINT";
			} else if ("double".equalsIgnoreCase(tipo)) {
				campiSQL+=" DOUBLE";
			} else if ("float".equalsIgnoreCase(tipo)) {
				campiSQL+=" FLOAT";
			} else if ("string".equalsIgnoreCase(tipo)) {
				campiSQL+=" VARCHAR";
			} else if ("boolean".equalsIgnoreCase(tipo)) {
				campiSQL+=" BOOLEAN";
			} else if ("datetime".equalsIgnoreCase(tipo)) {
				campiSQL+=" DATE";
			} else if ("longitude".equalsIgnoreCase(tipo)) {
				campiSQL+=" DOUBLE";
			} else if ("latitude".equalsIgnoreCase(tipo)) {
				campiSQL+=" DOUBLE";
			} 
			
			
			valuesSql+=",?";
			
		}
		
		String sql = "UPSERT INTO DB_TST_ARPA_RUMORE.MEASURES ("+campiSQL+")  VALUES  "+valuesSql+") ";
		while  (misureCur.hasNext()) {
			BasicDBObject ret=(BasicDBObject)misureCur.next();
			
			PreparedStatement stmt=conn.prepareStatement(sql);
			stmt.setInt(1,  idds);
			stmt.setInt(2,  datasetVersion);
			//stmt.setDate(3, ret.getDate("timestamp"));
			//stmt.s
			stmt.setString(4, ret.getString("_id"));
			stmt.setString(5, ret.getString("sensor"));
			stmt.setString(6, ret.getString("stream"));
			
			
			int pos=6;
			for (int i=0;i<campi.size();i++) {
				String nome=((BasicDBObject)campi.get(i)).getString("fieldName");
				String tipo=((BasicDBObject)campi.get(i)).getString("dataType");
				
				if ("int".equalsIgnoreCase(tipo)) {
					stmt.setInt(pos, ret.getInt(nome));
				} else if ("long".equalsIgnoreCase(tipo)) {
					stmt.setLong(pos, ret.getLong(nome));
				} else if ("double".equalsIgnoreCase(tipo)) {
					stmt.setDouble(pos, ret.getDouble(nome));
				} else if ("float".equalsIgnoreCase(tipo)) {
					stmt.setFloat(pos, ((Float)ret.get(nome)).floatValue() );
				} else if ("string".equalsIgnoreCase(tipo)) {
					stmt.setString(pos, ret.getString(nome));
				} else if ("boolean".equalsIgnoreCase(tipo)) {
					stmt.setBoolean(pos, ret.getBoolean(nome));
				} else if ("datetime".equalsIgnoreCase(tipo)) {
					//stmt.setBoolean(pos, ret.getBoolean(nome));
				} else if ("longitude".equalsIgnoreCase(tipo)) {
					stmt.setDouble(pos, ret.getDouble(nome));
				} else if ("latitude".equalsIgnoreCase(tipo)) {
					stmt.setDouble(pos, ret.getDouble(nome));
				} 
				
				pos++;
				
			}
			
			stmt.executeUpdate();
			
			
		}
		misureCur.close();

		conn.commit();			
		
	}

	private DBObject getDatasetInfo(MongoClient mongoClient, String tenantCode,
			String streamCode, String virtualEntityCode) {

		DBObject cached=null; 
		if (datasetCache!=null) {
			cached = datasetCache.getValueFromCache(tenantCode+":"+virtualEntityCode+":"+streamCode);
		}

		if (cached!=null)
		{
			log.debug("Elemento trovato in cache.");
			return cached;
		}
		else {
			log.debug("Elemento NON trovato in cache.");
			DB dbSupport = mongoClient.getDB("DB_SUPPORT");
			BasicDBObject query = new BasicDBObject("streamCode", streamCode).append("configData.tenantCode", tenantCode).append("streams.stream.virtualEntityCode", virtualEntityCode);
			DBCollection metaStreamCollection = dbSupport.getCollection("stream");

			DBCursor cursor = metaStreamCollection.find(query).sort(new BasicDBObject("streams.stream.deploymentVersion",-1)).limit(1);
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
					datasetCache.addToCache(tenantCode+":"+virtualEntityCode+":"+streamCode,ret);
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
			reqMongo = new ServerAddress(outputEventAdaptorConfiguration.getOutputProperties().get(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBURL),
					Integer.parseInt(outputEventAdaptorConfiguration.getOutputProperties().get(PhoenixOutEventAdaptorConstants.EVENT_ADAPTOR_CONF_FIELD_MONGODBPORT)));
		} catch (UnknownHostException e) {
			log.error("Impossible to connect MongoDB on "+reqMongo.toString());
			throw new RuntimeException(e.getMessage());
		}
	}


	private static void cleanupConnections(Statement stmt, Connection con) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.error("unable to close statement", e);
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				log.error("unable to close connection", e);
			}
		}
	}
}
