package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClearJobsIT extends BaseTransporterIT{

	@Test
	public void testJobDeletion() throws Exception{
		evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		evenClient.clearJobs();
		assertEquals(true, evenClient.listJobs().size() == 0);
	}
}
