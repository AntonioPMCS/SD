package pt.upa.broker;

import pt.upa.broker.ws.BrokerPort;
import pt.upa.naming.EndpointManager;

public class BrokerApplication {

	
	public static void main(String[] args) throws Exception {
		System.out.println(BrokerApplication.class.getSimpleName() + " starting...");
		
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + BrokerApplication.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;

		// Create server implementation object, according to options
		EndpointManager endpoint = null;
		BrokerPort port = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new EndpointManager(wsURL);
			//TODO: é suposto adicionar o port certo?
			
		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			endpoint = new EndpointManager(uddiURL, wsName, wsURL);
			endpoint.setVerbose(true);
			port = new BrokerPort(wsName, endpoint);
			endpoint.setPort(port);
			
			
		}

		try {
			
			endpoint.start();
			((BrokerPort) endpoint.getPort()).lookUpTransporterServices();
			System.out.println(((BrokerPort) endpoint.getPort()).ping("hello"));
			System.out.println(((BrokerPort) endpoint.getPort()).ping("hello"));
			System.out.println(((BrokerPort) endpoint.getPort()).ping("hello"));
			endpoint.awaitConnections();
			
		} finally {
			endpoint.stop();
		}

	}

}
