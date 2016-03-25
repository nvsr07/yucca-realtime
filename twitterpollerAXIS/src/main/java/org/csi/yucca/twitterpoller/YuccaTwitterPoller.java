package org.csi.yucca.twitterpoller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.csi.yucca.twitterpoller.business.TwitterInvoker;
import org.csi.yucca.twitterpoller.dto.YuccaInvokeResult;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterCepRecord;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterException;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterQuery;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterStreamConfig;
import org.csi.yucca.twitterpoller.mongo.YuccaTwitterMongoDataAcces;

import twitter4j.TwitterException;

public class YuccaTwitterPoller{
	private static final Log log=LogFactory.getLog("org.csi.yucca.twitterpoller");

	
	
	public String twtEcho(String pippo) throws Exception{
		log.info("[YuccaTwitterPoller::twtEcho] BEGIN ");
		return "echo from "+pippo;
		
	}
	public YuccaTwitterCepRecord invokeTwitter(YuccaTwitterQuery twitterQuery, YuccaTwitterStreamConfig streamInfo) throws Exception{
		log.info("[YuccaTwitterPoller::invokeTwitter] BEGIN ");
		
		try {
			
		
		log.info("[YuccaTwitterPoller::invokeTwitter] streamInfo.getStreamCode " +streamInfo.getStreamCode());
		log.info("[YuccaTwitterPoller::invokeTwitter] streamInfo.getTenatcode " +streamInfo.getTenatcode());
		log.info("[YuccaTwitterPoller::invokeTwitter] streamInfo.getVirtualEntityCode " +streamInfo.getVirtualEntityCode());
		log.info("[YuccaTwitterPoller::invokeTwitter] streamInfo.getResetLastId " +streamInfo.getResetLastId());
		log.info("[YuccaTwitterPoller::invokeTwitter] streamInfo.getStreamVersion " +streamInfo.getStreamVersion());

		
		
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtGeolocUnit " +twitterQuery.getTwtGeolocUnit());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtLang " +twitterQuery.getTwtLang());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtQuery " +twitterQuery.getTwtQuery());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtResultType " +twitterQuery.getTwtResultType());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtTokenSecret " +twitterQuery.getTwtTokenSecret());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtUntil " +twitterQuery.getTwtUntil());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtUserToken " +twitterQuery.getTwtUserToken());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtGeolocLat " +twitterQuery.getTwtGeolocLat());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtGeolocLon " +twitterQuery.getTwtGeolocLon());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtGeolocRadius " +twitterQuery.getTwtGeolocRadius());
		log.info("[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtLastSearchId " +twitterQuery.getTwtLastSearchId());
		
		
		YuccaTwitterMongoDataAcces mongoDAO=new YuccaTwitterMongoDataAcces();
		
		Long lastId=mongoDAO.retrieveLastTweetId(streamInfo.getStreamCode(), streamInfo.getTenatcode(), streamInfo.getVirtualEntityCode(),streamInfo.getStreamVersion(),(1==streamInfo.getResetLastId()));
		log.info("[YuccaTwitterPoller::invokeTwitter] lastId recuperato -->"+lastId);
		
		
		if (null!=lastId) twitterQuery.setTwtLastSearchId(lastId);
		
		TwitterInvoker invoke=new TwitterInvoker();
		
		
		YuccaInvokeResult resultChiamata=invoke.invokeTwitter(twitterQuery);
		lastId = resultChiamata.getMaxId();
		
		YuccaTwitterCepRecord ret= new YuccaTwitterCepRecord();
		ret.setSensor(streamInfo.getVirtualEntityCode());
		ret.setStream(streamInfo.getStreamCode());
		ret.setValues(resultChiamata.getValuesRet());
		
		
		log.info("[YuccaTwitterPoller::invokeTwitter] lastId da twitter -->"+lastId);

		log.info("[YuccaTwitterPoller::invokeTwitter] LAT -->"+twitterQuery.getTwtGeolocLat());
		log.info("[YuccaTwitterPoller::invokeTwitter] LON -->"+twitterQuery.getTwtGeolocLon());

		
		if (lastId!=-1)  mongoDAO.updateLastId(streamInfo.getStreamCode(), streamInfo.getTenatcode(), streamInfo.getVirtualEntityCode(),lastId);
		


		
		
		return ret;
		
		} catch (TwitterException twe) {
			log.error("[YuccaTwitterPoller::invokeTwitter] ERROR: "+twe);

			
			YuccaTwitterException ecc= new YuccaTwitterException();
			ecc.setTwtErrorCode("TWT_"+twe.getErrorCode());
			ecc.setTwtErrorMessage(twe.getErrorMessage());
			YuccaTwitterCepRecord ret= new YuccaTwitterCepRecord();
			ret.setErrore(ecc);
			
			return ret;
		} finally {
			log.info("[YuccaTwitterPoller::invokeTwitter] END ");

		}
	

	}

}