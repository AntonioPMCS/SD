package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

public class PingIT extends BaseTransporterIT {

	@Test
	public void testPingString(){
		assertEquals(client.ping(TEST_STRING), "TransporterServer responding to ping request...Message given: "+TEST_STRING);
	}
	
	//TODO: pôr expected de excepção
	@Test
	public void testPingNull() throws Exception{
		client.ping(null);
	}
	
	@Test
	public void testPingEmptyString(){
		assertEquals(client.ping(EMPTY_STRING), "TransporterServer responding to ping request...Message given: ");
	}
}
