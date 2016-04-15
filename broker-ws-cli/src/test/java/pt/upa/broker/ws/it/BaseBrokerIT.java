package pt.upa.broker.ws.it;


import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import pt.upa.broker.ws.cli.BrokerClient;
import pt.upa.transporter.ws.cli.TransporterClient;

/**
 * Super class for integration test suites
 * 
 * Loads test properties from configuration file
 */
public class BaseBrokerIT {
	
	protected static TransporterClient oddClient = null;
	protected static TransporterClient evenClient = null;
	protected static BrokerClient brokerClient = null;
	protected static final String VALID_LOCATION = "Lisboa";
	protected static final String INVALID_LOCATION = "Tavira";
	protected static final String SOUTH_LOCATION = "Faro";
	protected static final int VALID_EVEN_PRICE = 50;
	protected static final int VALID_ODD_PRICE = 51;
	protected static final int HIGH_PRICE = 101;
	protected static final int NEGATIVE_PRICE = -1;
	protected static final String UNKNOWN_ID = "teste";
	
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		
		evenClient = new TransporterClient("http://localhost:9090","UpaTransporter2");
		evenClient.setVerbose(true);
		
		oddClient = new TransporterClient("http://localhost:9090","UpaTransporter1");
		oddClient.setVerbose(true);
		
		brokerClient = new BrokerClient("http://localhost:8085/broker-ws/endpoint");
	}

	@AfterClass
	public static void cleanup() {
		evenClient = null;
		oddClient = null;
		brokerClient = null;
	}
	
	@Before
    public void setUp() {
    	evenClient.clearJobs();
    	if(oddClient != null)
    		oddClient.clearJobs();
    	brokerClient.clearTransports();
    }
}
