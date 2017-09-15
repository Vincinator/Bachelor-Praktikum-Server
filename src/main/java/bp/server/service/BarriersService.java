package bp.server.service;

import bp.common.model.*;
import bp.common.model.obstacles.Construction;
import bp.common.model.obstacles.Obstacle;
import bp.common.model.obstacles.Stairs;
import bp.common.model.ways.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
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
    List<T> datalist = getDataAsList(typeParameterClass);
    ObjectMapper mapper = new ObjectMapper();
    JavaType listJavaType =
            mapper.getTypeFactory().constructCollectionType(List.class, typeParameterClass);

    return mapper.writerWithType(listJavaType).writeValueAsString(datalist);
  }

  /**
   * return a list of retrieved data in form of a list of Objects
   * @param typeParameterClass
   * @param <T>
   * @return
   */
  public static <T> List<T> getDataAsList(Class<T> typeParameterClass){
    // Session Factory is created only once in the life span of the application. Get it from the Singleton
    SessionFactory sessionFactory = DatabaseSessionManager.instance().getSessionFactory();

    Session session = sessionFactory.openSession();

    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<T> criteria = builder.createQuery(typeParameterClass);
    Root<T> root = criteria.from(typeParameterClass);
    criteria.select(root);
    List<T> datalist = session.createQuery(criteria).getResultList();
    session.close();
    return datalist;
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

  /**
   * (Jersey) API exposes the POST interface.
   * Creates new way and save them in the database
   * @param way
   * @return
   */
  @POST
  @Path("/ways")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response postNewWay(Way way) {
    for(Node n:way.getNodes()){
      n.setWay(way);
    }
    String result = "Way hinzugefügt: " + way;
    try {
      // Session Factory is created only once in the life span of the application. Get it from the Singleton
      SessionFactory sessionFactory = DatabaseSessionManager.instance().getSessionFactory();
      Session session = sessionFactory.openSession();
      session.beginTransaction();
      session.save(way);
      session.getTransaction().commit();

    } catch (Exception e) {
      return Response.status(503).entity(e.toString()+way.toString()).build();
    }
    return Response.status(201).entity(result).build();
  }

  /**
   * API to get all the ways objects
   * @return all Way Objects from Database in JSON Format
   */
  @GET
  @Path("/ways")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWays() {
    String result = "";
    try {
      result = getData(Way.class);

    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();
  }

  /**
   * API to get all the ways object around a certain coordinates with radius
   * @param lat1
   * @param long1
   * @param radius
   * @return
   */
  @GET
  @Path("/ways/radius")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWaysInRadius(
          @QueryParam("lat1") double lat1,
          @QueryParam("long1") double long1,
          @QueryParam("radius") int radius) {
    List<Way> waysList = getDataAsList(Way.class);
    List<Way> waysResult = new ArrayList<Way>();
    for(Way w:waysList){
      for(Node n:w.getNodes()){
        if(CoordinationsDistance.distFrom(lat1,long1,n.getLatitude(),n.getLongitude()) <= radius){
          waysResult.add(w);
          break;
        }
      }
    }
    String result = "";
    ObjectMapper mapper = new ObjectMapper();
    JavaType listJavaType =
            mapper.getTypeFactory().constructCollectionType(List.class, Way.class);
    try {
      result = mapper.writerWithType(listJavaType).writeValueAsString(waysResult);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    } catch (Exception e) {
      return Response.status(503).entity(e.toString()).build();
    }
    return Response.status(200).entity(result).build();
  }


  @GET
  @Path("/getObstableById/{id}")
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
    ExportTool.getInstance().startExportProcess();
    return "<html> <title>Export Tool</title><body><h1>All the Obstacles are added" +
            "to the OSM Database. Run ExportOsmFile.sh to export an osm file.</body></h1></html>" ;
  }
  public static void main(String[] args) {
    double lat1 = 49.877633;
    double long1 = 8.649615;
    double lat2 = 49.877405;
    double long2 = 8.649679;
    double lat3 = 49.876802;
    double long3 = 8.649559;
    double lat4 = 8.111111;
    double long4 = 49.666666;
    Node node1 = new Node(lat1, long1);
    Node node2 = new Node(lat2, long2);
    Node node3 = new Node(lat3, long3);
    Node node4 = new Node(lat1, long2);
    Node node5 = new Node(lat2, long3);
    Node node6 = new Node(lat3, long1);
    Node node7 = new Node(lat4,long4);
    Node node8 = new Node(long4, lat4);

    ArrayList<Node> nodes = new ArrayList<Node>();
    nodes.add(node1);
    nodes.add(node2);
    nodes.add(node3);
    Way road1 = new Way("", nodes);
    for (Node n : nodes) n.setWay(road1);

    ArrayList<Node> nodes2 = new ArrayList<>();
    nodes2.add(node4);
    nodes2.add(node5);
    nodes2.add(node6);
    Way road2 = new Way("", nodes2);
    for (Node n : nodes2) n.setWay(road2);

    ArrayList<Node> nodes3 = new ArrayList<>();
    nodes3.add(node7);
    nodes3.add(node8);
    Way road3 = new Way("",nodes3);
    for (Node n : nodes3) n.setWay(road3);


    Stairs stairs1 = new Stairs("holy", long1, lat1, 52, 12, true);
    Construction construction1 = new Construction("nothing", long2, lat2, 100, new java.sql.Date(System.currentTimeMillis()));


/*
    ObjectMapper mapper = new ObjectMapper();
    String jsonString = "";
    try {
      jsonString = mapper.writeValueAsString(road);
      System.out.println(jsonString);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }*/

    BarriersService bs = new BarriersService();
    bs.postNewWay(road3);
    /*bs.postNewWay(road1);
    bs.postNewWay(road2);
    bs.postNewStairs(stairs1);
    bs.postNewStairs(construction1);*/

    List<Way> alist = getDataAsList(Way.class);
    for (Way w : alist) System.out.println(w.getOsm_id() == 0);

    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    List<Obstacle> obslist = getDataAsList(Obstacle.class);
    for (Obstacle ob : obslist) {
      System.out.println(ob.toString());
      if (ob instanceof Construction) {
        Construction cons = (Construction) ob;
        System.out.printf(String.valueOf(formatter.format(cons.getValidUntil())));
      }
    }
  }
}