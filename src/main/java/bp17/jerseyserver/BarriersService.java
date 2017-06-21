package bp17.jerseyserver;

import bp17.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Vincent on 18.05.2017.
 */
@Path("/barriers")
public class BarriersService {


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNewObstacle(Obstacle obstacle) {
        String result = "Obstacle hinzugefügt: " + obstacle;

        try{
            // Save the Barrier via Hibernate to the Database.
            Configuration config = new Configuration();
            config.configure("hibernate.cfg.xml"); //populates the data of the configuration file
            SessionFactory sessionFactory = config.buildSessionFactory();

            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(obstacle);
            session.getTransaction().commit();

        } catch (Exception e){
            // TODO: Entferne Exception aus Response für Produktiveinsatz.
            // Für Debugging wird die Exception als Response ausgegeben.
            return Response.status(503).entity(e.toString()).build();
        }

        return Response.status(201).entity(result).build();
    }


    @GET
    @Path("/{id}")
    public Response getIttt(@PathParam("id") String id) {

        return Response.status(200).entity("Barrier with, id: " + id).build();
    }

}
