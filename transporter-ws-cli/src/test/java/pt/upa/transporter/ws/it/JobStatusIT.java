package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;


public class JobStatusIT extends BaseTransporterIT{

	//TODO: definir expepção
	@Test
	public void testJobStatusNullID() throws Exception{
		client.jobStatus(null);
	}
	
	@Test
	public void testJobStatusUnknownID(){
		assertEquals(client.jobStatus(UNKNOWN_JOB_ID), null);
	}
	
	@Test
	public void testJobStatusEmptyString(){
		assertEquals(client.jobStatus(EMPTY_STRING), null);
	}
	
	@Test
	public void testJobStatusStatus() throws Exception{
		client.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		assertEquals(client.getWsName()+"1", client.jobStatus(client.getWsName()+"1").getJobIdentifier());
	}
}
