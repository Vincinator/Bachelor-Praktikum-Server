package bp.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import bp.common.model.Obstacle;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


/**
 * Created by Vincent on 18.05.2017.
 */
@Path("/barriers")
public class BarriersService {


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObstacles(){

        String result = "";
        try{
            // Save the Barrier via Hibernate to the Database.
            Configuration config = new Configuration();
            config.configure("hibernate.cfg.xml"); //populates the data of the configuration file
            SessionFactory sessionFactory = config.buildSessionFactory();

            Session session = sessionFactory.openSession();

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Obstacle> criteria = builder.createQuery(Obstacle.class);
            Root<Obstacle> obstacleRoot = criteria.from(Obstacle.class);

            criteria.select(obstacleRoot);

            List<Obstacle> obstacles = session.createQuery(criteria).getResultList();

            session.close();


            ObjectMapper mapper = new ObjectMapper();

            result = mapper.writeValueAsString(obstacles);


        } catch (Exception e){
            // TODO: Entferne Exception aus Response für Produktiveinsatz.
            // Für Debugging wird die Exception als Response ausgegeben.
            return Response.status(503).entity(e.toString()).build();
        }

        return Response.status(200).entity(result).build();

    }

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
