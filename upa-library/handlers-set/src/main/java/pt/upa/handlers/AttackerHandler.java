package pt.upa.handlers;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

public class AttackerHandler implements SOAPHandler<SOAPMessageContext> {
	private final String SCHEMA_PREFIX = "Teste";
	private boolean attack = false;

	public Set<QName> getHeaders() {
        return null;
    }

	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println("#-----------------------------------------------#");
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		/*try { FIXME: Comunicar com a CA e preciso nest handler?
			authority = new AuthorityClient("http://localhost:8086/ca-ws/endpoint");
		} catch (AuthorityClientException e) {
			e.printStackTrace();
		}*/
		if (outbound) {
			System.out.println("->Handling outgoing message");
			handleOutgoingMsg(smc);
		} else {
			System.out.println("->Handling incoming message");
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

	 private void handleOutgoingMsg(SOAPMessageContext smc) {
    	SOAPMessage message = smc.getMessage();
		if (attack==true) {
			try{
				SOAPBody sb = message.getSOAPBody();
				SOAPBodyElement attack = sb.addBodyElement(new QName("Broker", "TransporterName", SCHEMA_PREFIX));
				SOAPElement attackTag = attack.addChildElement("attackTag", SCHEMA_PREFIX);
				attackTag.addTextNode("attackMessage");
			} catch (Exception e1) {
				System.out.println(e1.getMessage());
				e1.printStackTrace();
			} finally {
				attack=false;
			}
		} else {
			attack=true;
		} 

	}

	 private void handleIncomingMsg(SOAPMessageContext smc){
    	/*SOAPMessage message = smc.getMessage();
    	try{
    		SOAPBody sb = message.getSOAPBody();
    		SOAPHeader sh = message.getSOAPHeader();
		}catch (Exception e){
    		System.out.println(e.getMessage());
    		e.printStackTrace();
		}*/
	}
}

