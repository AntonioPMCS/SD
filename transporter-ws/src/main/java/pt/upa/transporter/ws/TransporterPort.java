package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.jws.WebService;


@WebService(
	    endpointInterface="pt.upa.transporter.ws.TransporterPortType", //Java interface with invocable methods declaration
	    wsdlLocation="transporter.1_0.wsdl",
	    name="TransporterWebService",
	    portName="TransporterPort",
	    targetNamespace="http://ws.transporter.upa.pt/",
	    serviceName="TransporterService"
	)

public class TransporterPort implements TransporterPortType{
	private static final int ONE_SECOND = 1000;
	private static final int FIVE_SECONDS = 5000;
	private static final int CHANGE_TO_HEADING = 1;
	private final String name;
	private final boolean parity; //true if even, false otherwise
	private final String[] north = {"Porto","Braga","Viana do Castelo","Vila Real","Bragança"};
	private final String[] center = {"Lisboa","Leiria","Santarem","Castelo Branco","Coimbra","Aveiro","Viseu","Guarda"};
	private final String[] south = {"Setubal", "Évora", "Portalegre", "Beja", "Faro"};
	private Timer timer = new Timer();
	private static int id = 0;
	private ArrayList<JobView> jobs = new ArrayList<JobView>();
	
	
	
	
	public TransporterPort(String name){
		this.name=name;
		int temp = Character.getNumericValue(name.charAt(14));
		
		//Sets parity
		if(temp % 2 == 0){parity = true;}
		else{parity = false;}
	}

	@Override
	public String ping(String message) {
		return name+" responding to ping request...Message given: "+message;
	}

	@Override
	public JobView requestJob(String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception {
		
		//Check if price is negative
		//TODO: Verify BadPriceFault constructor
		if(price < 0){
			throw new BadPriceFault_Exception("Price offered can't be lower than zero!", new BadPriceFault());
		}
		
		//Check if give locations exist
		//TODO: Verify BadLocationFault constructor
		if(!locationExists(origin) || !locationExists(destination)){
			throw new BadLocationFault_Exception("Origin or destination location are invalid!", new BadLocationFault());
		}
		
		//Check situations to be answered with null
		if(price > 100){
			return null;
		}
		
		
		if(!workingZoneLocation(origin) || !workingZoneLocation(destination)){
			return null;
		}
		
		int offerPrice;
		
		//In case everything is correct, define offer price
		if(price == 0 || price == 1)
			offerPrice = 0;
		else if(raiseOffer(price)){
			offerPrice = ThreadLocalRandom.current().nextInt(price, 100 + 1);
			
		}
		else{
			offerPrice = ThreadLocalRandom.current().nextInt(1, price);
		}
		
		//Create job with PROPOSED state
		JobView job = getJobView(origin, destination, offerPrice);
		jobs.add(job);
		
		return job;
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		boolean jobFound = false;
		for(JobView job : jobs){
			if(job.getJobIdentifier().equals(id)){
				jobFound = true;
				if(accept){
					job.setJobState(JobStateView.ACCEPTED);
					int delay = ThreadLocalRandom.current().nextInt(ONE_SECOND, FIVE_SECONDS + 1);
					timer = new Timer();
					timer.schedule(new TransportTimer(id, CHANGE_TO_HEADING, this), delay);
				}
				else{
					job.setJobState(JobStateView.REJECTED);
				}
				return job;
			}
		}
		
		if(!jobFound){
			throw new BadJobFault_Exception("Job with id: #"+id+" doesn't exist!", new BadJobFault());
		}
		return null;
	}

	@Override
	public JobView jobStatus(String id) {
		for(JobView job : jobs){
			if(job.getJobIdentifier().equals(id)){
				return job;
			}
		}
		return null;
	}

	@Override
	public List<JobView> listJobs() {
		return jobs;
	}

	@Override
	public void clearJobs() {
		timer.cancel();
		timer.purge();
		id = 0;
		jobs.clear();
	}
	
	
	/**
	 * Checks if location exists in known locations.
	 * 
	 * @param location Location provided by client.
	 * @return boolean True if location exists, false otherwise
	 */
	public boolean locationExists(String location){
		return Arrays.asList(center).contains(location) ||
				Arrays.asList(south).contains(location) ||
				Arrays.asList(north).contains(location);
	}
	
	/**
	 * Verifies if a given location belongs to the working zone
	 * of the transporter.
	 * 
	 * @param location Location provided by the Client.
	 * @return knowsLocation True if location belong to working zone, false otherwise.
	 */
	public boolean workingZoneLocation(String location){
		boolean knowsLocation = Arrays.asList(center).contains(location);
		if(parity && !knowsLocation){
			knowsLocation = Arrays.asList(north).contains(location);
		}
		else if(!parity && !knowsLocation){
			knowsLocation = Arrays.asList(south).contains(location);
		}
		
		return knowsLocation;
	}
	
	/**
	 * Returns true if the offer is to be above the price received 
	 * by the client, and false otherwise.
	 * 
	 * @param price Price received by the client
	 * @return boolean True if offer > price, false otherwise.
	 */
	public boolean raiseOffer(int price){
		boolean raisePrice = false;
		boolean priceIsEven = false;
		
		//Check if price is even
		if(price % 2 == 0){
			priceIsEven = true;
		}
		
		//Decide upon the offer
		//Price is raised when transporter is Even and price not even
		//of vice-versa -> XOR between them!
		if(price > 10 && (parity ^ priceIsEven)){
			raisePrice = true;
		}
		
		return raisePrice;
	}
	
	/**
	 * Created a new job instance, with incrementing id.
	 * 
	 * @param origin 
	 * @param destination
	 * @param price
	 * @return job Job created
	 */
	public JobView getJobView(String origin, String destination, int price){
		id++;
		JobView job = new JobView();
		job.setCompanyName(name);
		job.setJobDestination(destination);
		job.setJobIdentifier(name+String.valueOf(id));
		job.setJobOrigin(origin);
		job.setJobPrice(price);
		job.setJobState(JobStateView.PROPOSED);
		return job;
	}
}
