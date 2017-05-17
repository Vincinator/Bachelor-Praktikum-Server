package bp17.jerseyserver;

import bp17.model.Barrier;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "demobarriere" path)
 */
@Path("demobarriere")
public class DemoBarriere {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        String jsonInString = "";
        ObjectMapper mapper = new ObjectMapper(); // create once, reuse
        Barrier demoBarrier = new Barrier();
        demoBarrier.setName("Baustelle");
        try{
            jsonInString = mapper.writeValueAsString(demoBarrier);

        }catch(Exception e){
        }

        return jsonInString;
    }
}
