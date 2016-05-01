package pt.upa.broker.ws;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;

import pt.upa.naming.EndpointManager;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
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
	public boolean alreadyCleanedFolder = false;
	private String name;
	private List<TransportView> transports = new ArrayList<TransportView>();
	private EndpointManager endpointManager;
	private ArrayList<String> endpoints = new ArrayList<String>();
	private ArrayList<TransporterClient> transporterClients = new ArrayList<TransporterClient>();
	
	public BrokerPort(String name, EndpointManager endpointManager) throws JAXRException{
		this.name=name;
		this.endpointManager = endpointManager;
		try {
			generateRSAKeys();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String ping (String word) {
		String comeBack = name + "received from the transporters: " + '\n';
		for(TransporterClient transporter : transporterClients){
			comeBack+= transporter.ping(word) + '\n';
		}
		return comeBack;
	}

	public String requestTransport (String origin, String destination, int price)
		throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
	
		TransportView chosenRequest = null;
		try{
			
			//CREATE TRANSPORT REQUESTS
			ArrayList<TransportView> requests = new ArrayList<TransportView>();
			
			ArrayList<JobView> tempJobs = new ArrayList<JobView>();
			ArrayList<TransporterClient> validTransporters = new ArrayList<TransporterClient>();
			
			//SEND REQUEST TO ALL TRANSPORTERS
			for(TransporterClient transporter : transporterClients){
				JobView newJob = transporter.requestJob(origin, destination, price);
				if(newJob != null){
					tempJobs.add(newJob);
					validTransporters.add(transporter);
					TransportView request = getTransportView(origin, destination, price, newJob.getCompanyName(), newJob.getJobIdentifier());
					requests.add(request);
				}
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
			
			//Defines budgeted
			for(TransportView temp : transports){
				temp.setState(TransportStateView.BUDGETED);
			}
			
			
			if(maxPrice == price){
				for(TransportView request : requests){
					request.setState(TransportStateView.FAILED);
				}
				throw new UnavailableTransportPriceFault_Exception("ERROR: No offers lower than requested", new UnavailableTransportPriceFault());
			}
			else{
				chosenRequest = chosenTransportView(chosenTransporter, requests);
				chosenRequest.setState(TransportStateView.BOOKED);
				transports.add(chosenRequest);
				
				//Contact Transporters, accepting or denying
				for(TransporterClient transporter : validTransporters){
					JobView receivingJob;
					
					//Gets Job returned from decideJob()
					if(transporter.getWsName().equals(chosenRequest.getTransporterCompany()))
						receivingJob = transporter.decideJob(chosenRequest.getId(), true);
					else{
						TransportView request = getTransportViewByCompany(transporter.getWsName(), requests);
						receivingJob = transporter.decideJob(request.getId(), false);
						request.setState(TransportStateView.FAILED);
					}
					//Checks nullity
					if(receivingJob == null)
						throw new UnavailableTransportFault_Exception("ERROR: Transporter Service doesn't know ID when accepting or rejecting job", new UnavailableTransportFault());
				}
			}
			
		}catch(BadLocationFault_Exception e){
			throw new UnknownLocationFault_Exception("ERROR: Unknown locations given: "+e.getMessage(), new UnknownLocationFault());
		}catch(BadPriceFault_Exception e){
			throw new InvalidPriceFault_Exception("ERROR: Price give can't be lower than zero.", new InvalidPriceFault());
		}catch(BadJobFault_Exception e){
			throw new UnavailableTransportFault_Exception("ERROR: Transporter Service doesn't know a given transport ID "+e.getMessage(), new UnavailableTransportFault());
		} 
	
		return "#-> Transport service successfully requested. Your transport has the following id: "+chosenRequest.getId();
	}

	public TransportView viewTransport (String id) throws UnknownTransportFault_Exception {
		for(TransportView transport : transports){
			if(transport.getId().equals(id)){
				for(TransporterClient temp : transporterClients){
					if(transport.getTransporterCompany().equals(temp.getWsName())){
						JobView job = temp.jobStatus(id);
						if(job.getJobState() == JobStateView.HEADING)
							transport.setState(TransportStateView.HEADING);
						else if(job.getJobState() == JobStateView.ONGOING)
							transport.setState(TransportStateView.ONGOING);
						else if(job.getJobState() == JobStateView.COMPLETED)
							transport.setState(TransportStateView.COMPLETED);
					}
				}
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
			if(transporterName.equals(tempTransport.getTransporterCompany())){
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
				tc.ping(" ");
			} catch (TransporterClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Given a job, returns the corresponding TransportView belonging to TransportViewList
	 * 
	 * @param job The JobView given
	 * @param requests The TransportView list in which to search
	 * @return TransportView THe transportView found
	 */
	public TransportView chosenTransportView(JobView job, ArrayList<TransportView> requests){
		for(TransportView request: requests){
			if(request.getId().equals(job.getJobIdentifier()))
					return request;
		}
		return null;
	}
	
	/**
	 *  Generates an assymetric key par
	 *  
	 *  @param publicKeyPath
	 *  @param privateKeyPath
	 */
	private void generateRSAKeys() throws Exception {

		// generate RSA key pair
		System.out.println("Generating RSA keys ...");
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair key = keyGen.generateKeyPair();

		System.out.println("Writing public key ...");
		System.out.println(key.getPublic().toString());
		byte[] pubEncoded = key.getPublic().getEncoded();
		writeKey(name, "public", pubEncoded);

		System.out.println("---");
		
		System.out.println("Writing private key ...");
		byte[] privEncoded = key.getPrivate().getEncoded();
		writeKey(name, "private", privEncoded);
	}
	
	/**
	 * Writes a key to a folder named as the service provider -> this.name
	 * 
	 * @param directory
	 * @param type
	 * @param content
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	
	private void writeKey(String directory, String type, byte[] content) throws FileNotFoundException, IOException {
		String directoryName = directory;
		File dir = new File("target/classes/"+directoryName);
		
		if (!dir.exists()) {
		    System.out.println("creating directory: " + directoryName);
		    try{ dir.mkdir();} 
		    catch(SecurityException se){
		        //handle it
		    }    
		}
		else if(!alreadyCleanedFolder){
			for(File file: dir.listFiles()){
				file.delete();
				alreadyCleanedFolder = true;
			}
		}
		
		File actualFile = new File (dir, type);
		
		FileOutputStream fos = new FileOutputStream(actualFile);
		fos.write(content);
		fos.close();
	}
	
	/**
	 * Returns the public key existent in the given directory
	 * 
	 * @param publicKeyPath
	 * @return pub The public key stored in the given path
	 * @throws Exception
	 */
	public PublicKey getPublicKeyFromDirectory(String directory) throws Exception {

		byte[] pubEncoded = readKeyFromDir(directory);

		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
		KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
		PublicKey pub = keyFacPub.generatePublic(pubSpec);
		System.out.println(pub);
		
		return pub;
	}
	
	/**
	 * Extracts the key in the given directory
	 * 
	 * @param directory
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	
	private byte[] readKeyFromDir(String directory) throws FileNotFoundException, IOException {
		File dir = new File("target/classes/"+directory);
		File keyFile = new File (dir, "public");
		FileInputStream fis = new FileInputStream(keyFile);
		byte[] content = new byte[fis.available()];
		fis.read(content);
		fis.close();
		return content;
	}
	
}	
