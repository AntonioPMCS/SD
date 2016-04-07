package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *  Unit Test example
 *  
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers 
 */
public class ExampleTest {

    // static members


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
    
    
    @Test
    public void testNegativePrice() {
    	try{
    		pairPort.requestJob("Leiria", "Lisboa", -1);
    	}
    	catch(BadPriceFault_Exception e){
    		String expectedMessage = "Price offered can't be lower than zero!";
    		assertEquals(expectedMessage, e.getMessage());
    	}
    	catch(Exception e){
    		Assert.fail("Should have thrown BadPriceFault_Exception");
    	}
    }
    
    @Test
    public void testUnknownOrigin(){
    	try{
    		pairPort.requestJob("Tavira", "Lisboa", 20);
    	}
    	catch(BadLocationFault_Exception e){
    		String expectedMessage = "Origin or destination location are invalid!";
    		assertEquals(expectedMessage, e.getMessage());
    	}
    	catch(Exception e){
    		Assert.fail("Should have thrown BadLocationFault_Exception");
    	}
    }
    
    @Test
    public void testUnknownDestiny(){
    	try{
    		pairPort.requestJob("Lisboa", "Tavira", 20);
    	}
    	catch(BadLocationFault_Exception e){
    		String expectedMessage = "Origin or destination location are invalid!";
    		assertEquals(expectedMessage, e.getMessage());
    	}
    	catch(Exception e){
    		Assert.fail("Should have thrown BadLocationFault_Exception");
    	}
    }
    
    @Test
    public void testPriceLowerThan10(){
    	try{
    		
	    	int clientPrice = 9;
	    	int offeredPrice = pairPort.requestJob("Leiria", "Lisboa", clientPrice).getJobPrice();
	    	assertTrue(offeredPrice < clientPrice);
	    	
    	}
    	catch(Exception e){
    		Assert.fail("No exception should be caught in this test");
    	}
    }
    
    @Test
    public void testPriceHigherThan100(){
    	try{
	    	assertEquals(null, pairPort.requestJob("Lisboa", "Leiria", 101));
    	}
    	catch(Exception e){
    		Assert.fail("No exception should be caught in this test");
    	}
    }
    
    @Test
    public void testPairWorkingZone(){
    	try{
    		assertEquals(null, pairPort.requestJob("Faro", "Lisboa", 20));
    	}
    	catch(Exception e){
    		Assert.fail("No exception should be caught in this test");
    	}
    }
    
    @Test
    public void testNotPairWorkingZone(){
    	try{
    		assertEquals(null, notPairPort.requestJob("Lisboa", "Porto", 20));
    	}
    	catch(Exception e){
    		Assert.fail("No exception should be caught in this test");
    	}
    }
    
    @Test
    public void testPairAndPairPrice(){
    	try{
    		
	    	int clientPrice = 20;
	    	int offeredPrice = pairPort.requestJob("Leiria", "Lisboa", clientPrice).getJobPrice();
	    	assertTrue(offeredPrice < clientPrice);
	    	
    	}
    	catch(Exception e){
    		Assert.fail("No exception should be caught in this test:" + e.getMessage());
    	}
    }
    
    @Test
    public void testPairAndNotPairPrice(){
    	try{
    		
	    	int clientPrice = 21;
	    	int offeredPrice = pairPort.requestJob("Leiria", "Lisboa", clientPrice).getJobPrice();
	    	assertTrue(offeredPrice > clientPrice);
	    	
    	}
    	catch(Exception e){
    		Assert.fail("No exception should be caught in this test:" + e.getMessage());
    	}
    }
    
    @Test
    public void testNotPairAndPairPrice(){
    	try{
    		
	    	int clientPrice = 20;
	    	int offeredPrice = notPairPort.requestJob("Leiria", "Lisboa", clientPrice).getJobPrice();
	    	assertTrue(offeredPrice > clientPrice);
	    	
    	}
    	catch(Exception e){
    		Assert.fail("No exception should be caught in this test:" + e.getMessage());
    	}    	
    }
    
    @Test
    public void testNotPairAndNotPairPrice(){
    	try{
    		
	    	int clientPrice = 21;
	    	int offeredPrice = notPairPort.requestJob("Leiria", "Lisboa", clientPrice).getJobPrice();
	    	assertTrue(offeredPrice < clientPrice);
	    	
    	}
    	catch(Exception e){
    		Assert.fail("No exception should be caught in this test:" + e.getMessage());
    	}   	
    }
}