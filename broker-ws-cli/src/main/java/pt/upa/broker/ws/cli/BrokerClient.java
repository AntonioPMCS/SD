package pt.upa.broker.ws.cli;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;

public class BrokerClient implements BrokerPortType{

	/** constructor with provided web service URL */
	public BrokerClient(String wsURL) throws BrokerClientException {
		this.wsURL = wsURL;
		createStub();
	}

	/** constructor with provided UDDI location and name */
	public BrokerClient(String uddiURL, String wsName) throws BrokerClientException {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}

	/** UDDI lookup */
	private void uddiLookup() throws BrokerClientException {
		try {
			if (verbose)
				System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			if (verbose)
				System.out.printf("Looking for '%s'%n", wsName);
			wsURL = uddiNaming.lookup(wsName);

		} catch (Exception e) {
			String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
			throw new BrokerClientException(msg, e);
		}

		if (wsURL == null) {
			String msg = String.format("Service with name %s not found on UDDI at %s", wsName, uddiURL);
			throw new BrokerClientException(msg);
		}
	}

	/** Stub creation and configuration */
	private void createStub() {
		if (verbose)
			System.out.println("Creating stub ...");
		service = new BrokerService();
		port = service.getBrokerPort();

		if (wsURL != null) {
			if (verbose)
				System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
			
			setTimeouts(requestContext);
		}
	}
	
	private void setTimeouts(Map<String, Object> requestContext){
		
		int connectionTimeout = 1000;
        // The connection timeout property has different names in different versions of JAX-WS
        // Set them all to avoid compatibility issues
        final List<String> CONN_TIME_PROPS = new ArrayList<String>();
        CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
        CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
        CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
        // Set timeout until a connection is established (unit is milliseconds; 0 means infinite)
        for (String propName : CONN_TIME_PROPS)
            requestContext.put(propName, connectionTimeout);
        System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);

        int receiveTimeout = 2000;
        // The receive timeout property has alternative names
        // Again, set them all to avoid compability issues
        final List<String> RECV_TIME_PROPS = new ArrayList<String>();
        RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
        RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
        RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
        // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
        for (String propName : RECV_TIME_PROPS)
            requestContext.put(propName, 1000);
        System.out.printf("Set receive timeout to %d milliseconds%n", receiveTimeout);
	}

	/** WS service */
	BrokerService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	BrokerPortType port = null;

	/** UDDI server URL */
	private String uddiURL = null;

	/** WS name */
	private String wsName = null;

	/** WS endpoint address */
	private String wsURL = null; // default value is defined inside WSDL

	public String getWsURL() {
		return wsURL;
	}

	public String ping(String name) {
		return port.ping(name);
	}

	public String requestTransport(String origin, String destination, int price) 
		throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		return port.requestTransport(origin, destination, price);
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		return port.viewTransport(id);
	}
	
	public List<TransportView> listTransports() {
		return port.listTransports();
	}

	public void clearTransports() {
		port.clearTransports();
	}


	/** output option **/
	private boolean verbose = false;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}



}
