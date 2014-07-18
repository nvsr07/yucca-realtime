package org.csi.yucca.realtime.authhandler;


import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.Handler;

public class AuthenticationHandler implements Handler {
	protected final Map<String, Object> properties;
	private static Log logger = LogFactory.getLog(AuthenticationHandler.class.getName());
	private String username = "a";
	private String password = "b";
	
	public AuthenticationHandler() {
		this.properties = new HashMap();
	}
	
	public void addProperty(String name, Object value) {
		this.properties.put(name, value);
	}

	public Map getProperties() {
		return this.properties;
	}

    public boolean handleRequest(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext
                = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object headers = axis2MessageContext.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);


        if (headers != null && headers instanceof Map) {
            Map headersMap = (Map) headers;
            if (headersMap.get("Authorization") == null) {
                headersMap.clear();
                axis2MessageContext.setProperty("HTTP_SC", "401");
                headersMap.put("WWW-Authenticate", "Basic realm=\"WSO2 ESB\"");
                messageContext.setProperty("RESPONSE", "true");
                messageContext.setTo(null);
                String out = "{ \"error_name\": Authentication Failed, \"error_code\": E002, \"output\": NONE}";
                JsonUtil.newJsonPayload(axis2MessageContext, out, true, true);
                Axis2Sender.sendBack(messageContext);
                return false;

            } else {
                String authHeader = (String) headersMap.get("Authorization");
                String credentials = authHeader.substring(6).trim();
                if (processSecurity(credentials)) {
                    return true;
                } else {
//                	handleAuthFailure(messageContext);
                	
                	headersMap.clear();
                    axis2MessageContext.setProperty("HTTP_SC", "403");
                    axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
                    axis2MessageContext.setProperty("RESPONSE", "true");
                    axis2MessageContext.setTo(null);
                    String out = "{\"error_name\": \"Authentication Failed\", \"error_code\": \"E002\", \"output\": \"NONE\"}";
                    OMElement om = JsonUtil.newJsonPayload(axis2MessageContext, out, true, true);
                    System.out.println("MESSAGGIO a2:["+axis2MessageContext.getEnvelope().getBody().getFirstElement()+"]");
                    System.out.println("Vers: 0.1a");
                    Axis2Sender.sendBack(messageContext);
                    return false;
                }
            }
        } 
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    public boolean processSecurity(String credentials) {
        String decodedCredentials = new String(new Base64().decode(credentials.getBytes()));
        String userName = decodedCredentials.split(":")[0];
        String password = decodedCredentials.split(":")[1];
        if (this.username.equals(userName) && this.password.equals(password)) {
            return true;
        } else {
            return false;
        }
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	

	
    
}