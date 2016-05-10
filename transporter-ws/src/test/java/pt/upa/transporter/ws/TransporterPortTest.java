package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.Properties;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.lang.NullArgumentException;

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
	private final static String EVEN_TRANSPORTER_NAME = "UpaTransporter2";
	private final static String ODD_TRANSPORTER_NAME = "UpaTransporter1";
	private final static String VALID_LOCATION = "Lisboa";
	private final static String SOUTH_LOCATION = "Faro";
	private final static String NORTH_LOCATION = "Porto";
	private final static String UNKNOWN_LOCATION = "Tavira";
	private final static String PING_RESPONSE = " responding to ping request...Message given: ";
	private final static String UNKNOWN_JOB_ID = "unknown";
	private static String name;
	private final static int VALID_PRICE = 50;
	private final static int HIGH_PRICE = 101;
	private final static int LOWER_THAN_TEN_PRICE = 9;
	private final static int EVEN_PRICE = 20;
	private final static int ODD_PRICE = 21;
	private final static int NEGATIVE_PRICE = -1;
    // one-time initialization and clean-up
	
	@Resource
    WebServiceContext ctxt;

    @BeforeClass
    public static void oneTimeSetUp() {
    	
    }

    @AfterClass
    public static void oneTimeTearDown() {

    }


    // members
    private TransporterPort evenPort;
    private TransporterPort oddPort;

    // initialization and clean-up for each test
    @Before
    public void setUp() {
    	evenPort = new TransporterPort(EVEN_TRANSPORTER_NAME);
    	oddPort = new TransporterPort(ODD_TRANSPORTER_NAME);
    	String[] tempList = evenPort.ping("").split(" ");
    	name = tempList[0];
    	
    }

    
    @After
    public void tearDown() {
    	evenPort = null;
    	oddPort = null;
    }

    // tests
    @Test
    public void testPingNullString() throws Exception {
    	assertEquals(name + PING_RESPONSE,evenPort.ping(""));
    }
    
    @Test
    public void testPingEmptyString() throws Exception {
    	assertEquals(name + PING_RESPONSE,evenPort.ping(""));
    }
    
    @Test
    public void testPingMessage() throws Exception {
    	String test = "test";
    	assertEquals(name + PING_RESPONSE+test, evenPort.ping(test));
    }
    
    @Test(expected = BadPriceFault_Exception.class)
    public void testNegativePrice() throws Exception {
    	evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, NEGATIVE_PRICE);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testNullOrigin() throws Exception{
    	evenPort.requestJob(null, VALID_LOCATION, VALID_PRICE);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testNullDestination() throws Exception{
    	evenPort.requestJob(VALID_LOCATION, null, VALID_PRICE);
    }
    
    @Test
    public void testPriceZERO() throws Exception{
    	int offeredPrice = evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, 0).getJobPrice();
    	assertTrue(offeredPrice == 0);
    }
    
    @Test
    public void testPriceONE() throws Exception{
    	int offeredPrice = evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, 1).getJobPrice();
    	assertTrue(offeredPrice == 0);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testUnknownOrigin() throws Exception{
    	evenPort.requestJob(UNKNOWN_LOCATION, VALID_LOCATION, VALID_PRICE);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testUnknownDestination() throws Exception{
    	evenPort.requestJob(VALID_LOCATION, UNKNOWN_LOCATION, VALID_PRICE);
    }
    
    @Test
    public void testPriceLowerThan10() throws Exception{
    	int offeredPrice = evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, LOWER_THAN_TEN_PRICE).getJobPrice();
    	assertTrue(offeredPrice < LOWER_THAN_TEN_PRICE);
    }
    
    @Test
    public void testPriceHigherThan100() throws Exception{
	    assertEquals(null, evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, HIGH_PRICE));
    }
    
    @Test
    public void testEvenWorkingZone() throws Exception{
    	assertEquals(null, evenPort.requestJob(SOUTH_LOCATION, VALID_LOCATION, VALID_PRICE));
    }
    
    @Test
    public void testNotEvenWorkingZone() throws Exception{
    
    	assertEquals(null, oddPort.requestJob(VALID_LOCATION, NORTH_LOCATION, VALID_PRICE));
    }
    
    @Test
    public void testEvenAndEvenPrice() throws Exception{
    	int offeredPrice = evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, EVEN_PRICE).getJobPrice();
    	assertTrue(offeredPrice < EVEN_PRICE);
    }
    
    @Test
    public void testEvenAndOddPrice() throws Exception{
    	int offeredPrice = evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, ODD_PRICE).getJobPrice();
    	assertTrue(offeredPrice > ODD_PRICE);
    }
    
    @Test
    public void testOddAndEvenPrice() throws Exception{
    	int offeredPrice = oddPort.requestJob(VALID_LOCATION, VALID_LOCATION, EVEN_PRICE).getJobPrice();
    	assertTrue(offeredPrice > EVEN_PRICE);
    }
    
    @Test
    public void testOddAndOddPrice() throws Exception{
    	int offeredPrice = oddPort.requestJob(VALID_LOCATION, VALID_LOCATION, ODD_PRICE).getJobPrice();
    	assertTrue(offeredPrice < ODD_PRICE);
    }
    
    @Test(expected = BadJobFault_Exception.class)
    public void testDecideJobUnknownID() throws Exception{
    	evenPort.decideJob(UNKNOWN_JOB_ID, true);
    }
    
    @Test(expected = BadJobFault_Exception.class)
    public void testDecideJobNullString() throws Exception{
    	evenPort.decideJob(null, true);
    }
    
    @Test
    public void testDecideJobAccept() throws Exception{
    	JobView job = evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
    	evenPort.decideJob(job.getJobIdentifier(), true);
    	assertEquals(JobStateView.ACCEPTED, evenPort.jobStatus(job.getJobIdentifier()).getJobState());
    }
    
    @Test
    public void testDecideJobDeny() throws Exception{
    	JobView job = evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
    	evenPort.decideJob(job.getJobIdentifier(), false);
    	assertEquals(JobStateView.REJECTED, evenPort.jobStatus(job.getJobIdentifier()).getJobState());
    }
    
    @Test
    public void testJobStatusUnknownID() throws Exception{
    	assertEquals(null, evenPort.jobStatus(UNKNOWN_JOB_ID));
    }
    
    @Test
    public void testJobStatusNullString() throws Exception{
    	assertEquals(null, evenPort.jobStatus(null));
    }
    
    @Test
    public void testJobStatusProposed() throws Exception{
    	JobView job = evenPort.requestJob(VALID_LOCATION, VALID_LOCATION, VALID_PRICE);
    	assertEquals(job.getJobState(), JobStateView.PROPOSED);
    }
}