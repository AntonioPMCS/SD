package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;

public class DecideJobIT extends AbstractIT{

	@Test(expected = BadJobFault_Exception.class)
	public void testDecideJobUnknownID() throws Exception{
		evenClient.decideJob(UNKNOWN_JOB_ID, true);
	}
	
	@Test(expected = BadJobFault_Exception.class)
	public void testDecideJobNullID() throws Exception{
		evenClient.decideJob(null, true);
	}
	
	@Test
	public void testAcceptJob() throws Exception{
		JobView job = evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		evenClient.decideJob(job.getJobIdentifier(), true);
		assertEquals(JobStateView.ACCEPTED, evenClient.jobStatus(job.getJobIdentifier()).getJobState());
	}
	
	@Test
	public void testRejectJob() throws Exception{
		JobView job = evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		evenClient.decideJob(job.getJobIdentifier(), false);
		assertEquals(JobStateView.REJECTED, evenClient.jobStatus(job.getJobIdentifier()).getJobState());
	}
}
