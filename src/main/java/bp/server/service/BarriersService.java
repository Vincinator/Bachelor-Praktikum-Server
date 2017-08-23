package bp.server.service;

import bp.common.model.*;
import bp.common.model.obstacles.Obstacle;
import bp.common.model.obstacles.Stairs;
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
import javax.xml.crypto.Data;

import io.swagger.annotations.Api;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import sun.net.www.protocol.file.FileURLConnection;

/**
 *
 */
@Api
@Path("/barriers")
public class BarriersService {

  /**
   * Starts a new Hibernate session.
   *
   * @return a new session
   */
  private static Session getSession() {
    // Save the Barrier via Hibernate to the Database.
    Configuration config = new Configuration();
    config.configure("hibernate.cfg.xml");
    SessionFactory sessionFactory = config.buildSessionFactory();
    return sessionFactory.openSession();
  }

  /**
   * Returns all barriers as JSON with the specified type from the database.
   *
   * @param typeParameterClass variable for the type of barrier
   * @param <T>                Type of Barrier
   * @return a JSON list with all Barriers of the specified type
   * @throws JsonProcessingException if Jackson throws exception
   */
  public static <T> String getData(Class<T> typeParameterClass)
          throws JsonProcessingException {

    // Session Factory is created only once in the life span of the application. Get it from the Singleton
    SessionFactory sessionFactory = DatabaseSessionManager.instance().getSessionFactory();

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

  /**
   * (Jersey) API for retrieving all stairs as JSON List.
   *
   * @return Stairs as JSON List
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStairs() {

    String result = "";
    try {
      result = getData(Obstacle.class);

    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();

  }


  /**
   * (Jersey) API exposes the POST interface.
   * Creates new stairs
   *
   * @return HTTP Result
   */
  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response postNewStairs(Obstacle obstacle) {
    String result = "Obstacle hinzugefügt: " + obstacle;
    try {
      // Session Factory is created only once in the life span of the application. Get it from the Singleton
      SessionFactory sessionFactory = DatabaseSessionManager.instance().getSessionFactory();

      Session session = sessionFactory.openSession();
      session.beginTransaction();
      session.save(obstacle);
      session.getTransaction().commit();

    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(201).entity(result).build();
  }


  @GET
  @Path("/{id}")
  public Response getObstacleById(@PathParam("id") String id) {

    return Response.status(200).entity("Barrier with, id: " + id).build();
  }


  /**
   * Trigger Export Tool, which will export a osm file including all the barriers
   *
   * @return HTTP Result
   */
  @GET
  @Path("/export")
  @Produces(MediaType.TEXT_HTML)
  public String export() {

    return "<html> <title>Export Tool</title><body><h1>Export Tool is here.</body></h1></html>" ;
  }
}