package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;


public class JobStatusIT extends BaseTransporterIT{

	@Test
	public void testJobStatusNullID() throws Exception{
		assertEquals(null, evenClient.jobStatus(null));
	}
	
	@Test
	public void testJobStatusUnknownID(){
		assertEquals(null, evenClient.jobStatus(UNKNOWN_JOB_ID));
	}
	
	@Test
	public void testJobStatusEmptyString(){
		assertEquals(null, evenClient.jobStatus(EMPTY_STRING));
	}
	
	@Test
	public void testJobStatusStatus() throws Exception{
		evenClient.clearJobs();
		JobView job = evenClient.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		
		JobView firstJob = evenClient.jobStatus(job.getJobIdentifier());
		
    	assertEquals(JobStateView.PROPOSED, firstJob.getJobState());
    	assertEquals(job.getJobOrigin(), firstJob.getJobOrigin());
    	assertEquals(job.getJobDestination(), firstJob.getJobDestination());
    	assertEquals(job.getCompanyName(), firstJob.getCompanyName());
    	assertEquals(job.getJobIdentifier(), firstJob.getJobIdentifier());
	}
}
