package org.csi.yucca.realtime.test;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class OdataUtils {

	
	public static  String makeUrl(JSONObject dato, String format) {
	//  String aaurlToCall=dato.get("odata.url")+"/"+dato.get("odata.apicode")+"/"+
//	    dato.get("odata.entityset")+"?"+
//	    "$top="+dato.get("odata.top")+"&"+
//	    "$skip="+dato.get("odata.skip")+"&"+
//	    "$orderby="+dato.get("odata.order")+
//	    "&$format=json";
	  
	  String urlToCall=dato.get("odata.url")+"/"+dato.get("odata.apicode")+"/"+dato.get("odata.entityset");
	  boolean added=false;
	  if (dato.getInt("odata.top")>0) {
	   added=true;
	   urlToCall+="?" + "$top="+dato.get("odata.top");
	  }
	  if (dato.getInt("odata.skip")>0) {
	   urlToCall+=(added ? "&" : "?") + "$skip="+dato.get("odata.skip");
	   added=true;
	  }
	  if (StringUtils.isNotEmpty(dato.getString("odata.orderby"))) {
	   urlToCall+=(added ? "&" : "?") + "$orderby="+dato.get("odata.orderby");
	   added=true;
	  }
	  if (StringUtils.isNotEmpty(dato.getString("odata.filter"))) {
	   urlToCall+=(added ? "&" : "?") + "$filter="+dato.get("odata.filter");
	   added=true;
	  }
	  
	  urlToCall+=(added ? "&" : "?") + "$format="+format;
	  
	  
	  
	  return urlToCall;
	 }
}
