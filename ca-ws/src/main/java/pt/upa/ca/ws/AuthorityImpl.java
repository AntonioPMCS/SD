package pt.upa.ca.ws;

import javax.jws.WebService;

@WebService(endpointInterface = "pt.upa.ca.ws.Authority")
public class AuthorityImpl implements Authority{

	public String ping(String msg) {
		return "hello from Certificate Authority, your msg was: "+msg;
	}

}
