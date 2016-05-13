package pt.upa.handlers;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;


import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class AttackerHandler implements SOAPHandler<SOAPMessageContext> {
	private final String SCHEMA_PREFIX = "Teste";
	private boolean attack = false;

	public Set<QName> getHeaders() {
        return null;
    }

	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		if (outbound) {
			handleOutgoingMsg(smc);
		} else {
			handleIncomingMsg(smc);
		}

		return true;
	}

	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	// nothing to clean up
	public void close(MessageContext messageContext) {
	}

	 private void handleIncomingMsg(SOAPMessageContext smc) {
		try {
			SOAPMessage message = smc.getMessage();
			SOAPBody sb = message.getSOAPBody();
			@SuppressWarnings("rawtypes")
			Iterator it = sb.getChildElements();
			while(it.hasNext()){
			
				Node node=(Node)it.next();
				NodeList childs=node.getChildNodes();
				Element ele = (Element)node;
				if ( ele.getLocalName().equals("ping") ) {
					if (childs.item(0).getTextContent().equals("ATAQUEJA")) { attack=true; }
				}
			}
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}
	  }

	 private void handleOutgoingMsg(SOAPMessageContext smc){
	     SOAPMessage message = smc.getMessage();
		if (attack==true) {
			try{
				SOAPBody sb = message.getSOAPBody();
				@SuppressWarnings("rawtypes")
				Iterator it = sb.getChildElements();
				while(it.hasNext()){
				
					Node node=(Node)it.next();
					NodeList childs=node.getChildNodes();
					Element ele = (Element)node;
					if ( ele.getLocalName().equals("pingResponse") ) {
						childs.item(0).setTextContent("ATTACK");
						//ele.setAttribute("return", "ATTACK"); //a o <return> na mensagem SOAP
					}
					
					/*NodeList list = node.getChildNodes();
				
					for(int i = 0; i < list.getLength(); i++){
						Element ele=(Element)node;
						pingReturn=ele.getLocalName();
						ele.setAttribute("return", "ATTACK"); //a o <return> na mensagem SOAP
					}*/
				}
				
				 // Get Procedure Return value
			} catch (Exception e1) {
				System.out.println(e1.getMessage());
				e1.printStackTrace();
			} finally {
				attack=false;
			}
		}
	}
}

