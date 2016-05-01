package pt.upa.transporter.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import pt.upa.transporter.ws.cli.TransporterClient;

/**
 * Integration Test suite abstract class. Test classes inherit this one to
 * better configure and prepare each test.
 */
public class AbstractIT {

	private static final String TEST_PROP_FILE = "/test.properties";

	private static Properties PROPS;
	protected static TransporterClient CLIENT;

	protected static TransporterClient oddClient = null;
	protected static TransporterClient evenClient = null;
	protected static final int PRICE_UPPER_LIMIT = 100;
	protected static final int PRICE_SMALLEST_LIMIT = 10;
	protected static final int UNITARY_PRICE = 1;
	protected static final int ZERO_PRICE = 0;
	protected static final int INVALID_PRICE = -1;
	protected static final String CENTRO_1 = "Lisboa";
	protected static final String SUL_1 = "Beja";
	protected static final String CENTRO_2 = "Coimbra";
	protected static final String SUL_2 = "Portalegre";
	protected static final String EMPTY_STRING = "";
	protected static final int DELAY_LOWER = 1000; // milliseconds
	protected static final int DELAY_UPPER = 5000; // milliseconds
	protected static final String VALID_LOCATION = "Lisboa";
	protected static final String NORTH_LOCATION = "Porto";
	protected static final String SOUTH_LOCATION = "Faro";
	protected static final String UNKNOWN_LOCATION = "Tavira";
	protected static final String UNKNOWN_JOB_ID = "Unknown";
	protected static final int NEGATIVE_PRICE = -1;
	protected static final int VALID_PRICE = 50;
	protected static final int HIGH_PRICE = 101;
	protected static final int INDEX_ZERO = 0;
	protected static final String TEST_STRING = "test";
	protected final static int LOWER_THAN_TEN_PRICE = 9;
	protected final static int EVEN_PRICE = 20;
	protected final static int ODD_PRICE = 21;

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		PROPS = new Properties();
		try {
			PROPS.load(AbstractIT.class.getResourceAsStream(TEST_PROP_FILE));
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}
		String uddiEnabled = PROPS.getProperty("uddi.enabled");
		String uddiURL = PROPS.getProperty("uddi.url");
		String wsName = PROPS.getProperty("ws.name");
		String wsURL = PROPS.getProperty("ws.url");

		// Note: CLIENT is defined to be an odd transporter in the pom file
		// (UpaTransporter1).

		if ("true".equalsIgnoreCase(uddiEnabled)) {
			CLIENT = new TransporterClient(uddiURL, wsName);
			evenClient = new TransporterClient("http://localhost:9090","UpaTransporter2");
			evenClient.setVerbose(true);
			oddClient = new TransporterClient("http://localhost:9090","UpaTransporter1");
			oddClient.setVerbose(true);
		} else {
			CLIENT = new TransporterClient(wsURL);
		}
		CLIENT.setVerbose(true);

	}

	@AfterClass
	public static void cleanup() {
		CLIENT = null;
	}

}
