package pt.upa.handlers;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;

import pt.upa.ca.ws.cli.AuthorityClient;
import pt.upa.crypt.Cypher;
import pt.upa.crypt.Digest;
import pt.upa.crypt.SecureRandomGen;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

@SuppressWarnings("restriction")
public class BrokerHandler implements SOAPHandler<SOAPMessageContext> {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	AuthorityClient CA = null;
	private final String BROKER_STORE_PASS = "ins3cur3";
	private final String BROKER_KEY_PASS = "1nsecure";
	private final String BROKER_CERTIFICATE_ALIAS = "UpaBroker";
	private final String CA_CERTIFICATE_ALIAS = "UpaBroker";
	private final String SCHEMA_PREFIX = "Teste";
	
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
    		SOAPPart sp = message.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            
    		//Get Keystore
    		String keystoreFilename = "./UpaBrokerSecurity/UpaBroker.jks";
    	    FileInputStream fIn = new FileInputStream(keystoreFilename);
    	    KeyStore keystore = KeyStore.getInstance("JKS");
    	    keystore.load(fIn, BROKER_STORE_PASS.toCharArray());
    	    
    	    
			message.writeTo(baos);
			String convertedSoap = baos.toString();
			System.out.println("SOAP MSG");
			System.out.println(convertedSoap);
			//Generate Nounce (byte array)
			SecureRandomGen generator = new SecureRandomGen();
			byte[] random = generator.getRandomNumber();
	        
			//Digest Message
			Digest digestor = new Digest();
			byte[] digestedMsg = digestor.digestMessage(convertedSoap);
			
			//Get Private Key from Broker Certificate
			PrivateKey pk = (PrivateKey) keystore.getKey("UpaBroker", BROKER_KEY_PASS.toCharArray());
			
			//Cipher nounce and msg
			Cypher cypher = new Cypher();
			byte[] cipheredRandom = cypher.cypherWithPrivateKey(random, pk);
			byte[] cipheredDigestMsg = cypher.cypherWithPrivateKey(digestedMsg, pk);
			
            //Add header
            SOAPHeader sh = se.getHeader();
            if (sh == null)
                sh = se.addHeader();

            // add header element (name, namespace prefix, namespace)
            
            //SOAPHeaderElement security = sh.addHeaderElement(new QName("Broker", "Certificate", SCHEMA_PREFIX));
            SOAPHeaderElement nounce = sh.addHeaderElement(new QName("Broker", "Nounce", SCHEMA_PREFIX));
            SOAPHeaderElement digest = sh.addHeaderElement(new QName("Broker", "Digest", SCHEMA_PREFIX));
           
            //SOAPElement headerCert = security.addChildElement("BrokerCertificate", SCHEMA_PREFIX);
            //headerCert.addTextNode(printBase64Binary(certificate));
            SOAPElement headerRandom = nounce.addChildElement("CipheredRandom", SCHEMA_PREFIX);
            headerRandom.addTextNode(printBase64Binary(cipheredRandom));
            SOAPElement headerDigest = digest.addChildElement("CipheredDigest", SCHEMA_PREFIX);
            headerDigest.addTextNode(printBase64Binary(cipheredDigestMsg));
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
/*
 * // import helper methods to print byte[]
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

...

    // encoding binary data with base 64
    System.out.println("Encoding to Base64 ...");
    String result = printBase64Binary(cipherBytes);

...

    // decoding string in base 64
    System.out.println("Decoding from Base64 ...");
    byte[] result = parseBase64Binary(cipherText);
*/
