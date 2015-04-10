package org.csi.yucca.realtime.authhandler;

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.Handler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

public class ISAuthenticationHandler implements Handler {
	protected final Map<String, Object> properties;
	private static Log logger = LogFactory.getLog(ISAuthenticationHandler.class
			.getName());
	private String isAdminUsername = "";
	private String isAdminPassword = "";
	private String isServerUrl = "";

    String remoteUserStoreManagerAuthCookie = "";
    ServiceClient remoteUserStoreManagerServiceClient;
    RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub;
	
	
	
	public ISAuthenticationHandler() {
		this.properties = new HashMap();
	}

	public void addProperty(String name, Object value) {
		this.properties.put(name, value);
	}

	public Map getProperties() {
		return this.properties;
	}

	public boolean handleRequest(MessageContext messageContext) {

		if (remoteUserStoreManagerServiceStub==null)
		{
			synchronized (RemoteUserStoreManagerServiceStub.class) {
				if (remoteUserStoreManagerServiceStub==null)
				{
					remoteUserStoreManagerServiceStub =createAdminClients(); 
				}
			}
		}
		
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
			
			String restApiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
			String restFullRequestContext = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
			String tenantCode = (restFullRequestContext).substring(restApiContext.length()+7); // 7 == ("/input/").length()
			if (tenantCode.endsWith("/"))
			{
				tenantCode = tenantCode.substring(0,tenantCode.length()-1);
			}
			Map headersMap = ((Map) headers);
			String authHeader = (String) headersMap.get("Authorization");
			String credentials = authHeader.substring(6).trim();
			if (processSecurity(tenantCode,credentials)) {
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

	public boolean processSecurity(String tenantCode,String credentials) {
		String decodedCredentials = new String(new Base64().decode(credentials
				.getBytes()));
		String userName = decodedCredentials.split(":")[0];
		String password = decodedCredentials.split(":")[1];
		
		boolean isValidUser = false;
		
		if ((""+tenantCode).equals(userName))
		{
			try {
				isValidUser = remoteUserStoreManagerServiceStub.authenticate(userName, password);
			} catch (Exception e) {
				logger.error("Errore durante il richiamo dell'IS per tenant:["+tenantCode+"] con utente:["+userName+"]",e);
			}
			return isValidUser;
		} 
		else {
			logger.warn("Richiamata API per tenant:["+tenantCode+"] con utente:["+userName+"]");
			return false;
		}
	}
	
	
	
	 private RemoteUserStoreManagerServiceStub createAdminClients() {

		 	RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub = null;
	        /**
	         * trust store path.  this must contains server's  certificate or Server's CA chain
	         */
//	        String trustStore = jksFileLocation + File.separator + "wso2carbon.jks";
	        /**
	         * Call to https://localhost:9443/services/   uses HTTPS protocol.
	         * Therefore we to validate the server certificate or CA chain. The server certificate is looked up in the
	         * trust store.
	         * Following code sets what trust-store to look for and its JKs password.
	         */
//	        System.setProperty("javax.net.ssl.trustStore", trustStore);
//	        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
	        /**
	         * Axis2 configuration context
	         */
	        ConfigurationContext configContext;

	        try {
	            /**
	             * Create a configuration context. A configuration context contains information for
	             * axis2 environment. This is needed to create an axis2 service client
	             */
	            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,null);
	            /**
	             * end point url with service name
	             */
	            // RemoteUserStoreManager
	            String remoteUserStoreManagerServiceEndPoint = isServerUrl + "/services/" + "RemoteUserStoreManagerService";
	            logger.error(">>>>>>>>>>>>>>" + isServerUrl);
	            remoteUserStoreManagerServiceStub = new RemoteUserStoreManagerServiceStub(configContext, remoteUserStoreManagerServiceEndPoint);
	            remoteUserStoreManagerServiceClient = remoteUserStoreManagerServiceStub._getServiceClient();
	            Options optionRemoteUser = remoteUserStoreManagerServiceClient.getOptions();
	            setProxyToOptions(optionRemoteUser, isAdminUsername, isAdminPassword);
	            remoteUserStoreManagerAuthCookie = (String) remoteUserStoreManagerServiceStub._getServiceClient().getServiceContext()
	                    .getProperty(HTTPConstants.COOKIE_STRING);
	            
	            
	        } catch (Exception e) {
	        	logger.error("Error init client to IS.",e);
	            e.printStackTrace();
	        }
	        
	        return remoteUserStoreManagerServiceStub;
	    }


		private void setProxyToOptions(Options option,String username, String password) {
			/**
			 * Setting a authenticated cookie that is received from Carbon server.
			 * If you have authenticated with Carbon server earlier, you can use that cookie, if
			 * it has not been expired
			 */
			option.setProperty(HTTPConstants.COOKIE_STRING, null);        
			/**
			 *  Setting proxy property if exists
			 */
			/**
			 * Setting basic auth headers for authentication for carbon server
			 */
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(username);
			auth.setPassword(password);
			auth.setPreemptiveAuthentication(true);
			option.setProperty(HTTPConstants.AUTHENTICATE, auth);
			option.setManageSession(true);
			option.setCallTransportCleanup(true);
		}
	
	

	public String getIsAdminUsername() {
		return isAdminUsername;
	}

	public void setIsAdminUsername(String isAdminUsername) {
		this.isAdminUsername = isAdminUsername;
	}

	public String getIsAdminPassword() {
		return isAdminPassword;
	}

	public void setIsAdminPassword(String isAdminPassword) {
		this.isAdminPassword = isAdminPassword;
	}

	public String getIsServerUrl() {
		return isServerUrl;
	}

	public void setIsServerUrl(String isServerUrl) {
		this.isServerUrl = isServerUrl;
	}


	public static void main(String[] args) {
		String REST_API_CONTEXT = "/wso005/stream";
		String REST_FULL_REQUEST_PATH =	"/wso005/stream/input/dd/";
		
		System.out.println(REST_FULL_REQUEST_PATH.substring(REST_API_CONTEXT.length()));
		
	}
	
	
}