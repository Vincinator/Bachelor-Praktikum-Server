package bp.server.service;

import bp.common.model.Construction;
import bp.common.model.Elevator;
import bp.common.model.FastTrafficLight;
import bp.common.model.Ramp;
import bp.common.model.Stairs;
import bp.common.model.TightPassage;
import bp.common.model.Unevenness;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 *
 */
@Api
@Path("/barriers")
public class BarriersService {

  /** Starts a new Hibernate session.
   * @return a new session
   */
  private static Session getSession() {
    // Save the Barrier via Hibernate to the Database.
    Configuration config = new Configuration();
    config.configure("hibernate.cfg.xml");
    SessionFactory sessionFactory = config.buildSessionFactory();
    return sessionFactory.openSession();
  }

  /** Returns all barriers as JSON with the specified type from the database.
   * @param typeParameterClass variable for the type of barrier
   * @param <T> Type of Barrier
   * @return a JSON list with all Barriers of the specified type
   * @throws JsonProcessingException if Jackson throws exception
   */
  public static <T> String getData(Class<T> typeParameterClass)
      throws JsonProcessingException {

    // Save the Barrier via Hibernate to the Database.
    Configuration config = new Configuration();
    config.configure("hibernate.cfg.xml");
    SessionFactory sessionFactory = config.buildSessionFactory();
    Session session = sessionFactory.openSession();
    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<T> criteria = builder.createQuery(typeParameterClass);
    Root<T> root = criteria.from(typeParameterClass);
    criteria.select(root);
    List<T> datalist = session.createQuery(criteria).getResultList();
    session.close();

    ObjectMapper mapper = new ObjectMapper();
    JavaType listJavaType =
        mapper.getTypeFactory().constructCollectionType(List.class, typeParameterClass);

    return mapper.writerWithType(listJavaType).writeValueAsString(datalist);
  }

  /** (Jersey) API for retrieving all stairs as JSON List.
   * @return Stairs as JSON List
   */
  @GET
  @Path("/stairs")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStairs() {

    String result = "";
    try {
      result = getData(Stairs.class);

    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();

  }


  /** (Jersey) API for retrieving all ramps as JSON List.
   * @return Ramps as JSON List
   */
  @GET
  @Path("/ramps")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRamps() {
    String result = "";
    try {
      result = getData(Ramp.class);

    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();
  }

  /** (Jersey) API for retrieving all unevennesses as JSON List.
   * @return unevennesses as JSON List
   */
  @GET
  @Path("/unevennesses")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUnevennesses() {
    String result = "";
    try {
      result = getData(Unevenness.class);
    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();
  }


  /** (Jersey) API for retrieving all tightpassages as JSON List.
   * @return tightpassages as JSON List
   */
  @GET
  @Path("/tightpassages")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTightPassages() {
    String result = "";
    try {
      result = getData(TightPassage.class);
    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();
  }


  /** (Jersey) API for retrieving all elevators as JSON List.
   * @return elevators as JSON List
   */
  @GET
  @Path("/elevators")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getElevators() {
    String result = "";
    try {
      result = getData(Elevator.class);
    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();
  }


  /** (Jersey) API for retrieving all fasttrafficlights as JSON List.
   * @return fasttrafficlights as JSON List
   */
  @GET
  @Path("/fasttrafficlights")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFastTrafficLights() {
    String result = "";
    try {
      result = getData(FastTrafficLight.class);
    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();
  }


  /** (Jersey) API for retrieving all constructions as JSON List.
   * @return constructions as JSON List
   */
  @GET
  @Path("/constructions")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConstructions() {
    String result = "";
    try {
      result = getData(Construction.class);

    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();
  }

  /** (Jersey) API exposes the POST interface.
   * Creates new stairs
   * @return HTTP Result
   */
  @POST
  @Path("/stairs")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response postNewStairs(Stairs stairs) {
    String result = "Stairs hinzugefügt: " + stairs;
    try {
      Session session = getSession();
      session.beginTransaction();
      session.save(stairs);
      session.getTransaction().commit();
    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(201).entity(result).build();
  }


  /** (Jersey) API exposes the POST interface.
   * Creates new ramps
   * @return HTTP Result
   */
  @POST
  @Path("/ramps")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response postNewRamp(Ramp ramp) {
    String result = "Ramp hinzugefügt: " + ramp;
    try {
      Session session = getSession();
      session.beginTransaction();
      session.save(ramp);
      session.getTransaction().commit();
    } catch (Exception e) {
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
