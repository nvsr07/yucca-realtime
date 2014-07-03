esbinput
========================================================

Wso2 based component.
This component receives events from sensors, feed and applications and sends them to internal message broker.
In this version it can receive HTTP Json events and can send to JMS based message broker.

To configure and build package for your environment you must:

1. copy file inputeventsesbconf/src/main/resource/template.environment.properties as <your_env>.properties  
2. insert yours variables in <your_env>.properties
3. change inputeventsesbconf/pom.xml, substituting template_environment.properties string to <your_env>.properties
2. run mvn clean install from esbinput dir

To achieve multitenancy, every tenant must install a new package, after has changed <tenant1> string in all files with its tenant name.

The exposed endpoint will be:

http://<your_server_wso2_esb_481>/wso001/services/ten1/<streamId>


In this alpha version this component implements a simple passthrough. In the next it must:
1. Validate source credentials
2. Validate input json format
3. Send to internal broker to queue input.ten1.streamId if this queue exists.
4. If an error happens it must sends another (or same) broker to error queue.
