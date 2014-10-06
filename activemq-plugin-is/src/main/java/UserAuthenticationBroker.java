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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DestinationInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.ProducerInfo;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

public class UserAuthenticationBroker extends BrokerFilter implements UserAuthenticationBrokerMBean {

    private static final Logger LOG = LoggerFactory.getLogger(UserAuthenticationBroker.class);

	
    private static final String SEPARATOR = "|";
    private static final String PREFIX_MB_ROLES = "mb"+SEPARATOR;
	//Necessary default properties required
    final String serverUrl;
    final String username;
    final String password;
    final String jksFileLocation;
    final String globalPublisherRole;
    final String globalSubscriberRole;
    final String globalPublisherSubscriberRole;
    final int cacheValidationInterval;

    //RemoteStoreManager Clients
    String remoteUserStoreManagerAuthCookie = "";
    ServiceClient remoteUserStoreManagerServiceClient;
    RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub;

    //To handle caching
    Map<String, AuthorizationRole[]> userSpecificRoles = new ConcurrentHashMap<String, AuthorizationRole[]>();

    public UserAuthenticationBroker(Broker next, String serverUrl, String username,
                                    String password, String jksFileLocation,
                                    String globalPublisherRole,
                                    String globalSubscriberRole,
                                    String globalPublisherSubscriberRole,
                                    int cacheValidationInterval) {
        super(next);
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.jksFileLocation = jksFileLocation;
        this.globalPublisherRole = globalPublisherRole;
        this.globalSubscriberRole = globalSubscriberRole;
        this.globalPublisherSubscriberRole = globalPublisherSubscriberRole;
        this.cacheValidationInterval = cacheValidationInterval;

        createAdminClients();
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Task task = new Task();
//        scheduler.scheduleAtFixedRate(task, 0, cacheValidationInterval, TimeUnit.MINUTES);
//        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
        
        registerMBean();
        
    }


    private void registerMBean() {
    	// Get the platform MBeanServer
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
  
       // Unique identification of MBeans
       ObjectName name = null;
  
       try {
          // Uniquely identify the MBeans and register them with the platform MBeanServer 
          name = new ObjectName("IdentityServerAuthPluginr:name=UserAuthMBean");
          mbs.registerMBean((UserAuthenticationBrokerMBean)this, name);
       } catch(Exception e) {
          e.printStackTrace();
       }		
	}


	public void addConnection(ConnectionContext context, ConnectionInfo info)
            throws Exception {

        Options option = remoteUserStoreManagerServiceClient.getOptions();
        option.setProperty(HTTPConstants.COOKIE_STRING, remoteUserStoreManagerAuthCookie);
        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(username);
        auth.setPassword(password);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);

        boolean isValidUser = remoteUserStoreManagerServiceStub.authenticate(info.getUserName(), info.getPassword());
        if (isValidUser) {
            super.addConnection(context, info);
        } else {
            throw new SecurityException("Not a valid user "+context.getUserName() +" connection");
        }
    }


    public Subscription addConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {

    	LOG.info(">>>>>>AddConsumer:"+info.getDestination());
    	
        if (isBrokerAccess(context, info.getDestination()))
        {
            return super.addConsumer(context, info);
        }
    	
    	AuthorizationRole[] roleNames = getRoleListOfUser(context.getUserName());

        if (roleNames!=null)
        {
        	for (int i = 0; i < roleNames.length; i++) {
				AuthorizationRole authorizationRole = roleNames[i];
				if (isAuthorized(authorizationRole,Operation.READ,info.getDestination()))
				{
                    return super.addConsumer(context, info);					
				}
			}
        }
        throw new SecurityException("Not a valid user "+context.getUserName() +" to subscribe : " + info.toString() + ".");
        
    }



	@Override
	public void addProducer(ConnectionContext context, ProducerInfo info)
			throws Exception {
    	LOG.info(">>>>>>AddProducer:"+info.getDestination());
        if (isBrokerAccess(context, info.getDestination()))
        {
            super.addProducer(context, info);
            return;
        }

		
		AuthorizationRole[] roleNames = getRoleListOfUser(context.getUserName());

        if (roleNames!=null)
        {
        	for (int i = 0; i < roleNames.length; i++) {
				AuthorizationRole authorizationRole = roleNames[i];
				if (isAuthorized(authorizationRole,Operation.WRITE,info.getDestination()))
						{
		                    super.addProducer(context, info);
		                    return;
						}
			}
        }
        throw new SecurityException("Not a valid user "+context.getUserName() +" to produce : " + info.toString() + ".");
	}
	
    public void send(ProducerBrokerExchange producerExchange, Message messageSend)
            throws Exception {
    	LOG.info(">>>>>>send:"+messageSend.getDestination());

        if (isBrokerAccess(producerExchange.getConnectionContext(), messageSend.getDestination()))
        {
            super.send(producerExchange,messageSend);
            return;
        }	
    	
    	AuthorizationRole[] roleNames = getRoleListOfUser(producerExchange.getConnectionContext().getUserName());

        if (roleNames!=null)
        {
        	for (int i = 0; i < roleNames.length; i++) {
				AuthorizationRole authorizationRole = roleNames[i];
				if (isAuthorized(authorizationRole,Operation.WRITE,messageSend.getDestination()))
						{
		                    super.send(producerExchange,messageSend);
		                    return;
						}
			}
        }
        throw new SecurityException("Not a valid user "+producerExchange.getConnectionContext().getUserName() +" to produce : " + messageSend.getDestination().toString() + ".");
    	
    }

    
    @Override
    public Destination addDestination(ConnectionContext context,
    		ActiveMQDestination destination, boolean createIfTemporary)
    		throws Exception {
    	
    	LOG.info(">>>>>>AddDestination:"+destination);

        if (isBrokerAccess(context, destination))
        {
            return super.addDestination(context, destination, createIfTemporary);
        }

    	
    	AuthorizationRole[] roleNames = getRoleListOfUser(context.getUserName());

        if (roleNames!=null)
        {
        	for (int i = 0; i < roleNames.length; i++) {
				AuthorizationRole authorizationRole = roleNames[i];
				if (isAuthorized(authorizationRole,Operation.ADMIN,destination))
						{
					    	return super.addDestination(context, destination, createIfTemporary);
						}
			}
        }
        throw new SecurityException("Not a valid user "+context.getUserName() +" to admin : " + destination.toString() + ".");

    	
    }
    
    @Override
    public void addDestinationInfo(ConnectionContext context,
    		DestinationInfo info) throws Exception {

    	LOG.info(">>>>>>AddDestinationInfpo:"+info.getDestination());

        if (isBrokerAccess(context, info.getDestination()))
        {
            super.addDestinationInfo(context, info);
            return;
        }

    	AuthorizationRole[] roleNames = getRoleListOfUser(context.getUserName());

        if (roleNames!=null)
        {
        	for (int i = 0; i < roleNames.length; i++) {
				AuthorizationRole authorizationRole = roleNames[i];
				if (isAuthorized(authorizationRole,Operation.ADMIN,info.getDestination()))
						{
							super.addDestinationInfo(context, info);
							return;
						}
			}
        }
        throw new SecurityException("Not a valid user "+context.getUserName() +" to admin : " + info.getDestination().toString() + ".");    	
    	
    	
    }
    
    @Override
    public void removeDestination(ConnectionContext context,
    		ActiveMQDestination destination, long timeout) throws Exception {
    	LOG.info(">>>>>>RemoveDestinatrion:"+destination);

        if (isBrokerAccess(context, destination))
        {
            super.removeDestination(context, destination, timeout);
            return;
        }

    	
    	AuthorizationRole[] roleNames = getRoleListOfUser(context.getUserName());

        if (roleNames!=null)
        {
        	for (int i = 0; i < roleNames.length; i++) {
				AuthorizationRole authorizationRole = roleNames[i];
				if (isAuthorized(authorizationRole,Operation.ADMIN,destination))
						{
					    	super.removeDestination(context, destination, timeout);
						}
			}
        }
        throw new SecurityException("Not a valid user "+context.getUserName() +" to admin : " + destination.toString() + ".");

    }
    
    @Override
    public void removeDestinationInfo(ConnectionContext context,
    		DestinationInfo info) throws Exception {
    	LOG.info(">>>>>>RemoveDestinaiotnINfo:"+info.getDestination());

        if (isBrokerAccess(context, info.getDestination()))
        {
            super.removeDestinationInfo(context, info);
            return;
        }

    	
    	AuthorizationRole[] roleNames = getRoleListOfUser(context.getUserName());

        if (roleNames!=null)
        {
        	for (int i = 0; i < roleNames.length; i++) {
				AuthorizationRole authorizationRole = roleNames[i];
				if (isAuthorized(authorizationRole,Operation.ADMIN,info.getDestination()))
						{
							super.removeDestinationInfo(context, info);
							return;
						}
			}
        }
        throw new SecurityException("Not a valid user "+context.getUserName() +" to admin : " + info.getDestination().toString() + ".");    	
    	
    }
    
    
	private boolean isBrokerAccess(ConnectionContext context, ActiveMQDestination dest) {
		
		if (dest==null || dest.getPhysicalName().startsWith("ActiveMQ.Advisory"))
			return true;
		if (context.getSecurityContext()!=null)
			return context.getSecurityContext().isBrokerContext();
		return false;
	}

    
	private boolean isAuthorized(AuthorizationRole authorizationRole,
			Operation operation, ActiveMQDestination destination) {
		if (authorizationRole.getOperation() == operation)
		{
			if (authorizationRole.getType().name().equalsIgnoreCase(destination.getDestinationTypeAsString()))
			{
				String[] destTokenAuth = authorizationRole.getDestinationWildCards().split("\\.");
				String[] destTokenToAuth = destination.getPhysicalName().split("\\.");

				return isEqualWildCards(destTokenAuth, destTokenToAuth);
			}
		}

		return false;
	}


	private static boolean isEqualWildCards(String[] destTokenAuth,
			String[] destTokenToAuth) {
		for (int i = 0; i < destTokenAuth.length; i++) {
			String partAuth = destTokenAuth[i];
			if (partAuth.equals("_"))
			{
				if (destTokenToAuth.length>i)
					return true;
				else
					return false;
			}
			if (!partAuth.equals(destTokenToAuth[i]))
			{
				return false;
			}
		}
		return true;
	}

//	public static void main(String[] args) {
//		String[] destTokenAuth = "pippo.pluto".split("\\.");
//		String[] destTokenToAuth = "pippo.pluto".split("\\.");
//		System.out.println(isEqualWildCards(destTokenAuth, destTokenToAuth));
//		destTokenAuth = "pippo.>".split("\\.");
//		destTokenToAuth = "pippo.pluto".split("\\.");
//		System.out.println(isEqualWildCards(destTokenAuth, destTokenToAuth));
//		
//		destTokenAuth = ">".split("\\.");
//		destTokenToAuth = "pippo.pluto".split("\\.");
//		System.out.println(isEqualWildCards(destTokenAuth, destTokenToAuth));
//
//		System.out.println("===== FALSE =========");
//		
//		destTokenAuth = "pippo.pluto".split("\\.");
//		destTokenToAuth = "pippo.>".split("\\.");
//		System.out.println(isEqualWildCards(destTokenAuth, destTokenToAuth));
//
//		destTokenAuth = "pippo.pluto.>".split("\\.");
//		destTokenToAuth = "pippo.pluto".split("\\.");
//		System.out.println(isEqualWildCards(destTokenAuth, destTokenToAuth));
//
//		destTokenAuth = "pippo.>".split("\\.");
//		destTokenToAuth = "pippo2.sda".split("\\.");
//		System.out.println(isEqualWildCards(destTokenAuth, destTokenToAuth));
//	}
	
	
	private AuthorizationRole[] getRoleListOfUser(String userName)
			throws RemoteException,
			RemoteUserStoreManagerServiceUserStoreExceptionException {
		
		return getRoleListOfUser(userName, false);
		
	}
	private AuthorizationRole[] getRoleListOfUser(String userName, boolean forceUpdate)
			throws RemoteException,
			RemoteUserStoreManagerServiceUserStoreExceptionException {
		AuthorizationRole[] roleNames;

        if (!forceUpdate && userSpecificRoles.containsKey(userName)) {
            roleNames = userSpecificRoles.get(userName);
        } else {
            Options option = remoteUserStoreManagerServiceClient.getOptions();
            option.setProperty(HTTPConstants.COOKIE_STRING, remoteUserStoreManagerAuthCookie);
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(username);
            auth.setPassword(password);
            auth.setPreemptiveAuthentication(true);
            option.setProperty(HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);
            String[] allRoleNames = remoteUserStoreManagerServiceStub.getRoleListOfUser(userName);
            
            ArrayList<AuthorizationRole> roles = new ArrayList<AuthorizationRole>();
            
            if (allRoleNames!=null)
            {
	            for (String roleName : allRoleNames) {
					if (roleName.startsWith(PREFIX_MB_ROLES))
					{
						String roleNameWithoutPrefix = roleName.substring(PREFIX_MB_ROLES.length());
						AuthorizationRole authRole = new AuthorizationRole(roleNameWithoutPrefix);
						if (authRole.isValid())
							roles.add(authRole);
					}
				}
            }
            roleNames = roles.toArray(new AuthorizationRole[0]);
            userSpecificRoles.put(userName, roleNames);
        }
		return roleNames;
	}

    

    private void createAdminClients() {

        /**
         * trust store path.  this must contains server's  certificate or Server's CA chain
         */

        String trustStore = jksFileLocation + File.separator + "wso2carbon.jks";

        /**
         * Call to https://localhost:9443/services/   uses HTTPS protocol.
         * Therefore we to validate the server certificate or CA chain. The server certificate is looked up in the
         * trust store.
         * Following code sets what trust-store to look for and its JKs password.
         */

        System.setProperty("javax.net.ssl.trustStore", trustStore);

        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        /**
         * Axis2 configuration context
         */
        ConfigurationContext configContext;

        try {

            /**
             * Create a configuration context. A configuration context contains information for
             * axis2 environment. This is needed to create an axis2 service client
             */
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

            /**
             * end point url with service name
             */
            String remoteUserStoreManagerServiceEndPoint = serverUrl + "/services/" + "RemoteUserStoreManagerService";

            /**
             * create stub and service client
             */
            remoteUserStoreManagerServiceStub = new RemoteUserStoreManagerServiceStub(configContext, remoteUserStoreManagerServiceEndPoint);
            remoteUserStoreManagerServiceClient = remoteUserStoreManagerServiceStub._getServiceClient();
            Options option = remoteUserStoreManagerServiceClient.getOptions();

            /**
             * Setting a authenticated cookie that is received from Carbon server.
             * If you have authenticated with Carbon server earlier, you can use that cookie, if
             * it has not been expired
             */
            option.setProperty(HTTPConstants.COOKIE_STRING, null);

            /**
             * Setting basic auth headers for authentication for carbon server
             */
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(username);
            auth.setPassword(password);
            auth.setPreemptiveAuthentication(true);
            option.setProperty(HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);


            remoteUserStoreManagerAuthCookie = (String) remoteUserStoreManagerServiceStub._getServiceClient().getServiceContext()
                    .getProperty(HTTPConstants.COOKIE_STRING);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    class Task implements Runnable {

        @Override
        public void run() {

            for (Map.Entry<String, AuthorizationRole[]> entry : userSpecificRoles.entrySet()) {
                String userName = entry.getKey();
                try {
                	getRoleListOfUser(userName, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    class AuthorizationRole {
    
    	public AuthorizationRole(String roleNameWithoutPrefix) {
    		try {
    		String[] parts = roleNameWithoutPrefix.split("["+SEPARATOR+"]",3);
    		if (parts.length!=3)
    		{
    			LOG.error(">>>>>>No valid role:"+roleNameWithoutPrefix);
    			this.setValid(false);
    			return;
    		}
			this.type = DestinationType.valueOf(parts[0].toUpperCase());
    		this.operation = Operation.valueOf(parts[1].toUpperCase());
    		this.destinationWildCards = parts[2];
    		if (destinationWildCards.length()<0)
        		this.setValid(false);
    			
    		this.setValid(true);
    		
    		} catch (Exception e) {
    			LOG.error(">>>>>>Exception on valid role:"+roleNameWithoutPrefix,e);
    			this.setValid(false);
    		}
		}
		
    	private DestinationType type;
    	private Operation operation;
    	private String destinationWildCards;
    	private boolean valid;
		public DestinationType getType() {
			return type;
		}
		public void setType(DestinationType type) {
			this.type = type;
		}
		public Operation getOperation() {
			return operation;
		}
		public void setOperation(Operation operation) {
			this.operation = operation;
		}
		public String getDestinationWildCards() {
			return destinationWildCards;
		}
		public void setDestinationWildCards(String destinationWildCards) {
			this.destinationWildCards = destinationWildCards;
		}
		public boolean isValid() {
			return valid;
		}
		public void setValid(boolean isValid) {
			this.valid = isValid;
		}
    	
    	@Override
    	public String toString() {
    		return type.name+SEPARATOR+operation.name+SEPARATOR+destinationWildCards+"("+valid+")";
    	}
    	
    }

    private static enum DestinationType {

        QUEUE("queue"),
        TOPIC("topic"),
        TEMPQUEUE("tempqueue"),
        TEMPTOPIC("temptopic");
        
        private String name;
        
        private DestinationType(String name) {
        	this.name = name;
        }
        
        
    }
    
    private static enum Operation {

        READ("read"),
        WRITE("write"),
        ADMIN("admin");
        
        private String name;
        
        private Operation(String name) {
        	this.name = name;
        }
        
        
    }

	@Override
	public void resetCache(String username) {
        try {
        	getRoleListOfUser(username, true);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            e.printStackTrace();
        }
	}
}
