package org.csi.yucca.realtime.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class  MqttCallbackResult implements MqttCallback {

	private final CountDownLatch loginLatch = new CountDownLatch (1);
	private boolean result = false;
	
	@Override
	public void messageArrived(String topic, MqttMessage message)  throws Exception
	{
		loginLatch.countDown();
	};
	
	@Override
	public abstract void deliveryComplete(IMqttDeliveryToken arg0) ;
	
	@Override
	public void connectionLost(Throwable arg0) {
		arg0.printStackTrace();
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public void waitForEvent(int millisecondTimeout) {
		try {
			loginLatch.await(millisecondTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
