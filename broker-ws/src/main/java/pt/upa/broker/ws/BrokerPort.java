package pt.upa.broker.ws;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;
import pt.upa.broker.ws.cli.BrokerClient;
import pt.upa.broker.ws.cli.BrokerClientException;
import pt.upa.naming.EndpointManager;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_1.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)

public class BrokerPort implements BrokerPortType{
	private String name;
	private List<TransportView> transports = new ArrayList<TransportView>();
	private EndpointManager endpointManager;
	private ArrayList<TransporterClient> transporterClients = new ArrayList<TransporterClient>();
	private boolean principal = false;
	private BrokerClient secondaryBroker = null;
	private static final int FIVE_SECONDS = 5000;
	private Timer pingBroker = new Timer();
	private Timer checkChange = new Timer();
	private boolean goPrimary = false;
	private boolean firstTime;
	
	
	//Secondary constructor
	public BrokerPort(String name, EndpointManager endpointManager) throws JAXRException{
		this.name=name;
		this.endpointManager = endpointManager;
		principal = false;
		firstTime = true;
	}
	
	//Primary constructor
	public BrokerPort(String name, EndpointManager endpointManager, String url) throws JAXRException{
		this.name=name;
		this.endpointManager = endpointManager;
		principal = true;
		
		try {
			secondaryBroker = new BrokerClient(url);
		} catch (BrokerClientException e) {
			e.printStackTrace();
		}
		
		//Sets thread launching on timer
		pingBroker.scheduleAtFixedRate(new TimerTask(){
		      					public void run() { secondaryBroker.sendAlive();}},
										FIVE_SECONDS, FIVE_SECONDS);
	}
	
	//================================== Fault tolerance methods
	@Override
	public void updateBroker(TransportView transports) {
		this.transports.add(transports);
	}

	@Override
	public void sendAlive() {
		if(!principal){
			if(firstTime){
				//Sets thread launching on timer
				checkChange.scheduleAtFixedRate(new TimerTask(){
				      					public void run() { checkGoPrimary();}},
												FIVE_SECONDS, FIVE_SECONDS);
			}
			firstTime = false;

			System.out.println("Secondary UpaBroker received message from primary UpaBroker");
			goPrimary = false;
		}
	}

	
	//if goPrimary true launches method do go primary server
	//if not puts goPrimary true, in the meanwhile
	//it is expected the primary broker to put this goPrimary false when he makes the ping.
	public void checkGoPrimary(){
		if(goPrimary)
			goPrimary();
		else
			goPrimary = true;
	}
	
	/*
	 * Method to put secondary UpaBroker in primary mode
	 */
	public void goPrimary(){
		checkChange.cancel();
		System.out.println("I just took over, i'm the primary!");
	}
	
	//================================== Domain Methods
	public String ping (String word) {
		String comeBack = name + " received from the transporters: " + '\n';
		for(TransporterClient transporter : transporterClients){
			comeBack+= transporter.ping(word) + '\n';
		}
		return comeBack;
	}
	
	public ArrayList<TransporterClient> getTransporters(){
		return transporterClients;
	}

	public String requestTransport (String origin, String destination, int price)
		throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
	
		
		TransportView chosenRequest = null;
		try{
			
			//CREATE TRANSPORT REQUESTS
			ArrayList<TransportView> requests = new ArrayList<TransportView>();
			
			ArrayList<JobView> tempJobs = new ArrayList<JobView>();
			ArrayList<TransporterClient> validTransporters = new ArrayList<TransporterClient>();
			
			//SEND REQUEST TO ALL TRANSPORTERS
			for(TransporterClient transporter : transporterClients){
				JobView newJob = transporter.requestJob(origin, destination, price);
				if(newJob != null){
					tempJobs.add(newJob);
					validTransporters.add(transporter);
					TransportView request = getTransportView(origin, destination, price, newJob.getCompanyName(), newJob.getJobIdentifier());
					requests.add(request);
				}
			}
			
			//IF only null responses
			if(tempJobs.size() == 0){
				if(price > 100)
					throw new UnavailableTransportFault_Exception("ERROR: Price request too high.", new UnavailableTransportFault());
				else 
					throw new UnavailableTransportFault_Exception("ERROR: Transporters don't operate on request areas.", new UnavailableTransportFault());
			}
			
			
			
			//Find cheapeast offer
			int maxPrice = price;
			JobView chosenTransporter = new JobView();
			for(JobView job: tempJobs){
				if(job.getJobPrice() < maxPrice){
					maxPrice = job.getJobPrice();
					chosenTransporter = job;
				}
			}
			
			//Defines budgeted
			for(TransportView temp : transports){
				temp.setState(TransportStateView.BUDGETED);
			}
			
			
			if(maxPrice == price){
				for(TransportView request : requests){
					request.setState(TransportStateView.FAILED);
				}
				throw new UnavailableTransportPriceFault_Exception("ERROR: No offers lower than requested", new UnavailableTransportPriceFault());
			}
			else{
				chosenRequest = chosenTransportView(chosenTransporter, requests);
				chosenRequest.setState(TransportStateView.BOOKED);
				chosenRequest.setPrice(chosenTransporter.getJobPrice());
				transports.add(chosenRequest);
				
				//Contact Transporters, accepting or denying
				for(TransporterClient transporter : validTransporters){
					JobView receivingJob;
					
					//Gets Job returned from decideJob()
					if(transporter.getWsName().equals(chosenRequest.getTransporterCompany()))
						receivingJob = transporter.decideJob(chosenRequest.getId(), true);
					else{
						TransportView request = getTransportViewByCompany(transporter.getWsName(), requests);
						receivingJob = transporter.decideJob(request.getId(), false);
						request.setState(TransportStateView.FAILED);
					}
					//Checks nullity
					if(receivingJob == null)
						throw new UnavailableTransportFault_Exception("ERROR: Transporter Service doesn't know ID when accepting or rejecting job", new UnavailableTransportFault());
				}
			}
			
		}catch(BadLocationFault_Exception e){
			throw new UnknownLocationFault_Exception("ERROR: Unknown locations given: "+e.getMessage(), new UnknownLocationFault());
		}catch(BadPriceFault_Exception e){
			throw new InvalidPriceFault_Exception("ERROR: Price give can't be lower than zero.", new InvalidPriceFault());
		}catch(BadJobFault_Exception e){
			throw new UnavailableTransportFault_Exception("ERROR: Transporter Service doesn't know a given transport ID "+e.getMessage(), new UnavailableTransportFault());
		} 
		
		//update secondaryBroker
		//if(secondaryBroker != null)
			//secondaryBroker.updateBroker(chosenRequest);
		
		return chosenRequest.getId();
	}

	public TransportView viewTransport (String id) throws UnknownTransportFault_Exception {
		for(TransportView transport : transports){
			if(transport.getId().equals(id)){
				for(TransporterClient temp : transporterClients){
					if(transport.getTransporterCompany().equals(temp.getWsName())){
						JobView job = temp.jobStatus(id);
						if(job.getJobState() == JobStateView.HEADING)
							transport.setState(TransportStateView.HEADING);
						else if(job.getJobState() == JobStateView.ONGOING)
							transport.setState(TransportStateView.ONGOING);
						else if(job.getJobState() == JobStateView.COMPLETED)
							transport.setState(TransportStateView.COMPLETED);
					}
				}
				return transport;
			}
		}
		throw new UnknownTransportFault_Exception("ERROR: Transport with id: "+id+" was not found.", new UnknownTransportFault());
	}

	public List<TransportView> listTransports() {
		return transports;
	}

	public void clearTransports() {
		transports.clear();
		
		//update secondaryBroker
		if(secondaryBroker != null)
			secondaryBroker.clearTransports();
	}
	
	/**
	 * Created a new transport instance, with incrementing id.
	 * 
	 * @param origin 
	 * @param destination
	 * @param price
	 * @return transport TransportView created
	 */
	public TransportView getTransportView(String origin, String destination, int price, String transporterName, String id){
		TransportView transport = new TransportView();
		transport.setTransporterCompany(transporterName);
		transport.setDestination(destination);
		transport.setId(id);
		transport.setOrigin(origin);
		transport.setPrice(price);
		transport.setState(TransportStateView.REQUESTED);
		return transport;
	}
	
	/**
	 * Returns a TransportView for a specific transporter
	 * 
	 * @param transporter The specific transporter name 
	 * @param 
	 * @return transport TransportView created
	 */
	public TransportView getTransportViewByCompany(String transporterName, ArrayList<TransportView> tempTransports) throws UnavailableTransportFault_Exception{
		for(TransportView tempTransport : tempTransports){
			if(transporterName.equals(tempTransport.getTransporterCompany())){
				return tempTransport;
			}
		}
		
		throw new UnavailableTransportFault_Exception("ERROR: Couldn't find a transport ID for given TransportView List", new UnavailableTransportFault());
	}
	
	/**
	 * Makes a search for TransporterServices available
	 */
	public void lookUpTransporterServices() throws JAXRException{
		boolean dontAdd = false;
		try{
			UDDIRecord r1 = endpointManager.getUddiNaming().lookupRecord("UpaTransporter1");
			TransporterClient tc = new TransporterClient(r1.getUrl(), r1.getOrgName());
			try{
				tc.ping("teste");
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println(r1.getOrgName() + "not operational");
				dontAdd = true;
			}
			if(!dontAdd){
				transporterClients.add(tc);
				System.out.println("Adding transporter "+r1.getOrgName() + " to available transporter services.");
			}
			
			dontAdd = false;
			UDDIRecord r2 = endpointManager.getUddiNaming().lookupRecord("UpaTransporter2");
			
			tc = new TransporterClient(r2.getUrl(), r2.getOrgName());
			try{
				tc.ping("teste");
			}
			catch(Exception e){
				System.out.println(r2.getOrgName() + "not operational");
				dontAdd = true;
			}
			if(!dontAdd){
				System.out.println("Adding transporter "+r2.getOrgName() + " to available transporter services.");
				transporterClients.add(tc);
			}
		}catch(TransporterClientException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Given a job, returns the corresponding TransportView belonging to TransportViewList
	 * 
	 * @param job The JobView given
	 * @param requests The TransportView list in which to search
	 * @return TransportView THe transportView found
	 */
	public TransportView chosenTransportView(JobView job, ArrayList<TransportView> requests){
		for(TransportView request: requests){
			if(request.getId().equals(job.getJobIdentifier()))
					return request;
		}
		return null;
	}
}	
