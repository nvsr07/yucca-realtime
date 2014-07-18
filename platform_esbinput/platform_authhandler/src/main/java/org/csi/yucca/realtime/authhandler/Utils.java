package org.csi.yucca.realtime.authhandler;

import org.apache.commons.logging.LogFactory;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
public class Utils {
	private static final Log log = LogFactory.getLog(Utils.class);

	public static void sendFault(
			org.apache.synapse.MessageContext messageContext, int status) {
		org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
				.getAxis2MessageContext();

		axis2MC.setProperty("HTTP_SC", Integer.valueOf(status));
		messageContext.setResponse(true);
		messageContext.setProperty("RESPONSE", "true");
		messageContext.setTo(null);
		axis2MC.removeProperty("NO_ENTITY_BODY");
		String method = (String) axis2MC.getProperty("HTTP_METHOD");
		if (method.matches("^(?!.*(POST|PUT)).*$")) {
			axis2MC.setProperty("messageType", "application/xml");
		}

		axis2MC.removeProperty("ContentType");
		Map headers = (Map) axis2MC.getProperty("TRANSPORT_HEADERS");
		if (headers != null) {
			headers.remove("Authorization");

			headers.remove("Authorization");

			if ((messageContext.getProperty("error_message_type") != null)
					&& (messageContext.getProperty("error_message_type")
							.toString().equalsIgnoreCase("application/json"))) {
				axis2MC.setProperty("messageType", "application/json");
			}

			headers.remove("Host");
		}
		Axis2Sender.sendBack(messageContext);
	}

	public static void setFaultPayload(
			org.apache.synapse.MessageContext messageContext, OMElement payload) {
		OMElement firstChild = messageContext.getEnvelope().getBody()
				.getFirstElement();
		if (firstChild != null) {
			firstChild.insertSiblingAfter(payload);
			firstChild.detach();
		} else {
			messageContext.getEnvelope().getBody().addChild(payload);
		}
	}

	public static void setSOAPFault(
			org.apache.synapse.MessageContext messageContext, String code,
			String reason, String detail) {
		SOAPFactory factory = (messageContext.isSOAP11()) ? OMAbstractFactory
				.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory();

		OMDocument soapFaultDocument = factory.createOMDocument();
		SOAPEnvelope faultEnvelope = factory.getDefaultFaultEnvelope();
		soapFaultDocument.addChild(faultEnvelope);

		SOAPFault fault = faultEnvelope.getBody().getFault();
		if (fault == null) {
			fault = factory.createSOAPFault();
		}

		SOAPFaultCode faultCode = factory.createSOAPFaultCode();
		if (messageContext.isSOAP11()) {
			faultCode.setText(new QName(fault.getNamespace().getNamespaceURI(),
					code));
		} else {
			SOAPFaultValue value = factory.createSOAPFaultValue(faultCode);
			value.setText(new QName(fault.getNamespace().getNamespaceURI(),
					code));
		}
		fault.setCode(faultCode);

		SOAPFaultReason faultReason = factory.createSOAPFaultReason();
		if (messageContext.isSOAP11()) {
			faultReason.setText(reason);
		} else {
			SOAPFaultText text = factory.createSOAPFaultText();
			text.setText(reason);
			text.setLang("en");
			faultReason.addSOAPText(text);
		}
		fault.setReason(faultReason);

		SOAPFaultDetail soapFaultDetail = factory.createSOAPFaultDetail();
		soapFaultDetail.setText(detail);
		fault.setDetail(soapFaultDetail);
		Iterator iterator;
		if (messageContext.getEnvelope() != null) {
			SOAPHeader soapHeader = messageContext.getEnvelope().getHeader();
			if (soapHeader != null) {
				for (iterator = soapHeader.examineAllHeaderBlocks(); iterator
						.hasNext();) {
					Object o = iterator.next();
					if (o instanceof SOAPHeaderBlock) {
						SOAPHeaderBlock header = (SOAPHeaderBlock) o;
						faultEnvelope.getHeader().addChild(header);
					} else if (o instanceof OMElement) {
						faultEnvelope.getHeader().addChild((OMElement) o);
					}
				}
			}
		}
		try {
			messageContext.setEnvelope(faultEnvelope);
		} catch (AxisFault af) {
			log.error("Error while setting SOAP fault as payload", af);
			return;
		}

		if (messageContext.getFaultTo() != null)
			messageContext.setTo(messageContext.getFaultTo());
		else if (messageContext.getReplyTo() != null)
			messageContext.setTo(messageContext.getReplyTo());
		else {
			messageContext.setTo(null);
		}

		if (messageContext.getMessageID() != null) {
			RelatesTo relatesTo = new RelatesTo(messageContext.getMessageID());
			messageContext.setRelatesTo(new RelatesTo[] { relatesTo });
		}
	}

	public static String getAllowedOrigin(String currentRequestOrigin) {
		
		return null;
	}

	
}
