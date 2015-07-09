package org.csi.yucca.twitterpoller.dto;

public class YuccaTwitterException {
	
	private String twtErrorCode="TWT_XXX";
	private String twtErrorMessage="UNKNOWN";
	public String getTwtErrorCode() {
		return twtErrorCode;
	}
	public void setTwtErrorCode(String twtErrorCode) {
		this.twtErrorCode = twtErrorCode;
	}
	public String getTwtErrorMessage() {
		return twtErrorMessage;
	}
	public void setTwtErrorMessage(String twtErrorMessage) {
		this.twtErrorMessage = twtErrorMessage;
	}
	

}
