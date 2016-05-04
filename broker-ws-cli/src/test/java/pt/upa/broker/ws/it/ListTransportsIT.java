package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.upa.broker.ws.TransportView;

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
	
	//Teacher Tests
	@Test
	public void testListTransports() throws Exception {
		brokerClient.clearTransports();// To start fresh
		String j1 = brokerClient.requestTransport(SOUTH_1, CENTER_1, PRICE_SMALLEST_LIMIT);
		String j2 = brokerClient.requestTransport(NORTH_1, CENTER_1, PRICE_SMALLEST_LIMIT);
		String j3 = brokerClient.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT);
		List<TransportView> jtvs = new ArrayList<>();
		jtvs.add(brokerClient.viewTransport(j1));
		jtvs.add(brokerClient.viewTransport(j2));
		brokerClient.viewTransport(j3);

		List<TransportView> tList = brokerClient.listTransports();
		assertEquals(3, tList.size());
		int counter = 0;
		
		for(TransportView tv : tList)
			for(TransportView jtv : jtvs)
				if((jtv.getId().equals(tv.getId())) && jtv.getOrigin().equals(tv.getOrigin())
						&& jtv.getDestination().equals(tv.getDestination()) && jtv.getPrice() == tv.getPrice()
						&& jtv.getState().toString().equals(tv.getState().toString()))
					counter++;
		assertEquals(2, counter);
	}
}
