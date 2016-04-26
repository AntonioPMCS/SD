package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import java.util.List;


import org.junit.Test;

public class ListJobsIT extends BaseTransporterIT{
	
	@Test
	public void testListJobsNoJobs(){
		evenClient.clearJobs();
		assertEquals(true, evenClient.listJobs().size() == 0);
	}
	
	@Test
	public void testListJobsReturnsJobs() throws Exception{
		evenClient.clearJobs();
		evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		assertEquals(List.class.getComponentType(), evenClient.listJobs().getClass().getComponentType());
	}
}
