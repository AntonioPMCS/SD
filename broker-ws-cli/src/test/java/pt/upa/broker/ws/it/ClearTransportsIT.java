package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ClearTransportsIT extends BaseBrokerIT{
	@Test
	public void testclearJobs(){
		
		evenClient.clearJobs();
		brokerClient.clearTransports();
		assertEquals(true, brokerClient.listTransports().size() == 0);
	}
}
