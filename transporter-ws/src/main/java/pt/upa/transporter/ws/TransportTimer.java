package pt.upa.transporter.ws;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class TransportTimer extends TimerTask{
	private static final int ONE_SECOND = 1000;
	private static final int FIVE_SECONDS = 5000;
	private static final int HEADING = 1;
	private static final int ONGOING = 2;
	private static final int COMPLETED = 3;
	
	private int operationNr = 0;
	private String transportId = "";
	private TransporterPort transporter;
	
	public TransportTimer(String transportId, int operationNr, TransporterPort transporter){
		this.operationNr = operationNr;
		this.transportId = transportId;
		this.transporter = transporter;
	}
	
	@Override
	public void run() {
		switch(operationNr){
			case 1 : changeJobState(HEADING);
					 break;
			case 2 : changeJobState(ONGOING);
			         break;
			case 3 : changeJobState(COMPLETED);
					 break;
		}
	}
	
	/**
	 * Changes Job Status to HEADING
	 * 
	 * Launches a new instance of TransportTimer with same
	 * transportId, another operationNr, and same transporter
	 * 
	 * @param newState	Name of the new State to change to
	 */
	public void changeJobState(int operationNr){
		JobView job = transporter.jobStatus(transportId);
		int delay = ThreadLocalRandom.current().nextInt(ONE_SECOND, FIVE_SECONDS + 1);
		Timer timer = new Timer();
		
		switch(operationNr){
			case 1 : job.setJobState(JobStateView.HEADING);
					 timer.schedule(new TransportTimer(transportId, ONGOING, transporter), delay);
					 break;
			case 2 : job.setJobState(JobStateView.ONGOING);
					 timer.schedule(new TransportTimer(transportId, COMPLETED, transporter), delay);
					 break;
			case 3 : job.setJobState(JobStateView.COMPLETED);
			         break;
		}
	}
}
