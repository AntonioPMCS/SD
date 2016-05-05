package pt.upa.handlers;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.upa.ca.ws.cli.AuthorityClient;
import pt.upa.crypt.Cypher;
import pt.upa.crypt.Digest;
import pt.upa.crypt.SecureRandomGen;

@SuppressWarnings("restriction")
public class TransporterHandler implements SOAPHandler<SOAPMessageContext> {
	AuthorityClient CA = null;
	
	public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
    	System.out.println("#------------------------------------------------------------------------#");
    	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        
    	if (outbound) {
            System.out.println("Outbound SOAP message:");
            //logOperationType(smc);
            handleOutgoingMsg(smc);
        } else {
            System.out.println("Inbound SOAP message:");
           // logOperationType(smc);
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
    }
    
    private void handleIncomingMsg(SOAPMessageContext smc){
    	Map<String, String> headerElements = new HashMap<String, String>();
    	SOAPMessage message = smc.getMessage();
    	
    	try{
    		SOAPBody sb = message.getSOAPBody();
    		SOAPHeader sh;
    		
    		//Get Elements from header
    		sh = message.getSOAPHeader();
			@SuppressWarnings("rawtypes")
			Iterator it = sh.getChildElements();
			while(it.hasNext()){
				 Node node=(Node)it.next();
				 NodeList list2 = node.getChildNodes();
				 
				 for(int i = 0; i < list2.getLength(); i++){
					 Element ele=(Element)node;
					 headerElements.put(ele.getLocalName(), ele.getTextContent());
				 }
			}
			
			//Get Certificate
			String certificate = headerElements.get("Certificate");
			System.out.println(certificate);
			
			//Get Nounce
			String nounce = headerElements.get("Nounce");
			System.out.println(nounce);
			
			//Get Digest
			String digest = headerElements.get("Digest");
			
			
    	}catch (SOAPException e) {
			e.printStackTrace();
		}
		
		
    }
    
    private void logOperationType(SOAPMessageContext smc){
    	SOAPMessage message = smc.getMessage();
    	SOAPHeader sh;
		try {
			sh = message.getSOAPHeader();
			
			
			@SuppressWarnings("rawtypes")
			Iterator it = sh.getChildElements();
			while(it.hasNext()){
			     System.out.println("#################################");
				 Node node=(Node)it.next();
				 NodeList list2 = node.getChildNodes();
				 
				 for(int i = 0; i < list2.getLength(); i++){
					 Element ele=(Element)node;
					 System.out.println(ele.getLocalName()); //-> tem de ser equal a Certificate, Nounce, Digest
					 if(ele.getTextContent().length() != 0);{
						 System.out.println("-----");
						 System.out.println(ele.getTextContent());
					 }
				 
				 }
				 	
			}
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
