package org.csi.yucca.twitterpoller.constants;

import java.util.Calendar;
import java.util.HashMap;
import java.util.ResourceBundle;



public class SDPTwitterConfig {
	
	
	private static final String SDP_TWITTER_CONSUMER_KEY="SDP_TWITTER_CONSUMER_KEY";
	private static final String SDP_TWITTER_CONSUMER_SECRET="SDP_TWITTER_CONSUMER_SECRET";

	
	private static final String SDP_TWITTER_PROXYHOST="SDP_TWITTER_PROXYHOST";
	private static final String SDP_TWITTER_PROXYPORT="SDP_TWITTER_PROXYPORT";
	
	
	public static SDPTwitterConfig instance=null;
	private static int anno_init = 0;
	private static int mese_init = 0;
	private static int giorno_init = 0;

	private HashMap<String, String> params = new HashMap<String, String>();

	private static boolean singletonToRefresh() {
		int curAnno = Calendar.getInstance().get(Calendar.YEAR);
		int curMese = Calendar.getInstance().get(Calendar.MONTH);
		int curGiorno = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		if (curAnno > anno_init) return true;
		else if (curMese > mese_init) return true;
		else if (curGiorno > giorno_init)return true;
		return false;
	}
	public synchronized static SDPTwitterConfig getInstance() throws Exception{
		if(instance == null || singletonToRefresh()) {
			instance = new SDPTwitterConfig();
			anno_init = Calendar.getInstance().get(Calendar.YEAR);
			mese_init = Calendar.getInstance().get(Calendar.MONTH);
			giorno_init = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		}
		return instance;
	}	
	
	public SDPTwitterConfig() throws Exception{
		ResourceBundle rb= ResourceBundle.getBundle("SDPTwitterConfig");
		params = new HashMap<String, String>();

		params.put(SDP_TWITTER_CONSUMER_KEY, rb.getString(SDP_TWITTER_CONSUMER_KEY));
		params.put(SDP_TWITTER_CONSUMER_SECRET, rb.getString(SDP_TWITTER_CONSUMER_SECRET));

		params.put(SDP_TWITTER_PROXYHOST, rb.getString(SDP_TWITTER_PROXYHOST));
		params.put(SDP_TWITTER_PROXYPORT, rb.getString(SDP_TWITTER_PROXYPORT));
		
		
		
		params.put("SDP_MONGO_CFG_DEFAULT_USER", rb.getString("SDP_MONGO_CFG_DEFAULT_USER"));
		params.put("SDP_MONGO_CFG_DEFAULT_PWD", rb.getString("SDP_MONGO_CFG_DEFAULT_PWD"));
		params.put("SDP_MONGO_CFG_DEFAULT_HOST", rb.getString("SDP_MONGO_CFG_DEFAULT_HOST"));
		params.put("SDP_MONGO_CFG_DEFAULT_PORT", rb.getString("SDP_MONGO_CFG_DEFAULT_PORT"));
		params.put("SDP_MONGO_CFG_DEFAULT_TWTCOLLECTION", rb.getString("SDP_MONGO_CFG_DEFAULT_TWTCOLLECTION"));
		params.put("SDP_MONGO_CFG_DEFAULT_DB", rb.getString("SDP_MONGO_CFG_DEFAULT_DB"));
		
		
		
	}
	
	public String getDb() {
		return params.get("SDP_MONGO_CFG_DEFAULT_DB");

	}
	
	public String getHost() {
		return params.get("SDP_MONGO_CFG_DEFAULT_HOST");

	}
	public String getPwd() {
		return params.get("SDP_MONGO_CFG_DEFAULT_PWD");

	}
	public String getUsr() {
		return params.get("SDP_MONGO_CFG_DEFAULT_USER");

	}
	public String getCollection() {
		return params.get("SDP_MONGO_CFG_DEFAULT_TWTCOLLECTION");

	}
	public int getPort() {
		return Integer.parseInt(params.get("SDP_MONGO_CFG_DEFAULT_PORT"));

	}
	
	public String getTwtConsumerKey() {
		return params.get(SDP_TWITTER_CONSUMER_KEY);
	}
	public String getTwtConsumerSecret() {
		return params.get(SDP_TWITTER_CONSUMER_SECRET);
	}
	
	public String getProxyHost() {
		return params.get(SDP_TWITTER_PROXYHOST);
		
	}
	
	public int getProxyPort() {
		return Integer.parseInt(params.get(SDP_TWITTER_PROXYPORT));
		
	}
	
	
}
