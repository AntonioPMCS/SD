package pt.upa.ca;

import pt.upa.ca.ws.cli.AuthorityClient;

public class AuthorityClientApplication {
	public static void main(String[] args) throws Exception{
		System.out.println(AuthorityClientApplication.class.getSimpleName() + " starting...");
		
		// Check arguments
		if (args.length == 0) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + AuthorityClientApplication.class.getName() + " wsURL OR uddiURL wsName");
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

		//Launch various clients for services here!!!
		// Create client
		AuthorityClient client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new AuthorityClient(wsURL);
		} else if (uddiURL != null) {
			System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
			client = new AuthorityClient(uddiURL, wsName);
		}
		
		System.out.println(client.ping("HELLO"));
	}
}
