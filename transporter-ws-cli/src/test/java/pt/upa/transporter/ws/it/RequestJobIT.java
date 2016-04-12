package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;

public class RequestJobIT extends BaseTransporterIT{

	//TODO: correct exception
	@Test(expected = Exception.class)
	public void testRequestJobNullOrigin() throws Exception{
		
	}
	
	//TODO: correct exception
	@Test(expected = Exception.class)
	public void testRequestJobNullDestination() throws Exception{
		
	}
	
	//TODO: correct exception
	@Test(expected = Exception.class)
	public void testRequestJobNullPrice() throws Exception{
		
	}
	
	@Test(expected = BadLocationFault_Exception.class)
	public void testRequestJobUnknownOriginLocation() throws Exception{
		client.requestJob(UNKNOWN_LOCATION, VALID_LOCATION, VALID_PRICE);
	}
	
	@Test(expected = BadLocationFault_Exception.class)
	public void testRequestJobUnknownDestinationLocation() throws Exception{
		client.requestJob(VALID_LOCATION, UNKNOWN_LOCATION, VALID_PRICE);
	}
	
	@Test(expected = BadPriceFault_Exception.class) 
	public void testRequestJobNegativePrice() throws Exception{
		client.requestJob(VALID_LOCATION, VALID_LOCATION, NEGATIVE_PRICE);
	}
	
	@Test
	public void testRequestJobHighPrice() throws Exception{
		assertEquals(null, client.requestJob(VALID_LOCATION, VALID_LOCATION, HIGH_PRICE));
	}
	
	//TODO: CHECK SITUATION OF PAIR AND NOT PAIR
	/*
	@Test
	public void testRequestJobNotWorkingSouth() throws Exception{
		assertEquals(null, client.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE));
	}
	
	@Test
	public void testRequestJobNotWorkingNorth() throws Exception{
		assertEquals(null, client.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE));
	}
	
	@Test
	public void testRequestJobPairNamePairPricePair() throws Exception{
	
	}
	
	@Test
	public void testRequestJobPairNamePairPriceNotPair() throws Exception(){
	
	}
	
	@Test
	public void testRequestJobPairNameNotPairPricePair() throws Exception{
	
	}
	
	@Test
	public void testRequestJobPairNameNotPairPriceNotPair() throws Exception(){
	
	}
	
	*/
	
	@Test
	public void testRequestCorrectJobCreation() throws Exception{
		client.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		assertEquals(1, client.listJobs().size());
	}
}
