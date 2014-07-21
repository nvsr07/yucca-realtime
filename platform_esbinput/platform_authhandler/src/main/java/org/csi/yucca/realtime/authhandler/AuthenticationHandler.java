package org.csi.yucca.realtime.authhandler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.Handler;
import org.apache.synapse.transport.passthru.util.RelayUtils;

public class AuthenticationHandler implements Handler {
	protected final Map<String, Object> properties;
	private static Log logger = LogFactory.getLog(AuthenticationHandler.class
			.getName());
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

		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
				.getAxis2MessageContext();
		Object headers = axis2MessageContext
				.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

		if (headers == null || !(headers instanceof Map)
				|| ((Map) headers).get("Authorization") == null) {
			if (headers != null && headers instanceof Map) {
				((Map) headers).put("WWW-Authenticate", "Basic realm=\"SDP\"");
			}
			axis2MessageContext.setProperty("HTTP_SC", "401");
			messageContext.setProperty("RESPONSE", "true");
			messageContext.setTo(null);
			Axis2Sender.sendBack(messageContext);
			return false;
		} else {
			Map headersMap = ((Map) headers);
			String authHeader = (String) headersMap.get("Authorization");
			String credentials = authHeader.substring(6).trim();
			if (processSecurity(credentials)) {
				return true;
			} else {

				prepareMessageToResponse(messageContext,"{\"error_name\": \"Authentication Failed\", \"error_code\": \"E002\", \"output\": \"NONE\"}", 
						"403");
				Axis2Sender.sendBack(messageContext);
				return false;
			}

		}
	}

	private void prepareMessageToResponse(MessageContext messageContext, String jsonMessageString, String httpStatusCode) {
		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
				.getAxis2MessageContext();

		messageContext.setProperty("RESPONSE", "true");
		messageContext.setTo(null);
		messageContext.setProperty("NIO-ACK-Requested", "true");
		messageContext.setProperty("ContentType", "application/json");

		// set the HTTP STATUS code
		axis2MessageContext.setProperty("HTTP_SC", httpStatusCode);
		axis2MessageContext.removeProperty("NO_ENTITY_BODY");
		axis2MessageContext.setProperty("messageType",	"application/json");
		axis2MessageContext.setProperty("ContentType",  "application/json");

		// Set the payload to be sent back
		try {
			RelayUtils.buildMessage(axis2MessageContext);

			// This part is only needed for POST requests
			if ("POST".equals(axis2MessageContext.getProperty("HTTP_METHOD"))) {
				messageContext.getEnvelope().getBody().getFirstElement().detach();
				axis2MessageContext.setProperty("ContentType","application/javascript");
				messageContext.setProperty("message.builder.invoked","false");
				axis2MessageContext.setProperty("org.apache.synapse.commons.json.JsonInputStream",null);
			}

			axis2MessageContext.setProperty("JSON_STRING",jsonMessageString);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean handleResponse(MessageContext messageContext) {
		return true;
	}

	public boolean processSecurity(String credentials) {
		String decodedCredentials = new String(new Base64().decode(credentials
				.getBytes()));
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