	package pt.upa.broker;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;
// classes generated from WSDL
import pt.upa.broker.ws.cli.BrokerClient;;


public class BrokerClientApplication {

	private static final String LIMITER = "######################################################";
	private static final String GREETING = "Welcome to UPA application";
	private static final String QUIT = "type 'quit' to exit...";
	private static final String REQUEST = "type 'request <origin city> <destination city> <max price> to request a transportation";
	private static final String PING = "type 'ping <word> to ping all known transporters";
	private static final String VIEW = "type 'view <#transport id> to verify the status of a transport requested";
	private static final String HELP = "type 'help' to review the commands";
	private static final long TIME_TO_WAIT = 1000;
	public static void showCommands(){
		
		System.out.println(REQUEST);
		System.out.println(VIEW);
		System.out.println(HELP);
		System.out.println(PING);
		System.out.println(QUIT);
	}
	
	public static void setTimeouts(BrokerClient client){
		BindingProvider bindingProvider = (BindingProvider) client.getPort();
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        int connectionTimeout = 1;
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
            requestContext.put(propName, 1);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(BrokerClientApplication.class.getSimpleName() + " starting...");

		// Check arguments
		if (args.length == 0) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + BrokerClientApplication.class.getName() + " wsURL OR uddiURL wsName");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		if (args.length == 1) {
			wsURL = args[0];
		} else if (args.length >= 2) {
			uddiURL = args[0];
			wsName = args[1];
		}
	
		// Create client
		BrokerClient client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new BrokerClient(wsURL);
		} else if (uddiURL != null) {
			System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
			client = new BrokerClient(uddiURL, wsName);
		}
		
		int counter = 0;
		String input = "";
		String order = null;
		System.out.println(LIMITER);
		System.out.println(GREETING);
		showCommands();
		System.out.println(LIMITER);
		boolean quit = false;
		setTimeouts(client);
		boolean tryingAgain = false;
		String previousCommand = null;
		
		while(!quit){
			input = System.console().readLine();
			String[] command = input.split(" ");
			try{
				if(tryingAgain)
					order = previousCommand; 
				else
					order = command[0];
				
				switch(order){
					case "request" : {
						System.out.println(client.requestTransport(command[1], command[2], Integer.parseInt(command[3])));
						break;
					}
					case "view" : {
						System.out.println(client.viewTransport(command[1]).getState());
						break;
					}
					case "ping" : {
						System.out.println(client.ping(command[1]));
						break;
					}
					case "help" : {
						showCommands();
						break;
					}
					case "quit" : {
						quit = true;
						System.out.println("Bye!");
						break;
					}
					default : {
						System.out.println("ERROR: command inserted not known! type help.");
					}
				}
				tryingAgain = false;
			}catch(UnavailableTransportPriceFault_Exception e){
				System.out.println(e.getMessage());
			}catch(UnavailableTransportFault_Exception e){
				System.out.println(e.getMessage());
			}catch(InvalidPriceFault_Exception e){
				System.out.println(e.getMessage());
			}catch(UnknownLocationFault_Exception e){
				System.out.println(e.getMessage());
			}catch(UnknownTransportFault_Exception e){
				System.out.println(e.getMessage());
			}catch(WebServiceException wse) {
                Throwable cause = wse.getCause();
                if (cause != null && cause instanceof SocketTimeoutException) {
                    System.out.println("UpaBroker not responding...");
                    //If third try already, then ask name server for UpaBroker service address
                    if(counter == 3){
                    	counter = 0;
                    	tryingAgain = false;
                    	
                    	String newBrokerEndpoint = null;
                    	
                    	//ask name server
                    	//There is only on service left with same name
                    	for(String endpoint : client.getUDDINaming().list("UpaBroker")){
                    			newBrokerEndpoint = endpoint;
                    	}
                    	client = new BrokerClient(newBrokerEndpoint);
                    }
                    else{
                    	tryingAgain = true;
                    	previousCommand = order;
                    	counter++;
                        System.out.println("Trying again in "+TIME_TO_WAIT+" ms!");
                        Thread.sleep(TIME_TO_WAIT);
                    }
                }
            }
	
		}

	}
}
