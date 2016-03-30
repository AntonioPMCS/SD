package pt.upa.broker.ws;

import javax.jws.WebService;

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

	public String ping (String name) {
		return "Method ping() was called with name parameter: "+name;
	}

	public String requestTransport (String origin, String destination, int price)
		throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {

		return "Method requestTransport() was called with origin: "+origin+" destination: "+destination+"and price: "+price;
	}

	public TransportView viewTransport (String id) throws UnknownTransportFault_Exception {
		TransportView transportView = new TransportView();
		transportView.setId("Method viewTransport() was called with id: "+id);
		return transportView;
	}

	public List<TransportView> listTransports() {
		ListTransportsResponse listTransports = new ListTransportsResponse();
		return listTransports.getReturn();
	}

	public void clearTransports() {} // TODO

}	
