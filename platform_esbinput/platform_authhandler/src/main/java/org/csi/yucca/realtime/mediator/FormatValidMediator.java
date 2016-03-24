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

public class FormatValidMediator extends AbstractMediator {

	private String variabileResult="p_result";
	protected static final Log accounting = LogFactory.getLog("sdpaccounting");
	
	public boolean mediate(MessageContext synCtx) {
		String isValidFormat= "true";

		AccountingLog logging = new AccountingLog();
		logging.setTenantcode(axis2MessageContext.getServiceContext().getName());
		
		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
				.getAxis2MessageContext();
		
		axis2MessageContext.setProperty("messageType",	"application/json");
		axis2MessageContext.setProperty("ContentType",  "application/json");
		synCtx.setProperty("ContentType",  "application/json");
		
		
		trace.info("[FormatValidMediator::mediate] BEGIN");
		try {
			
			if (axis2MessageContext.getIncomingTransportName().equals("jms"))
			{
				logging.setUniqueid(""+);
			}
			else {
				Object headers = axis2MessageContext.getProperty("TRANSPORT_HEADERS");
				if (headers!=null)
				{
					Map headersMap = (Map) headers;
					trace.info("[FormatValidMediator::mediate] "+headersMap.get("UNIQUE_ID"));
					logging.setUniqueid(""+headersMap.get("UNIQUE_ID"));
				}
				
			}
			String msg = synCtx.getEnvelope().getBody().toString();
			// capire come contare o estrarre info
			logging.setApicode(msg);
			logging.setPath(+"|"+axis2MessageContext.getSoapAction());
		} catch (Exception e)
		{
			logging.setErrore("JSON Invalid");
			trace.warn("[FormatValidMediator::mediate] Message invalid",e);
			if ("POST".equals(axis2MessageContext.getProperty("HTTP_METHOD"))) {
				synCtx.getEnvelope().getBody().getFirstElement().detach();
			}
			//			trace.info("[FormatValidMediator::mediate] Raw message "+msg);
//			prepareMessage(synCtx,"<jsonObject><rawMessage>Impossible to detect!</rawMessage></jsonObject>");
			isValidFormat = "false";
		}
		
		accounting.info(logging.toString());
		
		synCtx.setProperty(variabileResult, isValidFormat);
		
		return true;
	}
	
	
	private void prepareMessage(MessageContext messageContext, String jsonMessageString) {
		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
				.getAxis2MessageContext();

		JsonUtil.newJsonPayload(axis2MessageContext, jsonMessageString, false, true);

//		
//		messageContext.setProperty("NIO-ACK-Requested", "true");
//		messageContext.setProperty("ContentType", "application/json");
//
//		// set the HTTP STATUS code
//		axis2MessageContext.removeProperty("NO_ENTITY_BODY");
//		axis2MessageContext.setProperty("messageType",	"application/json");
//		axis2MessageContext.setProperty("ContentType",  "application/json");
//
//		// Set the payload to be sent back
//		try {
//			RelayUtils.buildMessage(axis2MessageContext);
//
//			// This part is only needed for POST requests
//			if ("POST".equals(axis2MessageContext.getProperty("HTTP_METHOD"))) {
//				messageContext.getEnvelope().getBody().getFirstElement().detach();
//				axis2MessageContext.setProperty("ContentType","application/javascript");
//				messageContext.setProperty("message.builder.invoked","false");
//				axis2MessageContext.setProperty("org.apache.synapse.commons.json.JsonInputStream",null);
//			}
//			axis2MessageContext.setProperty("JSON_STRING",jsonMessageString);
//
//		} catch (Exception e) {
//			trace.error("[FormatValidMediator::mediate] prepareMessage",e);
//		}
	}

	public String getVariabileResult() {
		return variabileResult;
	}

	public void setVariabileResult(String variabileResult) {
		this.variabileResult = variabileResult;
	}

}
