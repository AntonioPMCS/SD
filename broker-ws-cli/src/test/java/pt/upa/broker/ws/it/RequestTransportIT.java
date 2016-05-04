package pt.upa.broker.ws.it;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.transporter.ws.cli.TransporterClient;

public class RequestTransportIT extends BaseBrokerIT{
	
	@Test
	public void successfulRequest() throws Exception{
		assertEquals(0, brokerClient.listTransports().size());
		
		brokerClient.requestTransport(VALID_LOCATION, VALID_LOCATION, VALID_EVEN_PRICE);
		assertEquals(1, brokerClient.listTransports().size());
	}
	
	@Test(expected = UnavailableTransportFault_Exception.class)
	public void highPrice() throws Exception{
		brokerClient.requestTransport(VALID_LOCATION, VALID_LOCATION, HIGH_PRICE);
	}

	
	@Test(expected = UnknownLocationFault_Exception.class)
	public void unknownLocations() throws Exception{
		brokerClient.requestTransport(INVALID_LOCATION, VALID_LOCATION, HIGH_PRICE);
	}
	
	@Test(expected = InvalidPriceFault_Exception.class)
	public void negativePrice() throws Exception{
		brokerClient.requestTransport(VALID_LOCATION, VALID_LOCATION, NEGATIVE_PRICE);
	}

	//Teacher Tests
	// @Test
		// not tested for evaluation as stated in project Q&A:
		// http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/proj/faq.html
		public void testRequestTransportSouthZeroPrice() throws Exception {
			String id = brokerClient.requestTransport(SOUTH_1, CENTER_1, ZERO_PRICE);
			TransportView tv = brokerClient.viewTransport(id);
			assertEquals(ZERO_PRICE, tv.getPrice().intValue());
		}

		// @Test
		// not tested for evaluation as stated in project Q&A:
		// http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/proj/faq.html
		public void testRequestTransportCenterZeroPrice() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, CENTER_2, ZERO_PRICE);
			TransportView tv = brokerClient.viewTransport(id);
			assertEquals(ZERO_PRICE, tv.getPrice().intValue());
		}

		// @Test
		// not tested for evaluation as stated in project Q&A:
		// http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/proj/faq.html
		public void testRequestTransportNorthZeroPrice() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, NORTH_1, ZERO_PRICE);
			TransportView tv = brokerClient.viewTransport(id);
			assertEquals(ZERO_PRICE, tv.getPrice().intValue());
		}

		// ----------- Unitary Price --------------

		// @Test
		// not tested for evaluation as stated in project Q&A:
		// http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/proj/faq.html
		public void testRequestTransportSouthUnitaryPrice() throws Exception {
			String id = brokerClient.requestTransport(SOUTH_1, CENTER_1, UNITARY_PRICE);
			TransportView tv = brokerClient.viewTransport(id);
			assertEquals(ZERO_PRICE, tv.getPrice().intValue());
		}

		// @Test
		// not tested for evaluation as stated in project Q&A:
		// http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/proj/faq.html
		public void testRequestTransportCenterUnitaryPrice() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, CENTER_2, UNITARY_PRICE);
			TransportView tv = brokerClient.viewTransport(id);
			assertEquals(ZERO_PRICE, tv.getPrice().intValue());
		}

		// @Test
		// not tested for evaluation as stated in project Q&A:
		// http://disciplinas.tecnico.ulisboa.pt/leic-sod/2015-2016/labs/proj/faq.html
		public void testRequestTransportNorthUnitaryPrice() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, NORTH_1, UNITARY_PRICE);
			TransportView tv = brokerClient.viewTransport(id);
			assertEquals(ZERO_PRICE, tv.getPrice().intValue());
		}


		// ----------- Above upper limit price (> 100) --------------

		// No proposals expected
		
		@Test(expected = UnavailableTransportFault_Exception.class)
		public void testRequestTransportSouthAboveUpperLimitPrice() throws Exception {
			brokerClient.requestTransport(SOUTH_1, CENTER_1, PRICE_UPPER_LIMIT + ODD_INCREMENT);
		}

		@Test(expected = UnavailableTransportFault_Exception.class)
		public void testRequestTransportCenterAboveUpperLimitPrice() throws Exception {
			brokerClient.requestTransport(CENTER_1, CENTER_2, PRICE_UPPER_LIMIT + ODD_INCREMENT);
		}

		@Test(expected = UnavailableTransportFault_Exception.class)
		public void testRequestTransportNorthAboveUpperLimitPrice() throws Exception {
			brokerClient.requestTransport(CENTER_1, NORTH_1, PRICE_UPPER_LIMIT + ODD_INCREMENT);
		}


		// ----------- Below Smallest price (<=10) --------------

		// Odd Transporters operate in South and Center (e.g. UpaTransporter1)
		// Even Transporters operate in Center and North (e.g. UpaTransporter2)

		// If a request is issued in the South, only odd transporters respond 
		// (e.g. UpaTransporter1 ONLY)

		// If a request is issued in the Center, ALL transporters should respond 
		// (e.g. UpaTransporter1 AND UpaTransporter2)
		
		// If a request is issued in the North, only even transporters respond 
		// (e.g. UpaTransporter2 ONLY)
		
		@Test
		public void testRequestTransportSouthBelowSmallestPrice() throws Exception {
			String id = brokerClient.requestTransport(SOUTH_1, CENTER_1, PRICE_SMALLEST_LIMIT);
			TransportView tv = brokerClient.viewTransport(id);
			final int price = tv.getPrice().intValue();
			assertTrue(price >= ZERO_PRICE && price < PRICE_SMALLEST_LIMIT);
		}

		@Test
		public void testRequestTransportCenterBelowSmallestPrice() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT);
			TransportView tv = brokerClient.viewTransport(id);
			final int price = tv.getPrice().intValue();
			assertTrue(price >= ZERO_PRICE && price < PRICE_SMALLEST_LIMIT);
		}

		@Test
		public void testRequestTransportNorthBelowSmallestPrice() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, NORTH_1, PRICE_SMALLEST_LIMIT);
			TransportView tv = brokerClient.viewTransport(id);
			final int price = tv.getPrice().intValue();
			assertTrue(price >= ZERO_PRICE && price < PRICE_SMALLEST_LIMIT);
		}

		
		// ----------- Below upper limit price (>10 && <=100) --------------

		@Test
		public void testRequestTransportSouthOddPriceBelowUpperLimit() throws Exception {
			String id = brokerClient.requestTransport(SOUTH_1, CENTER_1, PRICE_SMALLEST_LIMIT + ODD_INCREMENT);
			TransportView tv = brokerClient.viewTransport(id);
			final int price = tv.getPrice().intValue();
			assertTrue(price >= ZERO_PRICE && price < PRICE_SMALLEST_LIMIT + ODD_INCREMENT);
			// should also check that it is an odd transporter (e.g. UpaTransporter1)
		}

		@Test(expected = UnavailableTransportPriceFault_Exception.class)
		public void testRequestTransportSouthEvenPriceBelowUpperLimit() throws Exception {
			brokerClient.requestTransport(SOUTH_1, CENTER_1, PRICE_SMALLEST_LIMIT + EVEN_INCREMENT);
		}

		@Test
		public void testRequestTransportCenterOddPriceBelowUpperLimit() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT + ODD_INCREMENT);
			TransportView tv = brokerClient.viewTransport(id);
			final int price = tv.getPrice().intValue();
			assertTrue(price >= ZERO_PRICE && price < PRICE_SMALLEST_LIMIT + ODD_INCREMENT);
			// should also check that it is an odd transporter (e.g. UpaTransporter1)
		}

		@Test
		public void testRequestTransportCenterEvenPriceBelowUpperLimit() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT + EVEN_INCREMENT);
			TransportView tv = brokerClient.viewTransport(id);
			final int price = tv.getPrice().intValue();
			assertTrue(price >= ZERO_PRICE && price < PRICE_SMALLEST_LIMIT + EVEN_INCREMENT);
			// should also check that it is an even transporter (e.g. UpaTransporter2)
		}

		@Test(expected = UnavailableTransportPriceFault_Exception.class)
		public void testRequestTransportNorthOddPriceBelowUpperLimit() throws Exception {
			brokerClient.requestTransport(CENTER_1, NORTH_1, (PRICE_SMALLEST_LIMIT + ODD_INCREMENT));
		}

		@Test
		public void testRequestTransportNorthEvenPriceBelowUpperLimit() throws Exception {
			String id = brokerClient.requestTransport(CENTER_1, NORTH_1, PRICE_SMALLEST_LIMIT + EVEN_INCREMENT);
			TransportView tv = brokerClient.viewTransport(id);
			final int price = tv.getPrice().intValue();
			assertTrue(price >= ZERO_PRICE && price < PRICE_SMALLEST_LIMIT + EVEN_INCREMENT);
			// should also check that it is an even transporter (e.g. UpaTransporter2)
		}

		
		// ----------- WSDL Exceptions --------------

		@Test(expected = UnknownLocationFault_Exception.class)
		public void testRequestTransportInvalidOrigin() throws Exception {
			brokerClient.requestTransport(EMPTY_STRING, SOUTH_1, PRICE_SMALLEST_LIMIT);
		}

		@Test(expected = UnknownLocationFault_Exception.class)
		public void testRequestTransportNullOrigin() throws Exception {
			brokerClient.requestTransport(null, CENTER_1, PRICE_SMALLEST_LIMIT);
		}

		@Test(expected = UnknownLocationFault_Exception.class)
		public void testRequestTransportInvalidDestination() throws Exception {
			brokerClient.requestTransport(NORTH_1, EMPTY_STRING, PRICE_SMALLEST_LIMIT);
		}

		@Test(expected = UnknownLocationFault_Exception.class)
		public void testRequestTransportNullDestination() throws Exception {
			brokerClient.requestTransport(SOUTH_2, null, PRICE_SMALLEST_LIMIT);
		}

		@Test(expected = UnknownLocationFault_Exception.class)
		public void testRequestTransportInvalidOD() throws Exception {
			brokerClient.requestTransport(EMPTY_STRING, EMPTY_STRING, PRICE_SMALLEST_LIMIT);
		}

		@Test(expected = UnknownLocationFault_Exception.class)
		public void testRequestTransportNullOD() throws Exception {
			brokerClient.requestTransport(null, null, PRICE_SMALLEST_LIMIT);
		}

		@Test(expected = InvalidPriceFault_Exception.class)
		public void testRequestTransportNegativePrice() throws Exception {
			brokerClient.requestTransport(CENTER_1, CENTER_2, INVALID_PRICE);
		}

		@Test(expected = UnavailableTransportFault_Exception.class)
		public void testRequestUnavailableLocation() throws Exception {
			brokerClient.requestTransport(SOUTH_1, NORTH_1, PRICE_SMALLEST_LIMIT);
		}
}
