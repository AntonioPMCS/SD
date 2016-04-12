package pt.upa.transporter.ws.it;

import static org.junit.Assert.*;

import org.junit.Test;

import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobStateView;

public class DecideJobIT extends BaseTransporterIT{

	@Test(expected = BadJobFault_Exception.class)
	public void testDecideJobUnknownID() throws Exception{
		client.decideJob(UNKNOWN_JOB_ID, true);
	}
	
	//TODO: nao é esta a excepção q deve lançar...
	@Test(expected = BadJobFault_Exception.class)
	public void testDecideJobNullID() throws Exception{
		client.decideJob(null, true);
	}
	
	@Test
	public void testAcceptJob() throws Exception{
		client.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		client.decideJob(client.getWsName()+"1", true);
		assertEquals(client.listJobs().get(INDEX_ZERO).getJobState(), JobStateView.ACCEPTED);
	}
	
	@Test
	public void testRejectJob() throws Exception{
		client.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
		client.decideJob(client.getWsName()+"1", false);
		assertEquals(client.listJobs().get(INDEX_ZERO).getJobState(), JobStateView.REJECTED);
	}
}
