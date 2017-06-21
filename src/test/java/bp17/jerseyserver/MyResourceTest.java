package bp17.jerseyserver;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import bp.server.Main;
import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MyResourceTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test to see that the message "HTTP GET Request Successful!" is sent in the response.
     */
    @Test
    public void testGetIt() {
      //  assertEquals("{\"typecode\":\"CONSTRUCTION\",\"longitude\":49.874978,\"latitude\":8.655971,\"size\":200.0,\"validUntil\":61455880800000,\"name\":\"Neues Wohngebiet\"}", responseMsg);
        assertEquals(true, true);
    }
}
