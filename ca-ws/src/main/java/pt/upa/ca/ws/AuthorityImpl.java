package pt.upa.ca.ws;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.jws.WebService;


@WebService(endpointInterface = "pt.upa.ca.ws.Authority")
public class AuthorityImpl implements Authority{
	private String name = "CertificateAuthority";
	private final String BROKER_CERTIFICATE_ALIAS = "UpaBroker";
	private Certificate brokerCert;
	private KeyStore keystore;
	
	public AuthorityImpl(){
		
		//Get broker Certificate
		CertificateFactory fact;
		try {
			fact = CertificateFactory.getInstance("X.509");
			FileInputStream is = new FileInputStream ("./CASecurity/UpaBroker.cer");
			brokerCert = fact.generateCertificate(is);
		    System.out.println(brokerCert);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    
	}

	public String ping(String msg) {
		return "hello from Certificate Authority, your msg was: "+msg;
	}

	@Override
	public byte[] getBrokerCertificate() {
		try {
			return brokerCert.getEncoded();
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] getTransporterCertificate(int nr) {
		// TODO Auto-generated method stub
		return null;
	}
}
