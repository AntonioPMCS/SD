package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

public class PingIT extends BaseTransporterIT {

	@Test
	public void testPingString(){
		assertEquals(evenClient.ping(TEST_STRING), evenClient.getWsName()+" responding to ping request...Message given: "+TEST_STRING);
	}
	
	@Test
	public void testPingNull() throws Exception{
		assertEquals(evenClient.ping(null), evenClient.getWsName()+" responding to ping request...Message given: "+null);
	}
	
	@Test
	public void testPingEmptyString(){
		assertEquals(evenClient.ping(EMPTY_STRING), evenClient.getWsName()+" responding to ping request...Message given: ");
	}
}
