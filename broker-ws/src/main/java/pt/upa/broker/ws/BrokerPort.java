package pt.upa.broker.ws;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import pt.upa.broker.BrokerEndpointManager;
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
			for(TransporterClient transporter : transporterClients){
				JobView newJob = transporter.requestJob(origin, destination, price);
				if(newJob == null && price > 100)
					throw new UnavailableTransportFault_Exception("ERROR: Transporter doesn't operate on request areas", new UnavailableTransportFault());
				else if(newJob == null)
					throw new UnavailableTransportPriceFault_Exception("ERROR: Price request too high.", new UnavailableTransportPriceFault());
				
				//TODO: Criar lista temporário onde recebes todos os job's
				// No final do for, percorrer a lista temporária e escolhes o com o preço mais baixo
				// criando um TransportView e fazendo decide job true para essa transportadoa e negativo para as outras.
				
				//AS transportadores q recebem negativo, devem cancelar esses jobs..
			}
			
		}catch(BadLocationFault_Exception e){
			throw new UnknownLocationFault_Exception("ERROR: Unknown locations given.", new UnknownLocationFault());
		}catch(BadPriceFault_Exception e){
			throw new InvalidPriceFault_Exception("ERROR: Price give can't be lower than zero.", new InvalidPriceFault());
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

}	
