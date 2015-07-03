package org.csi.yucca.twitterpoller.business;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csi.yucca.twitterpoller.constants.SDPTwitterConfig;
import org.csi.yucca.twitterpoller.dto.YuccaInvokeResult;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterQuery;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterResult;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterValue;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Query.Unit;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterInvoker {

	private static final Logger log=Logger.getLogger("org.csi.yucca.twitterpoller");

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

		log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] consumerKey "+consumerKey);
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] consumerSecret "+consumerSecret);
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtUsertoken() "+twitterQuery.getTwtUserToken());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtTokenSecret() "+twitterQuery.getTwtTokenSecret());
		

		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		User user = twitter.verifyCredentials();
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter]user.getName()    "+user.getName());
		Query query = new Query(twitterQuery.getTwtQuery());

		//GEOLOCATION
		if (twitterQuery.getTwtGeolocLat()!=null && twitterQuery.getTwtGeolocLon()!=null && twitterQuery.getTwtGeolocRadius()!=null && twitterQuery.getTwtGeolocUnit()!=null) {
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

		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] ++++++++++++++++++++++ query.toString() "+query.toString());
			
		QueryResult result = twitter.search(query);

		
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] ++++++++++++++++++++++ result.getCount() "+result.getCount());

		
		long maxId=-1;
		ArrayList<YuccaTwitterResult> twittTrovati = new ArrayList<YuccaTwitterResult>();
		for (Status status : result.getTweets()) {
			YuccaTwitterResult cur = new YuccaTwitterResult();

			log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] elaboro: " + status.getId() + " -- "+status.getText());
			
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
		    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		    df.setTimeZone(tz);			
			cur.setCreatedAt(df.format(status.getCreatedAt()));
			
			
			
			cur.setCurrentUserRetweetId(status.getCurrentUserRetweetId());
			cur.setFavoriteCount(status.getFavoriteCount());
			cur.setFavorited(status.isFavorited());
			cur.setGetText(status.getText());
			
			
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
				String media=status.getMediaEntities()[0].getId()+"||||"+status.getMediaEntities()[0].getType()+"||||"+status.getMediaEntities()[0].getMediaURL();
				cur.setMedia(media);
				cur.setMediaCnt(status.getMediaEntities().length);
				cur.setMediaUrl(status.getMediaEntities()[0].getMediaURL());
			}
			
			//PLACE
			if (status.getPlace()!=null)cur.setPlaceName(status.getPlace().getFullName()+" ("+status.getPlace().getCountry() + "-"+status.getPlace().getCountryCode()+")");
			
			cur.setPossiblySensitive(status.isPossiblySensitive());
			cur.setRetweet(status.isRetweeted());
			cur.setRetweetedByMe(status.isRetweetedByMe());
			cur.setRetweetCount(status.getRetweetCount());
			cur.setSource(status.getSource());
			cur.setTruncated(status.isTruncated());
			cur.setTweetid(status.getId());
			if (status.getId()>maxId) maxId=status.getId();
			
	
			if (status.getURLEntities()!=null && status.getURLEntities().length>0) {
				cur.setUrl(status.getURLEntities()[0].getURL());
			}
			
			twittTrovati.add(cur);
			
		}

		log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] RESULT");

		ArrayList<YuccaTwitterValue> valuesRet=new ArrayList<YuccaTwitterValue>();
		
		for (YuccaTwitterResult cur:twittTrovati) {
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] -----------------------");
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getContributors "+cur.getContributors());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getCreatedAt "+cur.getCreatedAt());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getGetText "+cur.getGetText());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getHashTags "+cur.getHashTags());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getLang "+cur.getLang());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getMedia "+cur.getMedia());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getMediaUrl "+cur.getMediaUrl());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getPlaceName "+cur.getPlaceName());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getSource "+cur.getSource());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getUrl "+cur.getUrl());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getCurrentUserRetweetId "+cur.getCurrentUserRetweetId());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getFavoriteCount "+cur.getFavoriteCount());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getLat "+cur.getLat());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getLon "+cur.getLon());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getMediaCnt "+cur.getMediaCnt());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getRetweetCount "+cur.getRetweetCount());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] getTweetid "+cur.getTweetid());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] isFavorited "+cur.isFavorited());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] isPossiblySensitive "+cur.isPossiblySensitive());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] isRetweet "+cur.isRetweet());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] isRetweetedByMe "+cur.isRetweetedByMe());
			log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] isTruncated "+cur.isTruncated());
			
			
			YuccaTwitterValue curValue=new YuccaTwitterValue();
			curValue.setComponents(cur);
			curValue.setTime(cur.getCreatedAt());
			valuesRet.add(curValue);
			
		}


		YuccaInvokeResult resultChiamata=new YuccaInvokeResult();
		resultChiamata.setMaxId(maxId);
		resultChiamata.setValuesRet(valuesRet);
		
		log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] RESULT --- END ");
		log.log(Level.INFO, "[TwitterInvoker::invokeTwitter] maxId="+maxId);
		
		return resultChiamata;
	}


}
