package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class ListTransportsIT extends BaseBrokerIT{

	@Test
	public void testListJobsNoJobs(){
		evenClient.clearJobs();
		brokerClient.clearTransports();
		assertEquals(true, brokerClient.listTransports().size() == 0);
	}
	
	@Test
	public void testListJobsReturnsJobs() throws Exception{
		brokerClient.clearTransports();
		brokerClient.requestTransport(VALID_LOCATION, VALID_LOCATION, VALID_EVEN_PRICE);
		assertEquals(List.class.getComponentType(), brokerClient.listTransports().getClass().getComponentType());
	}
}
