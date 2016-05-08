package pt.upa.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.upa.ca.ws.cli.AuthorityClient;
import pt.upa.ca.ws.cli.AuthorityClientException;
import pt.upa.crypt.Cypher;
import pt.upa.crypt.Digest;
import pt.upa.crypt.SecureRandomGen;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

@SuppressWarnings("restriction")
public class BrokerHandler implements SOAPHandler<SOAPMessageContext> {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	AuthorityClient authority = null;
	private final String BROKER_STORE_PASS = "ins3cur3";
	private final String BROKER_KEY_PASS = "1nsecure";
	private final String BROKER_CERTIFICATE_ALIAS = "UpaBroker";
	private final String CA_CERTIFICATE_ALIAS = "UpaBroker";
	private final String SCHEMA_PREFIX = "Teste";
	private Certificate caCertificate = null;
	private static final String TRANSPORTER1_CERTICATE_PROPERTY = "transporter1Certificate";
	private static final String TRANSPORTER2_CERTIFICATE_PROPERTY = "transporter2Certicate";
	public static final String  TRANSPORTER_NAME_PROPERTY = "transporterName";
	
	public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
    	System.out.println("#------------------------------------------------------------------------#");
    	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	try {
			authority = new AuthorityClient("http://localhost:8086/ca-ws/endpoint");
		} catch (AuthorityClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	if (outbound) {
            System.out.println("Outbound SOAP message:");
            handleOutgoingMsg(smc);
        } else {
            System.out.println("Inbound SOAP message:");
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
    	    
    	    //Gets Soap body to string
			message.writeTo(baos);
			String convertedSoap = baos.toString();
			
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
            SOAPHeaderElement transporter = sh.addHeaderElement(new QName("Broker", "TransporterName", SCHEMA_PREFIX));
            SOAPHeaderElement nounce = sh.addHeaderElement(new QName("Broker", "Nounce", SCHEMA_PREFIX));
            SOAPHeaderElement digest = sh.addHeaderElement(new QName("Broker", "Digest", SCHEMA_PREFIX));
           
            SOAPElement headerName = transporter.addChildElement("TransporterName", SCHEMA_PREFIX);
            headerName.addTextNode(getTransporterNameFromContext(smc));
            SOAPElement headerRandom = nounce.addChildElement("CipheredRandom", SCHEMA_PREFIX);
            headerRandom.addTextNode(printBase64Binary(cipheredRandom));
            SOAPElement headerDigest = digest.addChildElement("CipheredDigest", SCHEMA_PREFIX);
            headerDigest.addTextNode(printBase64Binary(cipheredDigestMsg));
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
    		e1.printStackTrace();
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
			
			//Get Transporter Certificate from context
			Certificate tCert = getTransporterCertificateFromContext(smc, tName);
			if(tCert == null){
				System.out.println("Pediste certificate transporter à CA");
				//If context doesn't has it, call Central Authority
				int nrCertificate = (tName.equals("UpaTransporter1")) ? 1 : 2;
				byte[] brokerCertByteArray = authority.getTransporterCertificate(nrCertificate);
				CertificateFactory cf   = CertificateFactory.getInstance("X.509");
				tCert = cf.generateCertificate(new ByteArrayInputStream(brokerCertByteArray));
				
				//PutCertificate in context so we don't have to ask for it again to the CA
				smc.put((nrCertificate == 1) ? TRANSPORTER1_CERTICATE_PROPERTY : TRANSPORTER2_CERTIFICATE_PROPERTY, tCert);
				smc.setScope((nrCertificate == 1) ? TRANSPORTER1_CERTICATE_PROPERTY : TRANSPORTER2_CERTIFICATE_PROPERTY, Scope.APPLICATION);
			}
			
			PublicKey tPubKey = tCert.getPublicKey();
	    	
			//Certificado do CA
			String keystoreFilename = "./UpaBrokerSecurity/UpaBroker.jks";
    	    FileInputStream fIn = new FileInputStream(keystoreFilename);
    	    KeyStore keystore = KeyStore.getInstance("JKS");
    	    keystore.load(fIn, BROKER_STORE_PASS.toCharArray());
    	    Certificate caCertificate = keystore.getCertificate("ca");
    	    
    	    
    	    PublicKey caPublicKey = caCertificate.getPublicKey();
    	    
    	    tCert.verify(caPublicKey);
    	    
    	    //Decipher nounce and digest
    	    Cypher cypher = new Cypher();
    	    byte[] digestResult = parseBase64Binary(digest);
    	    byte[] nounceResult = parseBase64Binary(nounce);
    	    byte[] decipheredDigestResult = cypher.decipherWithPublicKey(digestResult, tPubKey);
    	    byte[] decipheredNounceResult = cypher.decipherWithPublicKey(nounceResult, tPubKey);
    	    
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
    
    private void logOutboundMsg(SOAPMessageContext smc){
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
    
    private Certificate getTransporterCertificateFromContext(MessageContext map, String name){
    	System.out.println("NAME");
    	System.out.println(name);
    	String tName = name;
        java.util.Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            if(tName.equals("UpaTransporter1")){
            	if(key.toString().equals(TRANSPORTER1_CERTICATE_PROPERTY)){
            		Object value = map.get(key);
	            	return (Certificate) value;
            	}
            }
            else if(tName.equals("UpaTransporter2")){
            	if(key.toString().equals(TRANSPORTER2_CERTIFICATE_PROPERTY)){
           			Object value = map.get(key);
	            	return (Certificate) value;
           		}
            }
        }
		return null;
    }
    
    private String getTransporterNameFromContext(MessageContext map){
    	java.util.Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            System.out.println(key.toString() + " " + map.get(key).toString());
            if(key.toString().equals(TRANSPORTER_NAME_PROPERTY)){
            	Object value = map.get(key);
            	return value.toString();
            }
        }
		return null;
    }

}
