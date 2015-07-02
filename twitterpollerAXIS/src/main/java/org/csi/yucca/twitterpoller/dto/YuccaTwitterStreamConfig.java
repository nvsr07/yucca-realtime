package org.csi.yucca.twitterpoller.dto;

public class YuccaTwitterStreamConfig {

	private String streamCode=null;
	private String virtualEntityCode=null;
	private String tenatcode=null;
	private Integer streamVersion=new Integer(1);
	private Integer resetLastId=new Integer(1);
	
	public Integer getStreamVersion() {
		return streamVersion;
	}
	public void setStreamVersion(Integer streamVersion) {
		this.streamVersion = streamVersion;
	}
	public Integer getResetLastId() {
		return resetLastId;
	}
	public void setResetLastId(Integer resetLastId) {
		this.resetLastId = resetLastId;
	}
	public String getStreamCode() {
		return streamCode;
	}
	public void setStreamCode(String streamCode) {
		this.streamCode = streamCode;
	}
	public String getVirtualEntityCode() {
		return virtualEntityCode;
	}
	public void setVirtualEntityCode(String virtualEntityCode) {
		this.virtualEntityCode = virtualEntityCode;
	}
	public String getTenatcode() {
		return tenatcode;
	}
	public void setTenatcode(String tenatcode) {
		this.tenatcode = tenatcode;
	}
	
	
}
