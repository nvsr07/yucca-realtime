package org.csi.yucca.twitterpoller.dto;

public class YuccaTwitterQuery {

	
	public static final String TWT_GEOLOCUNIT_KILOMETERS="KM";
	public static final String TWT_GEOLOCUNIT_MILES="MI";
	
	private String twtUserToken = null;
	private String twtTokenSecret = null;
	private String twtQuery = null;

	private Double twtGeolocLat=null; 
	private Double twtGeolocLon =null; 
	private Double twtGeolocRadius =null; 
	private String twtGeolocUnit = null; //TODO mettere costanti
	
	private String twtLang = null;
	private String twtCount = null;
	private String twtResultType = null; //TODO mettere costanti
	
	
	private String twtUntil = null; //TODO sarebbe una data
	private Long twtLastSearchId = null; //TODO: e' calcolato; potrebbe non servire 
	
	
	
	
	
	
	
	public String getTwtUserToken() {
		return twtUserToken;
	}
	public void setTwtUserToken(String twtUsertoken) {
		this.twtUserToken = twtUsertoken;
	}
	public String getTwtTokenSecret() {
		return twtTokenSecret;
	}
	public void setTwtTokenSecret(String twtTokenSecret) {
		this.twtTokenSecret = twtTokenSecret;
	}
	public String getTwtQuery() {
		return twtQuery;
	}
	public void setTwtQuery(String twtQuery) {
		this.twtQuery = twtQuery;
	}
	public Double getTwtGeolocLat() {
		return twtGeolocLat;
	}
	public void setTwtGeolocLat(Double twtGeolocLat) {
		this.twtGeolocLat = twtGeolocLat;
	}
	public Double getTwtGeolocLon() {
		return twtGeolocLon;
	}
	public void setTwtGeolocLon(Double twtGeolocLon) {
		this.twtGeolocLon = twtGeolocLon;
	}
	public Double getTwtGeolocRadius() {
		return twtGeolocRadius;
	}
	public void setTwtGeolocRadius(Double twtGeolocRadius) {
		this.twtGeolocRadius = twtGeolocRadius;
	}
	public String getTwtGeolocUnit() {
		return twtGeolocUnit;
	}
	public void setTwtGeolocUnit(String twtGeolocUnit) {
		this.twtGeolocUnit = twtGeolocUnit;
	}
	public String getTwtLang() {
		return twtLang;
	}
	public void setTwtLang(String twtLang) {
		this.twtLang = twtLang;
	}
	public String getTwtCount() {
		return twtCount;
	}
	public void setTwtCount(String twtCount) {
		this.twtCount = twtCount;
	}
	public String getTwtResultType() {
		return twtResultType;
	}
	public void setTwtResultType(String twtResultType) {
		this.twtResultType = twtResultType;
	}
	public String getTwtUntil() {
		return twtUntil;
	}
	public void setTwtUntil(String twtUntil) {
		this.twtUntil = twtUntil;
	}
	public Long getTwtLastSearchId() {
		return twtLastSearchId;
	}
	public void setTwtLastSearchId(Long twtLastSearchId) {
		this.twtLastSearchId = twtLastSearchId;
	}
	
	
}
