package org.csi.yucca.realtime.mediator;



import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.swing.text.StyledEditorKit.ItalicAction;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.codehaus.jettison.json.JSONObject;
import org.csi.yucca.realtime.AccountingLog;
import org.wso2.throttle.ThrottleException;

public class AccountingMediator extends AbstractMediator {

	protected static final Log accounting = LogFactory.getLog("sdpaccounting");
	
	public boolean mediate(MessageContext synCtx) {
try{
		AccountingLog logging = new AccountingLog();
		
		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
				.getAxis2MessageContext();
		logging.setTenantcode(axis2MessageContext.getServiceContext().getName());
		
			
		Object headers = axis2MessageContext.getProperty("TRANSPORT_HEADERS");
		if (headers!=null )
		{
			Map headersMap = (Map) headers;
			for (Iterator iterator = headersMap.keySet().iterator(); iterator.hasNext();) {
				String type = (String) iterator.next();
				trace.info("*** header"+type);
			}
			if (headersMap.get("UNIQUE_ID")!=null)
			{
				logging.setUniqueid(""+headersMap.get("UNIQUE_ID"));
			}
			if (headersMap.get("JMS_MESSAGE_ID")!=null)
			{
				logging.setUniqueid(""+headersMap.get("JMS_MESSAGE_ID"));
			}
			if (headersMap.get("JMS_DESTINATION")!=null)
			{
				logging.setDestination(""+headersMap.get("JMS_DESTINATION"));
			}
			
		}
		
		logging.setErrore(""+synCtx.getProperty("accountingError"));
//		logging.setDestination(""+synCtx.getProperty("accountingDestination"));
		logging.setSensorStream(""+synCtx.getProperty("sourceId")+"|"+synCtx.getProperty("streamId"));
		logging.setNumeroEventi(1);
		
		accounting.info(logging.toString());
}
catch (Throwable e)
{
	trace.error("ERRRR"+e.getMessage(), e);
}
		return true;
	}
	
	

}
