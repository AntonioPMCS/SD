package pt.upa.broker.ws.it;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PingIT extends BaseBrokerIT {
	@Test
	public void pingTest() {
		assertNotNull(brokerClient.ping("test"));
	}
}
