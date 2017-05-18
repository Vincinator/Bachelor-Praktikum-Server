package bp17.jerseyserver;

import bp17.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;

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
        Construction demoConstruction = new Construction("Neues Wohngebiet", 49.874978, 8.655971, 200, new Date(2017,5,18));
        try{
            jsonInString = mapper.writeValueAsString(demoConstruction);

        }catch(Exception e){
        }

        return jsonInString;
    }
}
