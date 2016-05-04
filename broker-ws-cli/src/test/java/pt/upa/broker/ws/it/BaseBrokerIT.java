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
	protected static final String SOUTH_1 = "Beja";
	protected static final String SOUTH_2 = "Portalegre";
	protected static final String EMPTY_STRING = "";
	protected static final String CENTER_1 = "Lisboa";
	protected static final String CENTER_2 = "Coimbra";
	
	protected static final String NORTH_1 = "Porto";
	protected static final String NORTH_2 = "Braga";
	protected static int PRICE_UPPER_LIMIT = 100;
	protected static int PRICE_SMALLEST_LIMIT = 10;

	protected static int INVALID_PRICE = -1;
	protected static int ZERO_PRICE = 0;
	protected static int UNITARY_PRICE = 1;
	protected static final int DELAY_LOWER = 1000; // = 1 second
	protected static final int DELAY_UPPER = 5000; // = 5 seconds
    protected static final int TENTH_OF_SECOND = 100;

	protected static int ODD_INCREMENT = 1;
	protected static int EVEN_INCREMENT = 2;
	
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		
		evenClient = new TransporterClient("http://localhost:9090","UpaTransporter2");
		evenClient.setVerbose(true);
		
		oddClient = new TransporterClient("http://localhost:9090","UpaTransporter1");
		oddClient.setVerbose(true);
		
		brokerClient = new BrokerClient("http://localhost:8088/broker-ws/endpoint");
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
