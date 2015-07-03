package org.csi.yucca.twitterpoller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csi.yucca.twitterpoller.business.TwitterInvoker;
import org.csi.yucca.twitterpoller.dto.YuccaInvokeResult;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterCepRecord;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterQuery;
import org.csi.yucca.twitterpoller.dto.YuccaTwitterStreamConfig;
import org.csi.yucca.twitterpoller.mongo.YuccaTwitterMongoDataAcces;

public class YuccaTwitterPoller{
	private static final Logger log=Logger.getLogger("org.csi.yucca.twitterpoller");

	public YuccaTwitterCepRecord invokeTwitter(YuccaTwitterQuery twitterQuery, YuccaTwitterStreamConfig streamInfo) throws Exception{
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] BEGIN ");
		
		
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
		
		log.log(Level.INFO, "[YuccaTwitterPoller::invokeTwitter] END ");
		
		return ret;
	}

}