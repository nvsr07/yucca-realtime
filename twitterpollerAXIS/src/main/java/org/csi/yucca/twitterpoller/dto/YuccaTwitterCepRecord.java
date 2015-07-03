package org.csi.yucca.twitterpoller.dto;

import java.util.ArrayList;

public class YuccaTwitterCepRecord {

	
	
	private String stream=null;
	private String sensor=null;
	private ArrayList<YuccaTwitterValue> values=null;
	public String getStream() {
		return stream;
	}
	public void setStream(String stream) {
		this.stream = stream;
	}
	public String getSensor() {
		return sensor;
	}
	public void setSensor(String sensor) {
		this.sensor = sensor;
	}
	public ArrayList<YuccaTwitterValue> getValues() {
		return values;
	}
	public void setValues(ArrayList<YuccaTwitterValue> values) {
		this.values = values;
	}
	
}
