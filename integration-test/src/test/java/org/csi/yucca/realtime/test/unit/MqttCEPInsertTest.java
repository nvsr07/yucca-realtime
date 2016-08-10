package org.csi.yucca.realtime.test.unit;

import static io.restassured.RestAssured.given;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.csi.yucca.realtime.test.MqttCallbackResult;
import org.csi.yucca.realtime.test.RestTest;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class MqttCEPInsertTest extends RestTest{

	@DataProvider(name = "ValidationCEPInsertTest")
	public Iterator<Object[]> getFromJson() {

		return getFromJson("/ValidationCEPInsertTest.json");
	}

	@BeforeClass
	public void setUpSecretObject() throws IOException {
		super.setUpSecretObject("/testSecret.json");
	}

	@Test(dataProvider = "ValidationCEPInsertTest",singleThreaded=true)
	public void sendMQTTStatusErrorCodeTesting(final JSONObject dato) {
		if (dato.optBoolean("rt.toskip") || dato.optBoolean("rt.mqtttoskip"))
			throw new SkipException("TODO in future version");

		if (StringUtils.isNotEmpty(dato.optString("rt.mqttqueue"))) {
			MqttClient client = null;
			
			MqttCallbackResult mqttCallback = new MqttCallbackResult() {
				
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					JSONObject msg = new JSONObject(new String(message.getPayload(),"UTF-8"));
					setResult(msg.get("error_code").equals(dato.get("rt.errorCode")));
					super.messageArrived(topic, message);
				}
				
				@Override
				public void deliveryComplete(IMqttDeliveryToken arg0) {
					
				}
			};
			
			
			try {
				client = getMqttClient(dato, mqttCallback);
				
				client.setTimeToWait(10000);
				client.subscribe(dato.getString("rt.mqttqueue"), 0);
				

				this.publishMqtt(dato);
				
				mqttCallback.waitForEvent(10000);

				
				Assert.assertTrue(mqttCallback.isResult());
				
			} catch (Exception e) {
				Assert.fail("Mqtt problem", e);
			}
			finally {
				if (client!=null && client.isConnected())
					try {client.disconnect();} 
					catch (MqttException e) {Assert.fail("Mqtt problem", e);}
			}
		} else {
			throw new SkipException("Skipping the test case");
		}


	}

	private MqttClient getMqttClient(JSONObject dato, MqttCallback callback) throws MqttSecurityException, MqttException {
		MqttClient client = new MqttClient(dato.getString("rt.mqtturl"), dato.getString("rt.mqttclientid"));

		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setUserName(dato.getString("rt.mqttadminuser"));
		connOpts.setPassword(dato.getString("rt.mqttadminpwd").toCharArray());
		client.setTimeToWait(15000);
		client.setCallback(callback);
		client.connect(connOpts);
		return client;
	}

	private void publishMqtt(JSONObject dato) throws MqttSecurityException, MqttException {
		MqttClient client = new MqttClient(dato.getString("rt.mqtturl"), "publisher");

		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setUserName(dato.getString("rt.username"));
		connOpts.setPassword(dato.getString("rt.password").toCharArray());
		client.setTimeToWait(15000);
		client.connect(connOpts);
		
		MqttMessage msg = new MqttMessage();
		msg.setPayload(dato.getString("rt.message").getBytes());
		msg.setQos(0);
		client.publish("input/"+dato.getString("rt.tenant"), msg);
		client.disconnect();
	}

}
