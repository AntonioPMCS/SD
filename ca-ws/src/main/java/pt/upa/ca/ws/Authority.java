package pt.upa.ca.ws;

import javax.jws.WebService;

@WebService
public interface Authority {

	String ping(String msg);
}
