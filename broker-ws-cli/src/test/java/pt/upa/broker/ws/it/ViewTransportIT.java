package pt.upa.broker.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;


import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class ViewTransportIT extends BaseBrokerIT{

	@Test
	public void testViewTransport() throws Exception{
		String[] result = brokerClient.requestTransport(VALID_LOCATION, VALID_LOCATION, VALID_EVEN_PRICE).split(" ");
		String id = result[11];
		
		assertEquals(id,  brokerClient.viewTransport(id).getId());
	}
	
	@Test(expected = UnknownTransportFault_Exception.class)
	public void testUnknownID() throws Exception{
		brokerClient.viewTransport(UNKNOWN_ID);
	}
}
