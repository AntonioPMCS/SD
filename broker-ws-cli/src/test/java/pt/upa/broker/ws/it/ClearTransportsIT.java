package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class ClearTransportsIT extends BaseBrokerIT{
	@Test
	public void testclearJobs(){
		
		evenClient.clearJobs();
		brokerClient.clearTransports();
		assertEquals(true, brokerClient.listTransports().size() == 0);
	}
	
	//Teacher tests
	@Test(expected = UnknownTransportFault_Exception.class)
	public void testClearTransports() throws Exception {
		String rt = brokerClient.requestTransport(CENTER_1, SOUTH_1, PRICE_SMALLEST_LIMIT);
		brokerClient.clearTransports();
		assertEquals(0, brokerClient.listTransports().size());
		brokerClient.viewTransport(rt);
	}
}
