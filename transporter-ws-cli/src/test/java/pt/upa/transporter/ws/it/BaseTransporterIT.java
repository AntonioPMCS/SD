package pt.upa.transporter.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import pt.upa.transporter.ws.cli.TransporterClient;

/**
 * Super class for integration test suites
 * 
 * Loads test properties from configuration file
 */
public class BaseTransporterIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	private static Properties props = null;
	
	protected static TransporterClient oddClient = null;
	protected static TransporterClient evenClient = null;
	protected static final String VALID_LOCATION = "Lisboa";
	protected static final String NORTH_LOCATION = "Porto";
	protected static final String SOUTH_LOCATION = "Faro";
	protected static final String UNKNOWN_LOCATION = "Tavira";
	protected static final String UNKNOWN_JOB_ID = "Unknown";
	protected static final int NEGATIVE_PRICE = -1;
	protected static final int VALID_PRICE = 50;
	protected static final int HIGH_PRICE = 101;
	protected static final int INDEX_ZERO = 0;
	protected static final String EMPTY_STRING = "";
	protected static final String TEST_STRING = "test";
	protected final static int LOWER_THAN_TEN_PRICE = 9;
	protected final static int EVEN_PRICE = 20;
	protected final static int ODD_PRICE = 21;
	
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		
		evenClient = new TransporterClient("http://localhost:9090","UpaTransporter2");
		evenClient.setVerbose(true);
		
		oddClient = new TransporterClient("http://localhost:9090","UpaTransporter1");
		oddClient.setVerbose(true);
	}

	@AfterClass
	public static void cleanup() {
		evenClient = null;
	}
	
	@Before
    public void setUp() {
    	evenClient.clearJobs();
    	oddClient.clearJobs();
    }
}
