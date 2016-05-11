package pt.upa.broker;

import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;

import com.sun.xml.ws.client.ClientTransportException;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;
import pt.upa.broker.ws.cli.BrokerClient;
import pt.upa.broker.ws.cli.BrokerClientException;

public class BrokerClientApplication {

	private static final String LIMITER = "######################################################";
	private static final String GREETING = "Welcome to UPA application";
	private static final String QUIT = "type 'quit' to exit...";
	private static final String REQUEST = "type 'request <origin city> <destination city> <max price>' to request a transportation";
	private static final String PING = "type 'ping <word>' to ping all known transporters";
	private static final String VIEW = "type 'view <#transport id>' to verify the status of a transport requested";
	private static final String LIST = "type 'list' to view all requested transports";
	private static final String HELP = "type 'help' to review the commands";
	public static void showCommands(){
		System.out.println(REQUEST);
		System.out.println(VIEW);
		System.out.println(HELP);
		System.out.println(PING);
		System.out.println(LIST);
		System.out.println(QUIT);
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
		
		
		String input = "";
		System.out.println(LIMITER);
		System.out.println(GREETING);
		showCommands();
		System.out.println(LIMITER);
		boolean quit = false;
		
		//Client cycle
		while(!quit){
			input = System.console().readLine();
			String[] command = input.split(" ");
			try{
				switch(command[0]){
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
					case "list" : {
						System.out.println("------------------------------");
						for(TransportView transport : client.listTransports()){
							
							System.out.println("---> "+transport.getId());
							System.out.println("from : "+transport.getOrigin());
							System.out.println("to : "+transport.getDestination());
							System.out.println("price : "+transport.getPrice());
							System.out.println("state : "+transport.getState());
							System.out.println("company : "+transport.getTransporterCompany());
						}
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
			}
		}
	}
}
