package bp17.jerseyserver;

import bp17.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Vincent on 18.05.2017.
 */
@Path("/barriers")
public class BarriersService {


    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTrackInJSON(Obstacle barrier) {
        String result = "Barrier Saved: " + barrier;

        try{
            // Save the Barrier via Hibernate to the Database.
            Configuration config = new Configuration();
            config.configure();
            SessionFactory sessionFactory = config.buildSessionFactory();

            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(barrier);
            session.getTransaction().commit();

        } catch (Exception e){
            System.out.println(e.toString());

        }

        return Response.status(201).entity(result).build();
    }


    @GET
    @Path("/{id}")
    public Response getIt(@PathParam("id") String id) {

        return Response.status(200).entity("Barrier with, id : " + id).build();
    }

}
