package bp17.jerseyserver;

import bp12.model.Barrier;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 * Service accessible for local development under
 * http://localhost:8081/myapp/myresource
 * The server is configured to be exposed at 0.0.0.0:8081
 * this means that the server is exposed on all available networkinterfaces.
 */
@Path("myresource")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "HTTP GET Request Successful!";
    }
}
