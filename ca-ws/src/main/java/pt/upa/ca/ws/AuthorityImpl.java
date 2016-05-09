package pt.upa.ca.ws;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.jws.WebService;


@WebService(endpointInterface = "pt.upa.ca.ws.Authority")
public class AuthorityImpl implements Authority{
	private Certificate brokerCert;
	private Certificate transporter1Cert;
	private Certificate transporter2Cert;
	
	public AuthorityImpl(){
		CertificateFactory fact;
		try {
			fact = CertificateFactory.getInstance("X.509");
			
			//Get broker Certificate
			System.out.println("Obtaining broker certificate...");
			FileInputStream is = new FileInputStream ("./CASecurity/UpaBroker.cer");
			brokerCert = fact.generateCertificate(is);
			
			
			//Get Transporter1 Certificate
			System.out.println("Obtaining transporter1 certificate...");
			FileInputStream es = new FileInputStream ("./CASecurity/UpaTransporter1.cer");
			transporter1Cert = fact.generateCertificate(es);
			
			//Get Transporter2 Certificate
			System.out.println("Obtaining transporter2 certificate...");
			FileInputStream sd = new FileInputStream ("./CASecurity/UpaTransporter2.cer");
			transporter2Cert = fact.generateCertificate(sd);
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] getTransporterCertificate(int nr) {
		try{
			if(nr == 1)
				return transporter1Cert.getEncoded();
			else if(nr == 2)
				return transporter2Cert.getEncoded();
			
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
