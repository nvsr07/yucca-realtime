package org.csi.yucca.twitterpoller.business;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.csi.yucca.twitterpoller.constants.SDPTwitterConfig;
import org.csi.yucca.twitterpoller.dto.YuccaInvokeResult;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterQuery;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterResult;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterValue;

import twitter4j.GeoLocation;
import twitter4j.JSONObject;
import twitter4j.JSONObjectType;
import twitter4j.Query;
import twitter4j.Query.Unit;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterInvoker {

	private static final Log log=LogFactory.getLog("org.csi.yucca.twitterpoller");

	public YuccaInvokeResult invokeTwitter(YuccaTwitterQuery twitterQuery) throws Exception{

		String consumerKey=SDPTwitterConfig.getInstance().getTwtConsumerKey();
		String consumerSecret=SDPTwitterConfig.getInstance().getTwtConsumerSecret();
		ConfigurationBuilder cb = new ConfigurationBuilder();

		cb.setHttpProxyHost(SDPTwitterConfig.getInstance().getProxyHost());
		cb.setHttpProxyPort(SDPTwitterConfig.getInstance().getProxyPort());

		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);

		cb.setOAuthAccessToken(twitterQuery.getTwtUserToken());
		cb.setOAuthAccessTokenSecret(twitterQuery.getTwtTokenSecret());

		log.info("[TwitterInvoker::invokeTwitter] consumerKey "+consumerKey);
		log.info("[YuccaTwitterPoller::invokeTwitter] consumerSecret "+consumerSecret);
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtUsertoken() "+twitterQuery.getTwtUserToken());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtTokenSecret() "+twitterQuery.getTwtTokenSecret());
		

		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		//User user = twitter.verifyCredentials();
		//log.info("[YuccaTwitterPoller::invokeTwitter]user.getName()    "+user.getName());
		Query query = new Query(twitterQuery.getTwtQuery());

		//GEOLOCATION
		if (twitterQuery.getTwtGeolocLat()!=null && twitterQuery.getTwtGeolocLon()!=null && twitterQuery.getTwtGeolocRadius()!=null && twitterQuery.getTwtGeolocUnit()!=null
				&& twitterQuery.getTwtGeolocRadius()>0 ) {
			Unit unit=Query.KILOMETERS;
			if (twitterQuery.getTwtGeolocUnit().equals(YuccaTwitterQuery.TWT_GEOLOCUNIT_MILES)) unit=Query.MILES;

			query.setGeoCode(new GeoLocation(twitterQuery.getTwtGeolocLat(), twitterQuery.getTwtGeolocLon()), twitterQuery.getTwtGeolocRadius(), unit);
		}

		//LINGUA
		if (twitterQuery.getTwtLang()!=null) query.setLang(twitterQuery.getTwtLang());


		//SINCE ID 
		if (twitterQuery.getTwtLastSearchId()!=null) query.setSinceId(twitterQuery.getTwtLastSearchId());


		//COUNT ???? verificare se val la pensa di parametrizzarlo
		query.count(100);

		log.info("[YuccaTwitterPoller::invokeTwitter] ++++++++++++++++++++++ query.toString() "+query.toString());
			
		QueryResult result = twitter.search(query);

		
		log.info("[YuccaTwitterPoller::invokeTwitter] ++++++++++++++++++++++ result.getCount() "+result.getCount());
		log.info("[YuccaTwitterPoller::invokeTwitter] ++++++++++++++++++++++ result.getRateLimitStatus().getRemaining() "+result.getRateLimitStatus().getRemaining());
		log.info("[YuccaTwitterPoller::invokeTwitter] ++++++++++++++++++++++ result.getRateLimitStatus().getSecondsUntilReset() "+result.getRateLimitStatus().getSecondsUntilReset());
		log.info("[YuccaTwitterPoller::invokeTwitter] ++++++++++++++++++++++ result.getRateLimitStatus().getResetTimeInSeconds() "+result.getRateLimitStatus().getResetTimeInSeconds());
		log.info("[YuccaTwitterPoller::invokeTwitter] ++++++++++++++++++++++ result.getRateLimitStatus().getLimit() "+result.getRateLimitStatus().getLimit());

		
		long maxId=-1;
		ArrayList<YuccaTwitterResult> twittTrovati = new ArrayList<YuccaTwitterResult>();
		for (Status status : result.getTweets()) {
			YuccaTwitterResult cur = new YuccaTwitterResult();

			log.info("[YuccaTwitterPoller::invokeTwitter] elaboro: " + status.getId() + " -- "+status.getText());
			
			//contributors
			if (status.getContributors()!= null && status.getContributors().length>0) {
				String contributors=""+status.getContributors()[0];
				for (int i=1;i<status.getContributors().length;i++) {
					contributors+=","+status.getContributors()[i];
				}
				cur.setContributors(contributors);
			}

			
			//CREATED AT
			TimeZone tz = TimeZone.getTimeZone("UTC");
		    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		    df.setTimeZone(tz);			
			cur.setCreatedAt(df.format(status.getCreatedAt()));
			
			
			
			cur.setCurrentUserRetweetId(status.getCurrentUserRetweetId());
			cur.setFavoriteCount(status.getFavoriteCount());
			cur.setFavorited(status.isFavorited());
			cur.setGetText(escapeJson(status.getText()));
			
			
			//HASTAGS
			if (status.getHashtagEntities()!=null && status.getHashtagEntities().length>0) {
				String hashtags="#"+status.getHashtagEntities()[0].getText();
				for (int i=1;i<status.getHashtagEntities().length; i++) {
					hashtags+=" #"+status.getHashtagEntities()[i].getText();
				}
				cur.setHashTags(hashtags);
			}
			
			
			cur.setLang(status.getLang());
			
			//GEOLOCATION
			if (status.getGeoLocation()!=null) {
				cur.setLon(status.getGeoLocation().getLongitude());
				cur.setLat(status.getGeoLocation().getLatitude());
			}
			
			
			//MEDIA
			if (status.getMediaEntities()!=null && status.getMediaEntities().length>0) {
				String media=status.getMediaEntities()[0].getId()+"||||"+status.getMediaEntities()[0].getType()+"||||"+escapeJson(status.getMediaEntities()[0].getMediaURL());
				cur.setMedia(media);
				cur.setMediaCnt(status.getMediaEntities().length);
				cur.setMediaUrl(status.getMediaEntities()[0].getMediaURL());
			}
			
			//PLACE
			if (status.getPlace()!=null)cur.setPlaceName(escapeJson(status.getPlace().getFullName()+" ("+status.getPlace().getCountry() + "-"+status.getPlace().getCountryCode()+")"));
			
			cur.setPossiblySensitive(status.isPossiblySensitive());
			cur.setRetweet(status.isRetweeted());
			cur.setRetweetedByMe(status.isRetweetedByMe());
			cur.setRetweetCount(status.getRetweetCount());
			cur.setSource(escapeJson(status.getSource()));
			cur.setTruncated(status.isTruncated());
			cur.setTweetid(status.getId());
			if (status.getId()>maxId) maxId=status.getId();
			
	
			if (status.getURLEntities()!=null && status.getURLEntities().length>0) {
				cur.setUrl(escapeJson(status.getURLEntities()[0].getURL()));
			}
			
			
			if (null!=status.getUser()) {
				cur.setUserId(status.getUser().getId());
				cur.setUserName(status.getUser().getName());
				cur.setUserScreenName(status.getUser().getScreenName());
			}
			
			
			//YUCCA-387
			if (status.getRetweetedStatus()!=null) {
				cur.setRetweetParentId(status.getRetweetedStatus().getId());
			}
			if (status.getUserMentionEntities()!=null && status.getUserMentionEntities().length>0) {
				String userMentionsIds=""+status.getUserMentionEntities()[0].getId();
				for (int j=1; status.getUserMentionEntities()!=null && j<status.getUserMentionEntities().length;j++) {
					userMentionsIds+="|"+status.getUserMentionEntities()[0].getId();
				}
				cur.setUserMentions(userMentionsIds);
			}
			//YUCCA-387

			
			twittTrovati.add(cur);
			
		}

		log.info("[TwitterInvoker::invokeTwitter] RESULT");

		ArrayList<YuccaTwitterValue> valuesRet=new ArrayList<YuccaTwitterValue>();
		
		for (YuccaTwitterResult cur:twittTrovati) {
//			log.info("[TwitterInvoker::invokeTwitter] -----------------------");
//			log.info("[TwitterInvoker::invokeTwitter] getContributors "+cur.getContributors());
//			log.info("[TwitterInvoker::invokeTwitter] getCreatedAt "+cur.getCreatedAt());
			log.info("[TwitterInvoker::invokeTwitter] getGetText "+cur.getGetText());
//			log.info("[TwitterInvoker::invokeTwitter] getHashTags "+cur.getHashTags());
//			log.info("[TwitterInvoker::invokeTwitter] getLang "+cur.getLang());
//			log.info("[TwitterInvoker::invokeTwitter] getMedia "+cur.getMedia());
//			log.info("[TwitterInvoker::invokeTwitter] getMediaUrl "+cur.getMediaUrl());
//			log.info("[TwitterInvoker::invokeTwitter] getPlaceName "+cur.getPlaceName());
//			log.info("[TwitterInvoker::invokeTwitter] getSource "+cur.getSource());
//			log.info("[TwitterInvoker::invokeTwitter] getUrl "+cur.getUrl());
//			log.info("[TwitterInvoker::invokeTwitter] getCurrentUserRetweetId "+cur.getCurrentUserRetweetId());
//			log.info("[TwitterInvoker::invokeTwitter] getFavoriteCount "+cur.getFavoriteCount());
//			log.info("[TwitterInvoker::invokeTwitter] getLat "+cur.getLat());
//			log.info("[TwitterInvoker::invokeTwitter] getLon "+cur.getLon());
//			log.info("[TwitterInvoker::invokeTwitter] getMediaCnt "+cur.getMediaCnt());
//			log.info("[TwitterInvoker::invokeTwitter] getRetweetCount "+cur.getRetweetCount());
//			log.info("[TwitterInvoker::invokeTwitter] getTweetid "+cur.getTweetid());
//			log.info("[TwitterInvoker::invokeTwitter] isFavorited "+cur.isFavorited());
//			log.info("[TwitterInvoker::invokeTwitter] isPossiblySensitive "+cur.isPossiblySensitive());
//			log.info("[TwitterInvoker::invokeTwitter] isRetweet "+cur.isRetweet());
//			log.info("[TwitterInvoker::invokeTwitter] isRetweetedByMe "+cur.isRetweetedByMe());
//			log.info("[TwitterInvoker::invokeTwitter] isTruncated "+cur.isTruncated());
//			
			
			YuccaTwitterValue curValue=new YuccaTwitterValue();
			curValue.setComponents(cur);
			curValue.setTime(cur.getCreatedAt());
			valuesRet.add(0,curValue);
			
		}


		YuccaInvokeResult resultChiamata=new YuccaInvokeResult();
		resultChiamata.setMaxId(maxId);
		resultChiamata.setValuesRet(valuesRet);
		
		log.info("[TwitterInvoker::invokeTwitter] RESULT --- END ");
		log.info("[TwitterInvoker::invokeTwitter] maxId="+maxId);
		
		return resultChiamata;
	}

	
	private String escapeJson(String instr) {
		String retTmp=JSONObject.quote(instr);
		
		
		String ret=retTmp.substring(1,retTmp.length()-1);
		
		return ret;
	}

}
