package org.csi.yucca.twitterpoller.dto;

public class YuccaTwitterResult {

	private String contributors=null;
	private String createdAt=null; //TODO e' una data 
	private Long currentUserRetweetId = null;
	private Integer favoriteCount = null;
	private Double  lon=null;
	private Double lat=null;
	private Long tweetid = null; 
	private String lang = null;
	private String placeName=null;
	private Integer retweetCount = null;
	private String source = null;
	private String getText = null;
	private boolean isFavorited = false;
	private boolean isPossiblySensitive  = false;
	private boolean isRetweet  = false;
	private boolean isRetweetedByMe  = false;
	private boolean isTruncated   = false;
	private String hashTags = null;
	private String url=null;
	private String media= null;
	private String mediaUrl =null;
	private Integer mediaCnt = null;
	
	//YUCCA-387
	private Long retweetParentId  = null;
	private String userMentions  =null;
	//YUCCA-387
	
	
	public Long getRetweetParentId() {
		return retweetParentId;
	}
	public void setRetweetParentId(Long retweetParentId) {
		this.retweetParentId = retweetParentId;
	}
	public String getUserMentions() {
		return userMentions;
	}
	public void setUserMentions(String userMentions) {
		this.userMentions = userMentions;
	}
	private Long userId =null;
	private String userName =null;
	private String userScreenName =null;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserScreenName() {
		return userScreenName;
	}
	public void setUserScreenName(String userScreenName) {
		this.userScreenName = userScreenName;
	}
	public String getContributors() {
		return contributors;
	}
	public void setContributors(String contributors) {
		this.contributors = contributors;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public Long getCurrentUserRetweetId() {
		return currentUserRetweetId;
	}
	public void setCurrentUserRetweetId(Long currentUserRetweetId) {
		this.currentUserRetweetId = currentUserRetweetId;
	}
	public Integer getFavoriteCount() {
		return favoriteCount;
	}
	public void setFavoriteCount(Integer favoriteCount) {
		this.favoriteCount = favoriteCount;
	}
	public Double getLon() {
		return lon;
	}
	public void setLon(Double lon) {
		this.lon = lon;
	}
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Long getTweetid() {
		return tweetid;
	}
	public void setTweetid(Long tweetid) {
		this.tweetid = tweetid;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getPlaceName() {
		return placeName;
	}
	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}
	public Integer getRetweetCount() {
		return retweetCount;
	}
	public void setRetweetCount(Integer retweetCount) {
		this.retweetCount = retweetCount;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getGetText() {
		return getText;
	}
	public void setGetText(String getText) {
		this.getText = getText;
	}
	public boolean isFavorited() {
		return isFavorited;
	}
	public void setFavorited(boolean isFavorited) {
		this.isFavorited = isFavorited;
	}
	public boolean isPossiblySensitive() {
		return isPossiblySensitive;
	}
	public void setPossiblySensitive(boolean isPossiblySensitive) {
		this.isPossiblySensitive = isPossiblySensitive;
	}
	public boolean isRetweet() {
		return isRetweet;
	}
	public void setRetweet(boolean isRetweet) {
		this.isRetweet = isRetweet;
	}
	public boolean isRetweetedByMe() {
		return isRetweetedByMe;
	}
	public void setRetweetedByMe(boolean isRetweetedByMe) {
		this.isRetweetedByMe = isRetweetedByMe;
	}
	public boolean isTruncated() {
		return isTruncated;
	}
	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}
	public String getHashTags() {
		return hashTags;
	}
	public void setHashTags(String hashTags) {
		this.hashTags = hashTags;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMedia() {
		return media;
	}
	public void setMedia(String media) {
		this.media = media;
	}
	public String getMediaUrl() {
		return mediaUrl;
	}
	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}
	public Integer getMediaCnt() {
		return mediaCnt;
	}
	public void setMediaCnt(Integer mediaCnt) {
		this.mediaCnt = mediaCnt;
	} 
	
	
	
	
	
}
