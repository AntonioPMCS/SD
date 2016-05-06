package pt.upa.ca.ws;

import java.security.cert.Certificate;

import javax.jws.WebService;

@WebService
public interface Authority {

	String ping(String msg);
	
	byte[] getBrokerCertificate();
	byte[] getTransporterCertificate(int nr);
	
}
