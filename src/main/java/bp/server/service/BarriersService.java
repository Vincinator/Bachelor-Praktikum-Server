package bp.server.service;

import bp.common.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static Session getSession() {
        // Save the Barrier via Hibernate to the Database.
        Configuration config = new Configuration();
        config.configure("hibernate.cfg.xml"); //populates the data of the configuration file
        SessionFactory sessionFactory = config.buildSessionFactory();
        return sessionFactory.openSession();
    }

    public static <T> String getData (Class<T> typeParameterClass) throws JsonProcessingException {

        // Save the Barrier via Hibernate to the Database.
        Configuration config = new Configuration();
        config.configure("hibernate.cfg.xml"); //populates the data of the configuration file
        SessionFactory sessionFactory = config.buildSessionFactory();
        Session session = sessionFactory.openSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(typeParameterClass);
        Root<T> root = criteria.from(typeParameterClass);
        criteria.select(root);
        List<T> datalist = session.createQuery(criteria).getResultList();
        session.close();

        ObjectMapper mapper = new ObjectMapper();
        JavaType listJavaType = mapper.getTypeFactory().constructCollectionType(List.class, typeParameterClass);

        return mapper.writerWithType(listJavaType).writeValueAsString(datalist);
    }

    @GET
    @Path("/stairs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStairs(){

        String result = "";
        try{
            result = getData(Stairs.class);

        } catch (Exception e){
            return Response.status(503).entity(e.toString()).build();
        }
        return Response.status(200).entity(result).build();

    }

    @GET
    @Path("/ramps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRamps(){
        String result = "";
        try{
            result = getData(Ramp.class);

        } catch (Exception e){
            return Response.status(503).entity(e.toString()).build();
        }
        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/unevennesses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnevennesses(){
        String result = "";
        try{
            result = getData(Unevenness.class);
        } catch (Exception e){
            return Response.status(503).entity(e.toString()).build();
        }
        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/tightpassages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTightPassages(){
        String result = "";
        try{
            result = getData(TightPassage.class);
        } catch (Exception e){
            return Response.status(503).entity(e.toString()).build();
        }
        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/elevators")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getElevators(){
        String result = "";
        try{
            result = getData(Elevator.class);
        } catch (Exception e){
            return Response.status(503).entity(e.toString()).build();
        }
        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/fasttrafficlights")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFastTrafficLights(){
        String result = "";
        try{
            result = getData(FastTrafficLight.class);
        } catch (Exception e){
            return Response.status(503).entity(e.toString()).build();
        }
        return Response.status(200).entity(result).build();
    }
    @GET
    @Path("/constructions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConstructions(){
        String result = "";
        try{
            result = getData(Construction.class);

        } catch (Exception e){
            return Response.status(503).entity(e.toString()).build();
        }
        return Response.status(200).entity(result).build();
    }

    @POST
    @Path("/stairs")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNewObstacle(Stairs stairs) {
        String result = "Stairs hinzugefügt: " + stairs;
        try{
            Session session = getSession();
            session.beginTransaction();
            session.save(stairs);
            session.getTransaction().commit();
        } catch (Exception e){
            return Response.status(503).entity(e.toString()).build();
        }
        return Response.status(201).entity(result).build();
    }

    @POST
    @Path("/ramps")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNewObstacle(Ramp ramp) {
        String result = "Ramp hinzugefügt: " + ramp;
        try{
            Session session = getSession();
            session.beginTransaction();
            session.save(ramp);
            session.getTransaction().commit();
        } catch (Exception e){
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
