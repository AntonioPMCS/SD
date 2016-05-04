package pt.upa.broker.ws;

import org.junit.*;

import com.sun.xml.bind.v2.schemagen.xmlschema.List;

import pt.upa.naming.EndpointManager;

import static org.junit.Assert.*;

import javax.xml.registry.JAXRException;

/**
 *  Unit Test example
 *  
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers 
 */
public class BrokerPortTest {

    // static members
	private static final int HIGH_PRICE = 101;
	private static final int GOOD_PRICE = 50;
	private static final String INDIFFERENT_LOCATION = "Lisboa";
	private static final String UNKNOWN_JOB = "test";
	private static final String BROKER_NAME = "UpaBroker";
	private static final String WS_URL = "http://localhost";
	
    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members
    private BrokerPort port;

    // initialization and clean-up for each test

    @Before
    public void setUp() throws JAXRException {
    	port = new BrokerPort(BROKER_NAME, new EndpointManager(WS_URL));
    }

    @After
    public void tearDown() {
    	port = null;
    }


    // tests
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void testPriceTooHighNoJobs() throws Exception{
    	//since there is no communication with server, no JobViews will be supplied
    	port.requestTransport(INDIFFERENT_LOCATION, INDIFFERENT_LOCATION, HIGH_PRICE);
    }
    
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void testPriceGoodPriceNoJobs() throws Exception{
    	//since there is no communication with server, no JobViews will be supplied
    	port.requestTransport(INDIFFERENT_LOCATION, INDIFFERENT_LOCATION, GOOD_PRICE);
    }
    
    @Test(expected = UnknownTransportFault_Exception.class)
    public void testViewUnknownJob() throws Exception{
    	port.viewTransport(UNKNOWN_JOB);
    }
    
    @Test
    public void testListTransportsEmpty() throws Exception{
    	assertEquals(List.class.getComponentType(),   port.listTransports().getClass().getComponentType());
    }
    
    @Test
    public void testClearJobsCanRun() throws Exception{
    	port.clearTransports();
    }

}