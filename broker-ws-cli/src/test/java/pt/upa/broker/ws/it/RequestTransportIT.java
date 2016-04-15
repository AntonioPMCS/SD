package pt.upa.broker.ws.it;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
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
	
	@Test(expected = UnavailableTransportPriceFault_Exception.class)
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

}
