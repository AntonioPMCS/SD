package pt.upa.handlers;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;

import pt.upa.ca.ws.cli.AuthorityClient;
import pt.upa.crypt.Digest;
import pt.upa.crypt.SecureRandomGen;


@SuppressWarnings("restriction")
public class BrokerHandler implements SOAPHandler<SOAPMessageContext> {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	AuthorityClient CA = null;
	
	public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
    	System.out.println("#------------------------------------------------------------------------#");
    	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        
    	if (outbound) {
            System.out.println("Outbound SOAP message:");
            logOperationType(smc);
            handleOutgoingMsg(smc);
        } else {
            System.out.println("Inbound SOAP message:");
            logOperationType(smc);
            handleIncomingMsg(smc);
        }
    	
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        //logToSystemOut(smc);
        return true;
    }

    // nothing to clean up
    public void close(MessageContext messageContext) {
    }

    private void handleOutgoingMsg(SOAPMessageContext smc) {
    	SOAPMessage message = smc.getMessage();
    	try {
    		
        	//Convert Soap msg passed to string
			message.writeTo(baos);
			String convertedSoap = baos.toString();
			
			//Generate Nounce (byte array)
			SecureRandomGen generator = new SecureRandomGen();
			byte[] random = generator.getRandomNumber();
	        
			//Digest Message
			Digest digestor = new Digest();
			digestor.digestMessage(convertedSoap);
			
			//Put two together
			
			
			//Cipher with own privateKey
			
			
			//Put result on soad header
			
		} catch (Exception e1) {
			System.out.printf("Exception in handler: %s%n", e1);
		} 
    }
    
    private void handleIncomingMsg(SOAPMessageContext smc){
    	
    }
    
    private void logOperationType(SOAPMessageContext smc){
    	SOAPMessage message = smc.getMessage();
    	SOAPBody sb;
		try {
			sb = message.getSOAPBody();
			
			
			@SuppressWarnings("rawtypes")
			Iterator it = sb.getChildElements();
			while(it.hasNext()){
				 Node node=(Node)it.next();
				 Element ele=(Element)node;
				 System.out.println(ele.getLocalName());
				 if(ele.getTextContent().length() != 0);
				 	System.out.println(ele.getTextContent());
			}
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
