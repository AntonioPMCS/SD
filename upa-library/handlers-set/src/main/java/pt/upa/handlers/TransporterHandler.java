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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.upa.ca.ws.cli.AuthorityClient;
import pt.upa.ca.ws.cli.AuthorityClientException;
import pt.upa.crypt.Cypher;
import pt.upa.crypt.Digest;
import pt.upa.crypt.KeyManager;

public class TransporterHandler implements SOAPHandler<SOAPMessageContext> {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	AuthorityClient authority = null;
	KeyManager keyManager = new KeyManager();
	private final String TRANSPORTER_STORE_PASS = "ins3cur3";
	private final String CA_KEY_PASS = "1nsecure";
	private final String CA_ALIAS = "ca";
	
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
			
			//Get Nounce
			String nounce = headerElements.get("Nounce");
			
			//Get Digest
			String digest = headerElements.get("Digest");
			
			//Get Broker Certificate
			byte[] brokerCertByteArray = authority.getBrokerCertificate();
			CertificateFactory cf   = CertificateFactory.getInstance("X.509");
			Certificate brokerCert = cf.generateCertificate(new ByteArrayInputStream(brokerCertByteArray));
			
			System.out.println(brokerCert);
			//Get Public Key of Central Authority Certificate
			//TODO: Fazer para consoante o caso ir à pasta transporter1 ou transporter2
			String keystoreFilename = "./TransporterSecurity/UpaTransporter1/UpaTransporter1.jks";
    	    FileInputStream fIn = new FileInputStream(keystoreFilename);
    	    KeyStore keystore = KeyStore.getInstance("JKS");
    	    keystore.load(fIn, TRANSPORTER_STORE_PASS.toCharArray());
    	    PublicKey caPublicKey = (PublicKey) keystore.getKey(CA_ALIAS, CA_KEY_PASS.toCharArray());
			
    	    System.out.println("CHAVE PUBLICA CA");
    	    System.out.println(caPublicKey.toString());
    	    /*
    	    brokerCert.verify(caPublicKey);
    	    
    	    //Decipher nounce and digest
    	    Cypher cypher = new Cypher();
    	    byte[] digestResult = parseBase64Binary(digest);
    	    byte[] nounceResult = parseBase64Binary(nounce);
    	    byte[] decipheredDigestResult = cypher.decipherWithPublicKey(digestResult, caPublicKey);
    	    byte[] decipheredNounceResult = cypher.decipherWithPublicKey(nounceResult, caPublicKey);
    	    
    	    //make new digest from soap body
    	    message.writeTo(baos);
			String convertedSoap = baos.toString();
			
			Digest digestor = new Digest();
			byte[] digestedMsg = digestor.digestMessage(convertedSoap);
			
			//CHeck if they are equals
			if(digestedMsg != decipheredDigestResult){
				//TODO: Throw SOAP Fault
				System.out.println("ERRO: Sao diferentes...nao é suposto");
			}
				
			System.out.println("Ok está tudo bem!!");
    	    */
			
			
			
			
    	}catch (Exception e){
    		System.out.println(e.getMessage());
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
