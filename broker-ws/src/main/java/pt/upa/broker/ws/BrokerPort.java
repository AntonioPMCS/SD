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
	private Collection endpoints;
	private ArrayList<TransporterClient> transporterClients = new ArrayList<TransporterClient>();
	
	public BrokerPort(String name, BrokerEndpointManager endpointManager) throws JAXRException{
		this.name=name;
		this.endpointManager = endpointManager;
		endpoints = this.endpointManager.getUddiNaming().list("UpaTransporter%");
		
		Iterator itr = endpoints.iterator();
		
		//TODO: ask where this should be, in endpoint manager, here?
	    while (itr.hasNext()) {
	    	
	        String transporterEndpoint = (String) itr.next();
	        TransporterClient tc;
			try {
				tc = new TransporterClient(transporterEndpoint);
				transporterClients.add(tc);
			} catch (TransporterClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    }
	}
	
	public String ping (String name) {
		/*for(TransporterClient transporter : transporterClients){
			transporter.ping(name);
		}*/
		return name;
	}

	public String requestTransport (String origin, String destination, int price)
		throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
	
		
		try{
			ArrayList<TransportView> tempTransports = new ArrayList<TransportView>();
			
			for(TransporterClient transporter : transporterClients){
				
				//CREATE REQUESTS
				TransportView transport = getTransportView(origin, destination, price, transporter.getWsName(), null);
				tempTransports.add(transport);
				
				
				//SEND REQUEST
				JobView newJob = transporter.requestJob(origin, destination, price);
				
				if(newJob == null && price > 100)
					throw new UnavailableTransportPriceFault_Exception("ERROR: Price request too high.", new UnavailableTransportPriceFault());
				else if(newJob == null)
					throw new UnavailableTransportFault_Exception("ERROR: Transporter doesn't operate on request areas.", new UnavailableTransportFault());
				
				//GETS BUDGET
				transport.setPrice(newJob.getJobPrice());
				transport.setId(newJob.getCompanyName()); 
				transport.setState(TransportStateView.BUDGETED);
			}
			
			//Find cheapest Transporter
			int chosenPrice = 100;
			TransportView chosenOne = new TransportView();
			for(TransportView transport: tempTransports){
				if(transport.getPrice() < chosenPrice){
					chosenPrice = transport.getPrice();
					chosenOne = transport;
				}
			}
			
			//Contact Transporters, accepting or denying
			for(TransporterClient transporter : transporterClients){
				JobView receivingJob;
				boolean chosen = false;
				String chosenOneID = chosenOne.getId();
				TransportView transportView = getTransportViewByCompany(transporter.getWsName(), tempTransports);
				
				if(transporter.getWsName() == chosenOneID){
					receivingJob = transporter.decideJob(chosenOneID, true);
					chosen = true;
				}
				
				else{
					receivingJob = transporter.decideJob(transportView.getId(), false);
				}
				
				if(receivingJob != null){
					transportView.setState(chosen ? TransportStateView.BOOKED : TransportStateView.FAILED);
				}
				else
					throw new UnavailableTransportFault_Exception("ERROR: Transporter Service doesn't know ID when accepting or rejecting job", new UnavailableTransportFault());
			}
			
			
			
			
		}catch(BadLocationFault_Exception e){
			throw new UnknownLocationFault_Exception("ERROR: Unknown locations given.", new UnknownLocationFault());
		}catch(BadPriceFault_Exception e){
			throw new InvalidPriceFault_Exception("ERROR: Price give can't be lower than zero.", new InvalidPriceFault());
		}catch(BadJobFault_Exception e){
			throw new UnavailableTransportFault_Exception("ERROR: Transporter Service doesn't know a given transport ID", new UnavailableTransportFault());
		}
		
		

		return "Method requestTransport() was called with origin: "+origin+" destination: "+destination+"and price: "+price;
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
	
	
}	
