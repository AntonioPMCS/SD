package pt.upa.transporter;

import pt.upa.handlers.BrokerHandler;
import pt.upa.naming.EndpointManager;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPort;

public class TransporterApplication {

	public static void main(String[] args) throws Exception {
		
		System.out.println(TransporterApplication.class.getSimpleName() + " starting...");
		
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + TransporterApplication.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;

		// Create server implementation object, according to options
		EndpointManager endpoint = null;
		TransporterPort port = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new EndpointManager(wsURL);
		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			endpoint = new EndpointManager(uddiURL, wsName, wsURL);
			endpoint.setVerbose(true);
			port = new TransporterPort(wsName);
			endpoint.setPort(port);
			
		}

		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
