package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import java.util.List;


import org.junit.Test;

public class ListJobsIT extends BaseTransporterIT{
	
	@Test
	public void testListJobsNoJobs(){
		assertEquals(null, client.listJobs());
	}
	
	@Test
	public void testListJobsReturnsJobs() throws Exception{
		client.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		assertEquals(List.class, client.listJobs());
	}
}
