package org.csi.yucca.realtime.mediator;



import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;

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
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.codehaus.jettison.json.JSONObject;

public class FormatValidMediator extends AbstractMediator {

	private String variabileResult="p_result";
	
	public boolean mediate(MessageContext synCtx) {
		String isValidFormat= "true";

		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx)
				.getAxis2MessageContext();
		
		axis2MessageContext.setProperty("messageType",	"application/json");
		axis2MessageContext.setProperty("ContentType",  "application/json");
		synCtx.setProperty("ContentType",  "application/json");
		
		
		trace.info("[FormatValidMediator::mediate] BEGIN");
		try {
			String msg = synCtx.getEnvelope().getBody().toString();
		} catch (Exception e)
		{
			trace.warn("[FormatValidMediator::mediate] Message invalid",e);
			if ("POST".equals(axis2MessageContext.getProperty("HTTP_METHOD"))) {
				synCtx.getEnvelope().getBody().getFirstElement().detach();
			}
			//			trace.info("[FormatValidMediator::mediate] Raw message "+msg);
//			prepareMessage(synCtx,"<jsonObject><rawMessage>Impossible to detect!</rawMessage></jsonObject>");
			isValidFormat = "false";
		}
		
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
