package pt.upa.ca.ws;

import javax.jws.WebService;

import pt.upa.crypt.KeyManager;

@WebService(endpointInterface = "pt.upa.ca.ws.Authority")
public class AuthorityImpl implements Authority{
	private String name = "CertificateAuthority";
	private KeyManager keyManager;
	
	public AuthorityImpl(){
		keyManager = new KeyManager();
		try {
			keyManager.generateRSAKeys("transporter-ws",1);
			keyManager.generateRSAKeys("transporter-ws",2);
			keyManager.generateRSAKeys("broker-ws",1);
			keyManager.generateRSAKeys("broker-ws",2);
			keyManager.generateRSAKeys("ca-ws",1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String ping(String msg) {
		return "hello from Certificate Authority, your msg was: "+msg;
	}
}
