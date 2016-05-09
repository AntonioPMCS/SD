package pt.upa.handlers;

import java.io.ByteArrayInputStream;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
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
import javax.xml.namespace.QName;
import javax.xml.soap.Node;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import java.security.MessageDigest;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pt.upa.ca.ws.cli.AuthorityClient;
import pt.upa.crypt.Cypher;
import pt.upa.crypt.Digest;
import pt.upa.crypt.SecureRandomGen;

public class TransporterHandler implements SOAPHandler<SOAPMessageContext> {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	AuthorityClient authority = null;
	private final String TRANSPORTER_STORE_PASS = "ins3cur3";
	private final String TRANSPORTER_KEY_PASS = "1nsecure";
	private final String SCHEMA_PREFIX = "Teste";
	public static final String  TRANSPORTER_NAME_PROPERTY = "transporterName";
	private Certificate brokerCert;
	
	public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
    	System.out.println("#------------------------------------------------------------------------#");
    	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	try {
			authority = new AuthorityClient("http://localhost:8086/ca-ws/endpoint");
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    	if (outbound) {
            System.out.println("Outbound SOAP message:");
            //logOperationType(smc);
            handleOutgoingMsg(smc);
        } else {
            System.out.println("Inbound SOAP message:");
            //logOperationType(smc);
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
    	baos.reset();
    	
    	try {
    		SOAPPart sp = message.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            String tName = (String) smc.get(TRANSPORTER_NAME_PROPERTY);
            
            
            String keystoreFilename = null;
    		//Get Keystore
            if(tName.equals("UpaTransporter1"))
            	keystoreFilename = "./TransporterSecurity/UpaTransporter1.jks";
            else if(tName.equals("UpaTransporter2"))
            	keystoreFilename = "./TransporterSecurity/UpaTransporter2.jks";
            
            
    	    FileInputStream fIn = new FileInputStream(keystoreFilename);
    	    KeyStore keystore = KeyStore.getInstance("JKS");
    	    keystore.load(fIn,TRANSPORTER_STORE_PASS.toCharArray());
    	    
    	    
			message.writeTo(baos);
			String convertedSoap = baos.toString();
			
			//Generate Nounce (byte array)
			SecureRandomGen generator = new SecureRandomGen();
			byte[] random = generator.getRandomNumber();
	        
			//Digest Message
			Digest digestor = new Digest();
			byte[] digestedMsg = digestor.digestMessage(convertedSoap);
			
			//Get Private Key from Transporter Certificate
			PrivateKey pk = (PrivateKey) keystore.getKey(tName, TRANSPORTER_KEY_PASS.toCharArray());
			
			/*-----TESTE*/
			Cypher cypher = new Cypher();
			System.out.println(printHexBinary(random));
			System.out.println("Asked "+tName+" certificate to CA.");
			byte[] brokerCertByteArray = authority.getTransporterCertificate(2);
			CertificateFactory cf   = CertificateFactory.getInstance("X.509");
			Certificate tCert2 = cf.generateCertificate(new ByteArrayInputStream(brokerCertByteArray));
			PublicKey publicKey = tCert2.getPublicKey();
			byte[] teste = cypher.cypherWithPrivateKey(random, pk);
			byte[] teste2=	cypher.decipherWithPublicKey(teste, publicKey);
			System.out.println(printHexBinary(teste2));
			//////////////
			
			
			//Cipher nounce and msg
			//Cypher cypher = new Cypher();
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
		} catch (Exception e) {
			System.out.printf("Exception in handler: %s%n", e);
    		e.printStackTrace();
		} 
    	
    }
    
    private void handleIncomingMsg(SOAPMessageContext smc){
    	Map<String, String> headerElements = new HashMap<String, String>();
    	SOAPMessage message = smc.getMessage();
    	SOAPPart sp = message.getSOAPPart();
    	baos.reset();
    	
    	
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
			
			
			//Get Broker Certificate
			if(brokerCert == null){
				System.out.println("Asked UpaBroker certificate to CA.");
				//If null go get it to the CA
				byte[] brokerCertByteArray = authority.getBrokerCertificate();
				CertificateFactory cf   = CertificateFactory.getInstance("X.509");
				brokerCert = cf.generateCertificate(new ByteArrayInputStream(brokerCertByteArray));
			}
			
			PublicKey brokerPubKey = brokerCert.getPublicKey();
			
			//Get key store for the given transporter
			String keystoreFilename = null;
			if(tName.equals("UpaTransporter1")){
				keystoreFilename = "./TransporterSecurity/UpaTransporter1.jks";
			}
			else if(tName.equals("UpaTransporter2")){
				keystoreFilename = "./TransporterSecurity/UpaTransporter2.jks";
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
    		System.out.printf("Exception in handler: %s%n", e);
    		e.printStackTrace();
		}
		
		
    }
}
