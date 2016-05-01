package pt.upa.transporter.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test suite
 */
public class PingIT extends AbstractIT {

	/**
	 * Receive a non-null reply from the transporter that was pinged through
	 * CLIENT.
	 */
	@Test
	public void pingEmptyTest() {
		assertNotNull(CLIENT.ping("test"));
	}

	/*
	 * 	OUR TESTS
	 */
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
