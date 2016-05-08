package pt.upa.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Node;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import java.security.MessageDigest;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import pt.upa.ca.ws.cli.AuthorityClient;
import pt.upa.ca.ws.cli.AuthorityClientException;
import pt.upa.crypt.Cypher;
import pt.upa.crypt.Digest;
import pt.upa.crypt.KeyManager;
import pt.upa.crypt.SecureRandomGen;

public class TransporterHandler implements SOAPHandler<SOAPMessageContext> {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	AuthorityClient authority = null;
	KeyManager keyManager = new KeyManager();
	public static final String BROKER_CERTIFICATE_PROPERTY = "brokerCertificate";
	private final String TRANSPORTER_STORE_PASS = "ins3cur3";
	private final String TRANSPORTER_KEY_PASS = "1nsecure";
	private final String CA_KEY_PASS = "1nsecure";
	private final String CA_ALIAS = "ca";
	private final String SCHEMA_PREFIX = "Teste";
	public static final String  TRANSPORTER_NAME_PROPERTY = "transporterName";
	
	public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
    	System.out.println("#------------------------------------------------------------------------#");
    	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	try {
			authority = new AuthorityClient("http://localhost:8086/ca-ws/endpoint");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	if (outbound) {
            System.out.println("Outbound SOAP message:");
            //logOperationType(smc);
            handleOutgoingMsg(smc);
            printMessageContext(smc);
        } else {
            System.out.println("Inbound SOAP message:");
            //logOperationType(smc);
            handleIncomingMsg(smc);
            printMessageContext(smc);
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
    	
    	try {
    		SOAPPart sp = message.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            String tName = getTransporterNameFromContext(smc);
            
    		//Get Keystore
    		String keystoreFilename = (tName.equals("UpaTransporter1") ? "./TransporterSecurity/UpaTransporter1.jks" : 
    																	 "./TransporterSecurity/UpaTransporter2.jks");
    	    FileInputStream fIn = new FileInputStream(keystoreFilename);
    	    KeyStore keystore = KeyStore.getInstance("JKS");
    	    keystore.load(fIn,TRANSPORTER_STORE_PASS.toCharArray());
    	    
    	    
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
			PrivateKey pk = (PrivateKey) keystore.getKey("UpaBroker", TRANSPORTER_KEY_PASS.toCharArray());
			
			//Cipher nounce and msg
			Cypher cypher = new Cypher();
			byte[] cipheredRandom = cypher.cypherWithPrivateKey(random, pk);
			byte[] cipheredDigestMsg = cypher.cypherWithPrivateKey(digestedMsg, pk);
			
            //Add header
            SOAPHeader sh = se.getHeader();
            if (sh == null)
                sh = se.addHeader();

            // add header element (name, namespace prefix, namespace)
            SOAPHeaderElement transporter = sh.addHeaderElement(new QName("Broker", "TransporterName", SCHEMA_PREFIX));
            SOAPHeaderElement nounce = sh.addHeaderElement(new QName("Broker", "Nounce", SCHEMA_PREFIX));
            SOAPHeaderElement digest = sh.addHeaderElement(new QName("Broker", "Digest", SCHEMA_PREFIX));
           
            SOAPElement headerName = transporter.addChildElement("TransporterName", SCHEMA_PREFIX);
            headerName.addTextNode(tName);
            SOAPElement headerRandom = nounce.addChildElement("CipheredRandom", SCHEMA_PREFIX);
            headerRandom.addTextNode(printBase64Binary(cipheredRandom));
            SOAPElement headerDigest = digest.addChildElement("CipheredDigest", SCHEMA_PREFIX);
            headerDigest.addTextNode(printBase64Binary(cipheredDigestMsg));
		} catch (Exception e1) {
			System.out.printf("Exception in handler: %s%n", e1);
		} 
    	
    }
    
    private void handleIncomingMsg(SOAPMessageContext smc){
    	Map<String, String> headerElements = new HashMap<String, String>();
    	SOAPMessage message = smc.getMessage();
    	SOAPPart sp = message.getSOAPPart();
    	
    	
    	
    	try{
    		SOAPEnvelope se = sp.getEnvelope();
    		SOAPBody sb = message.getSOAPBody();
    		SOAPHeader sh = message.getSOAPHeader();
    		
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
			
			//Get Nounce
			String nounce = headerElements.get("Nounce");
			
			//Get Digest
			String digest = headerElements.get("Digest");
			
			//Get TransporterName
			String tName = headerElements.get("TransporterName");
			
			Certificate brokerCert = getBrokerCertificateFromContext(smc);
			
			//Get Broker Certificate from Context
			if(brokerCert == null){
				System.out.println("Pediste Certificate broker à cA");
				//If null go get it to the CA
				byte[] brokerCertByteArray = authority.getBrokerCertificate();
				CertificateFactory cf   = CertificateFactory.getInstance("X.509");
				brokerCert = cf.generateCertificate(new ByteArrayInputStream(brokerCertByteArray));
				
				//Put it in the context so we can use it later
				smc.put(BROKER_CERTIFICATE_PROPERTY, brokerCert);
				smc.setScope(BROKER_CERTIFICATE_PROPERTY, Scope.APPLICATION);
				
				//TODO: Preciso mesmo de por uma variavel no servidor com isto? não pode só ficar no context?
			}
			
			PublicKey brokerPubKey = brokerCert.getPublicKey();
			
			//Get key store for the given transporter
			String keystoreFilename = null;
			if(tName.equals("UpaTransporter1")){
				keystoreFilename = "./TransporterSecurity/UpaTransporter1/UpaTransporter1.jks";
			}
			else if(tName.equals("UpaTransporter2")){
				keystoreFilename = "./TransporterSecurity/UpaTransporter2/UpaTransporter2.jks";
			}
				
    	    FileInputStream fIn = new FileInputStream(keystoreFilename);
    	    KeyStore keystore = KeyStore.getInstance("JKS");
    	    keystore.load(fIn, TRANSPORTER_STORE_PASS.toCharArray());
    	    
    	    //Get ca certificate and it's public key
    	    Certificate caCertificate = keystore.getCertificate("ca");
    	    PublicKey caPublicKey = caCertificate.getPublicKey();
    	    
    	    brokerCert.verify(caPublicKey);
    	    
    	    //Decipher nounce and digest
    	    Cypher cypher = new Cypher();
    	    byte[] digestResult = parseBase64Binary(digest);
    	    byte[] nounceResult = parseBase64Binary(nounce);
    	    byte[] decipheredDigestResult = cypher.decipherWithPublicKey(digestResult, brokerPubKey);
    	    byte[] decipheredNounceResult = cypher.decipherWithPublicKey(nounceResult, brokerPubKey);
    	    
    	    sh.removeContents();
    	    message.saveChanges();
    	    
    	    message.writeTo(baos);
			String convertedSoap = baos.toString();
			
			Digest digestor = new Digest();
			byte[] digestedMsg = digestor.digestMessage(convertedSoap);
			
			//CHeck if they are equals
			if(!MessageDigest.isEqual(digestedMsg, decipheredDigestResult)){
		        SOAPFault soapFault = sb.addFault();
		        soapFault.setFaultString("Security Error: Message was tampered.");
		        throw new SOAPFaultException(soapFault);
			}
			
    	}catch (Exception e){
    		System.out.println(e.getMessage());
    		e.printStackTrace();
		}
		
		
    }
    
    private void logInboundMsg(SOAPMessageContext smc){
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
    
    private void logOutBoundMsg(SOAPMessageContext smc){
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
    
    private void printMessageContext(MessageContext map) {
        System.out.println("Message context: (scope,key,value)");
        try {
            java.util.Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object value = map.get(key);

                String keyString;
                if (key == null)
                    keyString = "null";
                else
                    keyString = key.toString();

                String valueString;
                if (value == null)
                    valueString = "null";
                else
                    valueString = value.toString();

                Object scope = map.getScope(keyString);
                String scopeString;
                if (scope == null)
                    scopeString = "null";
                else
                    scopeString = scope.toString();
                scopeString = scopeString.toLowerCase();

                System.out.println("(" + scopeString + "," + keyString + ","
                        + valueString + ")");
            }

        } catch (Exception e) {
            System.out.printf("Exception while printing message context: %s%n",
                    e);
        }
    }
    
    private Certificate getBrokerCertificateFromContext(MessageContext map) {
        java.util.Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            if(key.toString().equals(BROKER_CERTIFICATE_PROPERTY)){
            	Object value = map.get(key);
            	return (Certificate) value;
            }
        }
		return null;
    }

    private String getTransporterNameFromContext(MessageContext map){
    	java.util.Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            if(key.toString().equals(TRANSPORTER_NAME_PROPERTY)){
            	Object value = map.get(key);
            	return value.toString();
            }
        }
		return null;
    }
}
