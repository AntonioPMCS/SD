package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

public class RequestJobIT extends BaseTransporterIT{

	@Test(expected = BadLocationFault_Exception.class)
	public void testRequestJobNullOrigin() throws Exception{
		evenClient.requestJob(null, VALID_LOCATION, VALID_PRICE);
	}
	
	@Test(expected = BadLocationFault_Exception.class)
	public void testRequestJobNullDestination() throws Exception{
		evenClient.requestJob(VALID_LOCATION, null, VALID_PRICE);
	}
	
	@Test(expected = BadLocationFault_Exception.class)
	public void testRequestJobUnknownOriginLocation() throws Exception{
		evenClient.requestJob(UNKNOWN_LOCATION, VALID_LOCATION, VALID_PRICE);
	}
	
	@Test(expected = BadLocationFault_Exception.class)
	public void testRequestJobUnknownDestinationLocation() throws Exception{
		evenClient.requestJob(VALID_LOCATION, UNKNOWN_LOCATION, VALID_PRICE);
	}
	
	@Test(expected = BadPriceFault_Exception.class) 
	public void testRequestJobNegativePrice() throws Exception{
		evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, NEGATIVE_PRICE);
	}
	
	@Test
	public void testRequestJobHighPrice() throws Exception{
		assertEquals(null, evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, HIGH_PRICE));
	}
	
	@Test
    public void testPriceLowerThan10() throws Exception{
    	int offeredPrice = evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, LOWER_THAN_TEN_PRICE).getJobPrice();
    	assertTrue(offeredPrice < LOWER_THAN_TEN_PRICE);
    }
	
	@Test
	public void testRequestJobNotWorkingSouth() throws Exception{
		assertEquals(null, evenClient.requestJob(SOUTH_LOCATION, VALID_LOCATION, VALID_PRICE));
	}
	
	@Test
	public void testRequestJobNotWorkingNorth() throws Exception{
		assertEquals(null, oddClient.requestJob(NORTH_LOCATION, VALID_LOCATION, VALID_PRICE));
	}
	
	@Test
	public void testRequestJobNameEvenPriceEven() throws Exception{
		int offeredPrice = evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, EVEN_PRICE).getJobPrice();
    	assertTrue(offeredPrice < EVEN_PRICE);
	}
	
	@Test
	public void testRequestJobNameEvenPriceOdd() throws Exception{
		int offeredPrice = evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, ODD_PRICE).getJobPrice();
    	assertTrue(offeredPrice > ODD_PRICE);
	}
	
	@Test
	public void testRequestJobNameOddPriceEven() throws Exception{
		int offeredPrice = oddClient.requestJob(VALID_LOCATION, VALID_LOCATION, EVEN_PRICE).getJobPrice();
    	assertTrue(offeredPrice > EVEN_PRICE);
	}
	
	@Test
	public void testRequestJobNameOddPriceOdd() throws Exception{
		int offeredPrice = oddClient.requestJob(VALID_LOCATION, VALID_LOCATION, ODD_PRICE).getJobPrice();
    	assertTrue(offeredPrice < ODD_PRICE);
	}
	
	@Test
	public void testRequestCorrectJobCreation() throws Exception{
		evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		assertEquals(1, evenClient.listJobs().size());
	}
}
