package org.csi.yucca.twitterpoller.dto;

import java.util.ArrayList;

public class YuccaInvokeResult {

	
	
	private long maxId=-1;
	
	private ArrayList<YuccaTwitterValue> valuesRet = null;

	public long getMaxId() {
		return maxId;
	}

	public void setMaxId(long maxId) {
		this.maxId = maxId;
	}

	public ArrayList<YuccaTwitterValue> getValuesRet() {
		return valuesRet;
	}

	public void setValuesRet(ArrayList<YuccaTwitterValue> valuesRet) {
		this.valuesRet = valuesRet;
	} 
	
}
