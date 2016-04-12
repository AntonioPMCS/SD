package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClearJobsIT extends BaseTransporterIT{

	@Test
	public void testJobDeletion() throws Exception{
		client.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		client.clearJobs();
		assertEquals(client.listJobs() == null, true);
	}
}
