package pt.upa.broker.ws;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;

import pt.upa.broker.BrokerEndpointManager;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)

public class BrokerPort implements BrokerPortType{

	private String name;
	private List<TransportView> transports = new ArrayList<TransportView>();
	private BrokerEndpointManager endpointManager;
	private ArrayList<String> endpoints = new ArrayList<String>();
	private ArrayList<TransporterClient> transporterClients = new ArrayList<TransporterClient>();
	
	public BrokerPort(String name, BrokerEndpointManager endpointManager) throws JAXRException{
		this.name=name;
		this.endpointManager = endpointManager;
		
	}
	
	public String ping (String name) {
		for(TransporterClient transporter : transporterClients){
			return transporter.ping(name);

		}
		return "Unavailable to ping";
	}

	public String requestTransport (String origin, String destination, int price)
		throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
	
		
		TransportView transport = new TransportView();
		try{
			lookUpTransporterServices();
			
			//CREATE TRANSPORT REQUEST
			transport = getTransportView(origin, destination, price, null, null);
			
			ArrayList<JobView> tempJobs = new ArrayList<JobView>();
			
			//SEND REQUEST TO ALL TRANSPORTERS
			for(TransporterClient transporter : transporterClients){
				JobView newJob = transporter.requestJob(origin, destination, price);
				if(newJob != null)
					tempJobs.add(newJob);
			}
			
			//IF only null responses
			if(tempJobs.size() == 0){
				if(price > 100)
					throw new UnavailableTransportPriceFault_Exception("ERROR: Price request too high.", new UnavailableTransportPriceFault());
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
			
			//Defines trip
			transport.setId(chosenTransporter.getJobIdentifier());
			transport.setState(TransportStateView.BUDGETED);
			
			if(maxPrice == price){
				for(TransporterClient transporter : transporterClients){
					transporter.decideJob(transport.getId(), false);
					
				}
				transport.setState(TransportStateView.FAILED);
				throw new UnavailableTransportPriceFault_Exception("ERROR: No offers lower than requested", new UnavailableTransportPriceFault());
			}
			else{
				transport.setPrice(chosenTransporter.getJobPrice());
				
				//Contact Transporters, accepting or denying
				for(TransporterClient transporter : transporterClients){
					JobView receivingJob;
					
					//Gets Job returned from decideJob()
					if(transporter.getWsName() == transport.getId())
						receivingJob = transporter.decideJob(transport.getId(), true);
		
					else
						receivingJob = transporter.decideJob(transport.getId(), false);
					
					//Checks nullity
					if(receivingJob == null)
						throw new UnavailableTransportFault_Exception("ERROR: Transporter Service doesn't know ID when accepting or rejecting job", new UnavailableTransportFault());
					
					transport.setState(TransportStateView.BOOKED);
				}
			}
			
		}catch(BadLocationFault_Exception e){
			throw new UnknownLocationFault_Exception("ERROR: Unknown locations given.", new UnknownLocationFault());
		}catch(BadPriceFault_Exception e){
			throw new InvalidPriceFault_Exception("ERROR: Price give can't be lower than zero.", new InvalidPriceFault());
		}catch(BadJobFault_Exception e){
			throw new UnavailableTransportFault_Exception("ERROR: Transporter Service doesn't know a given transport ID", new UnavailableTransportFault());
		} catch (JAXRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return "#-> Transport service successfully requested. Your transport has the following id: "+transport.getId();
	}

	public TransportView viewTransport (String id) throws UnknownTransportFault_Exception {
		for(TransportView transport : transports){
			if(transport.getId() == id){
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
			if(transporterName == tempTransport.getTransporterCompany()){
				return tempTransport;
			}
		}
		
		throw new UnavailableTransportFault_Exception("ERROR: Couldn't find a transport ID for given TransportView List", new UnavailableTransportFault());
	}
	
	/**
	 * Makes a search for TransporterServices available
	 */
	public void lookUpTransporterServices() throws JAXRException{
		
		for(String endpoint : endpointManager.getUddiNaming().list("UpaTransporter%")){
			endpoints.add(endpoint);
		
			TransporterClient tc;
			try {
				tc = new TransporterClient(endpoint);
				transporterClients.add(tc);
			} catch (TransporterClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}	
