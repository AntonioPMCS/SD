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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
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
import javax.xml.ws.ProtocolException;


import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class BrokerHandler implements SOAPHandler<SOAPMessageContext> {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	AuthorityClient authority = null;
	private final String BROKER_STORE_PASS = "ins3cur3";
	private final String BROKER_KEY_PASS = "1nsecure";
	private final String SCHEMA_PREFIX = "Teste";
	public static final String  TRANSPORTER_NAME_PROPERTY = "transporterName";
	public String tName = null;
	private Certificate tCert1 = null;
	private Certificate tCert2 = null;
	private ArrayList<String> storedNounces = new ArrayList<String>();
	String keystoreFilename = "../broker-ws/UpaBrokerSecurity/UpaBroker.jks";
	
	public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
    	System.out.println("#-----------------------------------------------#");
    	Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	
    	try {
		authority = new AuthorityClient("http://localhost:8086/ca-ws/endpoint");
	
		if (outbound) {
		    System.out.println("->Handling outgoing message");
		    handleOutgoingMsg(smc);
		} else {
		    System.out.println("->Handling incoming message");
		    handleIncomingMsg(smc);
		}
	} catch (AuthorityClientException e) {
		e.printStackTrace();
	}
    	
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
	System.out.println("----------------------------- ESTOU no handlefault ----------------------------------------- ");
        return false;
    }

    // nothing to clean up
    public void close(MessageContext messageContext) {
    }

    private void handleOutgoingMsg(SOAPMessageContext smc) {
    	SOAPMessage message = smc.getMessage();
    	tName = (String) smc.get(TRANSPORTER_NAME_PROPERTY);
    	baos.reset();
    	try {
    		SOAPPart sp = message.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            
	    //Get Keystore
	   
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
			byte[] digestedRandom = digestor.digestMessage(printBase64Binary(random));
			
			//Get Private Key from Broker Certificate
			PrivateKey pk = (PrivateKey) keystore.getKey("UpaBroker", BROKER_KEY_PASS.toCharArray());
			
			//Cipher nounce and msg
			Cypher cypher = new Cypher();
			byte[] cipheredRandom = cypher.cypherWithPrivateKey(digestedRandom, pk);
			byte[] cipheredDigestMsg = cypher.cypherWithPrivateKey(digestedMsg, pk);
			
            //Add header
            SOAPHeader sh = se.getHeader();
            if (sh == null)
                sh = se.addHeader();

            // add header element (name, namespace prefix, namespace)
            SOAPHeaderElement transporter = sh.addHeaderElement(new QName("Broker", "TransporterName", SCHEMA_PREFIX));
            SOAPHeaderElement cipherNounce = sh.addHeaderElement(new QName("Broker", "CipherNounce", SCHEMA_PREFIX));
            SOAPHeaderElement nounce = sh.addHeaderElement(new QName("BRoker", "Nounce", SCHEMA_PREFIX));
            SOAPHeaderElement digest = sh.addHeaderElement(new QName("Broker", "Digest", SCHEMA_PREFIX));
           
            SOAPElement headerName = transporter.addChildElement("TransporterName", SCHEMA_PREFIX);
            headerName.addTextNode(tName);
            SOAPElement headerCipherRandom = cipherNounce.addChildElement("CipheredRandom", SCHEMA_PREFIX);
            headerCipherRandom.addTextNode(printBase64Binary(cipheredRandom));
            SOAPElement headerNounce =  nounce.addChildElement("Nounce", SCHEMA_PREFIX);
            headerNounce.addTextNode(printBase64Binary(random));
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
    	baos.reset();
	try {
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
		//Get Ciphered nounce
		String cipherNounce = headerElements.get("CipherNounce");
		
		//Get Nounce
		String nounce = headerElements.get("Nounce");
		
		//Get Digest
		String digest = headerElements.get("Digest");
		
		//Get TransporterName
		String tName = headerElements.get("TransporterName");
		
		//Get Transporter Certificate from context
		if(tName.equals("UpaTransporter1") && tCert1 == null){
			System.out.println("Asked "+tName+" certificate to CA.");
			byte[] brokerCertByteArray = authority.getTransporterCertificate(1);
			CertificateFactory cf   = CertificateFactory.getInstance("X.509");
			tCert1 = cf.generateCertificate(new ByteArrayInputStream(brokerCertByteArray));
		}
		else if(tName.equals("UpaTransporter2") && tCert2 == null){
			System.out.println("Asked "+tName+" certificate to CA.");
			byte[] brokerCertByteArray = authority.getTransporterCertificate(2);
			CertificateFactory cf   = CertificateFactory.getInstance("X.509");
			tCert2 = cf.generateCertificate(new ByteArrayInputStream(brokerCertByteArray));
		}
		
		Certificate tCert = null;
		if(tName.equals("UpaTransporter1")){
			tCert = tCert1;
		}
			
		else if(tName.equals("UpaTransporter2")){
			tCert = tCert2;
		}
		
		PublicKey tPubKey = tCert.getPublicKey();

		//Certificado do CA
		String keystoreFilename = "../broker-ws/UpaBrokerSecurity/UpaBroker.jks";
		FileInputStream fIn = new FileInputStream(keystoreFilename);
		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(fIn, BROKER_STORE_PASS.toCharArray());
		Certificate caCertificate = keystore.getCertificate("ca");
		PublicKey caPublicKey = caCertificate.getPublicKey();
		
		tCert.verify(caPublicKey);
		
		//Decipher nounce and digest
		Cypher cypher = new Cypher();
		byte[] digestResult = parseBase64Binary(digest);
		byte[] nounceResult = parseBase64Binary(cipherNounce);
		byte[] decipheredDigestResult = cypher.decipherWithPublicKey(digestResult, tPubKey);
		byte[] decipheredNounceResult = cypher.decipherWithPublicKey(nounceResult, tPubKey);
		
		sh.removeContents();
		message.saveChanges();
		
		message.writeTo(baos);
		String convertedSoap = baos.toString();
		
		Digest digestor = new Digest();
		byte[] digestedMsg = digestor.digestMessage(convertedSoap);
		byte[] digestedNounce = digestor.digestMessage(nounce);
		//CHeck if they are equals
		if(!MessageDigest.isEqual(digestedMsg, decipheredDigestResult)){
		SOAPFault soapFault = sb.addFault();
		soapFault.setFaultString("Security Error: Message was tampered.");
		throw new SOAPFaultException(soapFault);
		}
		if(!MessageDigest.isEqual(digestedNounce, decipheredNounceResult)){
			SOAPFault soapFault = sb.addFault();
			soapFault.setFaultString("Security Error: Message was tampered.");
			throw new SOAPFaultException(soapFault); 
		}
		else{
			if(storedNounces.contains(digestedNounce)){
				SOAPFault soapFault = sb.addFault();
				soapFault.setFaultString("Security Error: Message is repeated!.");
				throw new SOAPFaultException(soapFault);
			}
			else
				storedNounces.add(nounce);
		}
	} catch (SOAPFaultException e1) {
		 throw e1;
	} catch (Exception e) {
		System.out.println(e.getMessage());
	}

    }
}
