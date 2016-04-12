package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *  Unit Test example
 *  
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers 
 *  
 *  
 *  Unit Test suite
 *  The purpose of this class is to test CalcPort locally.
 */
public class TransporterPortTest {

    // static members
	private final static String PAIR_TRANSPORTER_NAME = "UpaTransporter2";
	private final static String NOTPAIR_TRANSPORTER_NAME = "UpaTransporter1";
	private final static String VALID_LOCATION = "Lisboa";
	private final static String SOUTH_LOCATION = "Faro";
	private final static String NORTH_LOCATION = "Porto";
	private final static String UNKNOWN_LOCATION = "Tavira";
	private final static int VALID_PRICE = 50;
	private final static int HIGH_PRICE = 101;
	private final static int LOWER_THAN_TEN_PRICE = 9;
	private final static int PAIR_PRICE = 20;
	private final static int NOT_PAIR_PRICE = 21;
	private final static int NEGATIVE_PRICE = -1;

    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {

    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members
    private TransporterPort pairPort;
    private TransporterPort notPairPort;

    // initialization and clean-up for each test

    @Before
    public void setUp() {
    	pairPort = new TransporterPort("UpaTransporter2");
    	notPairPort = new TransporterPort("UpaTransporter1");
    }

    @After
    public void tearDown() {
    	pairPort = null;
    	notPairPort = null;
    }


    // tests
    @Test(expected = BadPriceFault_Exception.class)
    public void testNegativePrice() throws Exception {
    		pairPort.requestJob(VALID_LOCATION, VALID_LOCATION, NEGATIVE_PRICE);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testUnknownOrigin() throws Exception{
    	pairPort.requestJob(UNKNOWN_LOCATION, VALID_LOCATION, VALID_PRICE);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testUnknownDestination() throws Exception{
    	pairPort.requestJob(VALID_LOCATION, UNKNOWN_LOCATION, VALID_PRICE);
    }
    
    @Test
    public void testPriceLowerThan10() throws Exception{
    	int offeredPrice = pairPort.requestJob(VALID_LOCATION, VALID_LOCATION, LOWER_THAN_TEN_PRICE).getJobPrice();
    	assertTrue(offeredPrice < LOWER_THAN_TEN_PRICE);
    }
    
    @Test
    public void testPriceHigherThan100() throws Exception{
	    assertEquals(null, pairPort.requestJob(VALID_LOCATION, VALID_LOCATION, HIGH_PRICE));
    }
    
    @Test
    public void testPairWorkingZone() throws Exception{
    	assertEquals(null, pairPort.requestJob(SOUTH_LOCATION, VALID_LOCATION, VALID_PRICE));
    }
    
    @Test
    public void testNotPairWorkingZone() throws Exception{
    
    	assertEquals(null, notPairPort.requestJob(VALID_LOCATION, NORTH_LOCATION, VALID_PRICE));
    }
    
    @Test
    public void testPairAndPairPrice() throws Exception{
    	int offeredPrice = pairPort.requestJob(VALID_LOCATION, VALID_LOCATION, PAIR_PRICE).getJobPrice();
    	assertTrue(offeredPrice < PAIR_PRICE);
    }
    
    @Test
    public void testPairAndNotPairPrice() throws Exception{
    	int offeredPrice = pairPort.requestJob(VALID_LOCATION, VALID_LOCATION, NOT_PAIR_PRICE).getJobPrice();
    	assertTrue(offeredPrice > NOT_PAIR_PRICE);
    }
    
    @Test
    public void testNotPairAndPairPrice() throws Exception{
    	int offeredPrice = notPairPort.requestJob(VALID_LOCATION, VALID_LOCATION, PAIR_PRICE).getJobPrice();
    	assertTrue(offeredPrice > PAIR_PRICE);
    }
    
    @Test
    public void testNotPairAndNotPairPrice() throws Exception{
    	int offeredPrice = notPairPort.requestJob(VALID_LOCATION, VALID_LOCATION, NOT_PAIR_PRICE).getJobPrice();
    	assertTrue(offeredPrice < NOT_PAIR_PRICE);
    }
    
    //Missing state testing!
}