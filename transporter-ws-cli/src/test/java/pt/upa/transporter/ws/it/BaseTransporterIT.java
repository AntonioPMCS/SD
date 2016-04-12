package pt.upa.transporter.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
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
	
	protected static TransporterClient client = null;
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
	
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		props = new Properties();
		try {
			props.load(BaseTransporterIT.class.getResourceAsStream(TEST_PROP_FILE));
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}
		String uddiEnabled = props.getProperty("uddi.enabled");
		
		String uddiURL2 = props.getProperty("uddiPair.url");
		String wsName2 = props.getProperty("wsPair.name");
		String wsURL2 = props.getProperty("wsPair.url");
		
		String uddiURL1 = props.getProperty("uddiNotPair.url");
		String wsName1 = props.getProperty("wsNotPair.name");
		String wsURL1 = props.getProperty("wsNotPair.url");
		

		if ("true".equalsIgnoreCase(uddiEnabled)) {
			client = new TransporterClient(uddiURL1, wsName1);
		} else {
			client = new TransporterClient(wsURL1);
		}
		client.setVerbose(true);

	}

	@AfterClass
	public static void cleanup() {
		client = null;
	}
}
