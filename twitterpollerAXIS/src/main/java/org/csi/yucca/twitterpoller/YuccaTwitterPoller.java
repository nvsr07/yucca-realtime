package org.csi.yucca.twitterpoller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csi.yucca.twitterpoller.business.TwitterInvoker;
import org.csi.yucca.twitterpoller.dto.YuccaInvokeResult;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterCepRecord;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterException;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterQuery;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterStreamConfig;
import org.csi.yucca.twitterpoller.mongo.YuccaTwitterMongoDataAcces;

import twitter4j.TwitterException;

public class YuccaTwitterPoller{
	private static final Logger log=Logger.getLogger("org.csi.yucca.twitterpoller");

	
	
	public String twtEcho(String pippo) throws Exception{
		log.log(Level.INFO, "[YuccaTwitterPoller::twtEcho] BEGIN ");
		return "echo from "+pippo;
		
	}
	public YuccaTwitterCepRecord invokeTwitter(YuccaTwitterQuery twitterQuery, YuccaTwitterStreamConfig streamInfo) throws Exception{
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] BEGIN ");
		
		try {
			
		
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] streamInfo.getStreamCode " +streamInfo.getStreamCode());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] streamInfo.getTenatcode " +streamInfo.getTenatcode());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] streamInfo.getVirtualEntityCode " +streamInfo.getVirtualEntityCode());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] streamInfo.getResetLastId " +streamInfo.getResetLastId());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] streamInfo.getStreamVersion " +streamInfo.getStreamVersion());

		
		
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtGeolocUnit " +twitterQuery.getTwtGeolocUnit());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtLang " +twitterQuery.getTwtLang());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtQuery " +twitterQuery.getTwtQuery());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtResultType " +twitterQuery.getTwtResultType());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtTokenSecret " +twitterQuery.getTwtTokenSecret());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtUntil " +twitterQuery.getTwtUntil());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtUserToken " +twitterQuery.getTwtUserToken());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtGeolocLat " +twitterQuery.getTwtGeolocLat());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtGeolocLon " +twitterQuery.getTwtGeolocLon());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtGeolocRadius " +twitterQuery.getTwtGeolocRadius());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] twitterQuery.getTwtLastSearchId " +twitterQuery.getTwtLastSearchId());
		
		
		YuccaTwitterMongoDataAcces mongoDAO=new YuccaTwitterMongoDataAcces();
		
		Long lastId=mongoDAO.retrieveLastTweetId(streamInfo.getStreamCode(), streamInfo.getTenatcode(), streamInfo.getVirtualEntityCode(),streamInfo.getStreamVersion(),(1==streamInfo.getResetLastId()));
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] lastId recuperato -->"+lastId);
		
		
		if (null!=lastId) twitterQuery.setTwtLastSearchId(lastId);
		
		TwitterInvoker invoke=new TwitterInvoker();
		
		
		YuccaInvokeResult resultChiamata=invoke.invokeTwitter(twitterQuery);
		lastId = resultChiamata.getMaxId();
		
		YuccaTwitterCepRecord ret= new YuccaTwitterCepRecord();
		ret.setSensor(streamInfo.getVirtualEntityCode());
		ret.setStream(streamInfo.getStreamCode());
		ret.setValues(resultChiamata.getValuesRet());
		
		
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] lastId da twitter -->"+lastId);

		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] LAT -->"+twitterQuery.getTwtGeolocLat());
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] LON -->"+twitterQuery.getTwtGeolocLon());

		
		if (lastId!=-1)  mongoDAO.updateLastId(streamInfo.getStreamCode(), streamInfo.getTenatcode(), streamInfo.getVirtualEntityCode(),lastId);
		


		
		
		return ret;
		
		} catch (TwitterException twe) {
			YuccaTwitterException ecc= new YuccaTwitterException();
			ecc.setTwtErrorCode("TWT_"+twe.getErrorCode());
			ecc.setTwtErrorMessage(twe.getErrorMessage());
			YuccaTwitterCepRecord ret= new YuccaTwitterCepRecord();
			ret.setErrore(ecc);
			
			return ret;
		} finally {
			log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] END ");

		}
	

	}

}