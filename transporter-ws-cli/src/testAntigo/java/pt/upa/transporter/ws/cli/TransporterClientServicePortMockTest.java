package pt.upa.transporter.ws.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;


/**
 *  Unit Test suite using a mocked (simulated) service and port
 */
public class TransporterClientServicePortMockTest {
	
	
	
    // static members

	/** mocked web service endpoint address */
	private static String wsURL = "http://host:port/endpoint";
	private final static String PING_RESPONSE = " responding to ping request...Message given: ";

	
    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @AfterClass
    public static void oneTimeTearDown() {
    }


    // members
    
    /** used for the BindingProvider request context */
	Map<String,Object> contextMap = null;


    // initialization and clean-up for each test

    @Before
    public void setUp() {
    	contextMap = new HashMap<String,Object>();
    }

    @After
    public void tearDown() {
    	contextMap = null;
    }

    /**
     *  In this test the server is mocked to
     *  simulate a communication exception.
     */
    @Test(expected=WebServiceException.class)
    public <P extends TransporterPortType & BindingProvider> void testMockServerException(
        @Mocked final TransporterService service,
        @Mocked final P port)
        throws Exception {

    	
        // an "expectation block"
        // One or more invocations to mocked types, causing expectations to be recorded.
        new Expectations() {{
            new TransporterService();
            service.getTransporterPort(); result = port;
            port.getRequestContext(); result = contextMap;
           
            port.ping(anyString);
            result = new WebServiceException("fabricated");
        }};


        // Unit under test is exercised.
        TransporterClient client = new TransporterClient(wsURL);
        
        // call to mocked server
        client.ping("Mocking again...");
    }

    /**
     *  In this test the server is mocked to
     *  simulate a communication exception on a second call.
     */
    @Test
    public <P extends TransporterPortType & BindingProvider> void testMockServerExceptionOnSecondCall(
        @Mocked final TransporterService service,
        @Mocked final P port)
        throws Exception {

        // an "expectation block"
        // One or more invocations to mocked types, causing expectations to be recorded.
        new Expectations() {{
            new TransporterService();
            service.getTransporterPort(); result = port;
            port.getRequestContext(); result = contextMap;
            port.ping("test");
            // first call to sum returns the result
            result = PING_RESPONSE;
            // second call throws an exception
            result = new WebServiceException("fabricated");
        }};


        // Unit under test is exercised.
        TransporterClient client = new TransporterClient(wsURL);

        // first call to mocked server
        try {
           port.ping("test");
        } catch(WebServiceException e) {
            // exception is not expected
            fail();
        }

        // second call to mocked server
        try {
            port.ping("test");
            fail();
        } catch(WebServiceException e) {
            // exception is expected
            assertEquals("fabricated", e.getMessage());
        }
    }

    /**
     *  In this test the server is mocked to
     *  test the divide by zero exception propagation.
     */
    @Test
    public <P extends TransporterPortType & BindingProvider> void testMockServer(
        @Mocked final TransporterService service,
        @Mocked final P port)
        throws Exception {
    	/*
        // an "expectation block"
        // One or more invocations to mocked types, causing expectations to be recorded.
        new Expectations() {{
            new TransporterService();
            service.getTransporterPort(); result = port;
            port.getRequestContext(); result = contextMap;
            
            port.requestJob(anyString, anyString, 101);
            // first call to intdiv returns any number
            result = null;
            // second call throws an exception
            result = new DivideByZero("fabricated", new DivideByZeroType()); TODO
        }};


        // Unit under test is exercised.
        TransporterClient client = new TransporterClient(wsURL);

        // first call to mocked server
       // client.intdiv(10,5); TODO

        // second call to mocked server
        try {
            //client.intdiv(10,5);
            fail();
        } catch(Exception e  {
            // exception is expected
            assertEquals("fabricated", e.getMessage());
        }


        // a "verification block"
        // One or more invocations to mocked types, causing expectations to be verified.
        new Verifications() {{
            // Verifies that zero or one invocations occurred, with the specified argument value:
            //port.intdiv(anyInt, anyInt); maxTimes = 2; TODO
        }};
        */
    }
    
    

}