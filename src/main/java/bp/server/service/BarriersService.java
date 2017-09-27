package bp.server.service;

import bp.common.model.WayBlacklist;
import bp.common.model.obstacles.Obstacle;
import bp.common.model.obstacles.Stairs;
import bp.common.model.ways.Node;
import bp.common.model.ways.Way;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.max;

/**
 *
 */
@Api
@Path("/barriers")
public class BarriersService {
  public static long nextPossibleNodeID = 0;
  public static long nextPossibleWayID = 0;

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
   * compare max ID from OSM DB with Hibernate DB and choose the max one
   */
  private void getNextPossibleNodeAndWayID() {
    // TODO Test if ID distributed correctly
    long osm_maxNodeID;
    long osm_maxWayID;
    long hibernate_maxNodeID;
    long hibernate_maxWayID;
    long hibernate_maxObstacleIDStart;
    long hibernate_maxObstacleIDEnd;

    Connection postgres_connection = PostgreSQLJDBC.getInstance().getConnection();

    Statement stmt1;
    try {
      // Get Informations from OSM Database
      stmt1 = postgres_connection.createStatement();
      ResultSet rs = stmt1.executeQuery("SELECT max(id) FROM nodes");
      rs.next();
      osm_maxNodeID = rs.getLong("max") ;
      rs.close();
      stmt1.close();

      Statement stmt2 = postgres_connection.createStatement();
      ResultSet rs2 = stmt2.executeQuery("SELECT max(id) FROM ways");
      rs2.next();
      osm_maxWayID = rs2.getLong("max");
      rs2.close();
      stmt2.close();

      // Get Information from Hibernate Database

      SessionFactory sessionFactory = DatabaseSessionManager.instance().getSessionFactory();
      Session session = sessionFactory.openSession();
      Query query_node = session.createQuery("SELECT max(N.osm_id) FROM Node N");
      Long maxNodeID = (Long) query_node.list().get(0);
      if(maxNodeID != null) hibernate_maxNodeID = maxNodeID;
      else hibernate_maxNodeID = 0;

      Query query_obstacle_start = session.createQuery("SELECT max(O.osm_id_start) FROM Obstacle O");
      Long maxObstacleIDStart = (Long) query_obstacle_start.list().get(0);
      if(maxObstacleIDStart != null) hibernate_maxObstacleIDStart = maxObstacleIDStart;
      else hibernate_maxObstacleIDStart = 0;

      Query query_obstacle_end = session.createQuery("SELECT max(O.osm_id_end) FROM Obstacle O");
      Long maxObstacleIDEnd = (Long) query_obstacle_end.list().get(0);
      if(maxObstacleIDEnd != null) hibernate_maxObstacleIDEnd = maxObstacleIDEnd;
      else hibernate_maxObstacleIDEnd = 0;

      Query query_way = session.createQuery("SELECT max(W.osm_id) FROM Way W");
      Long maxWayID = (Long) query_way.list().get(0);
      if(maxWayID != null) hibernate_maxWayID = maxWayID;
      else hibernate_maxWayID = 0;

      nextPossibleNodeID = max(max(max(osm_maxNodeID, hibernate_maxNodeID),hibernate_maxObstacleIDStart),hibernate_maxObstacleIDEnd)+1;
      nextPossibleWayID = max(osm_maxWayID, hibernate_maxWayID)+1;
      session.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
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
    List<T> dataList = session.createQuery(criteria).getResultList();
    session.close();
    return dataList;
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
    // update next possible NodeID if necessary
    if(nextPossibleNodeID == 0) getNextPossibleNodeAndWayID();

    // Give new Obstacle new OSM ID for Node
    obstacle.setOsm_id_start(nextPossibleNodeID);
    nextPossibleNodeID++;
    if(obstacle.getLatitudeEnd() != 0 && obstacle.getLongitudeEnd() != 0){
      obstacle.setOsm_id_end(nextPossibleNodeID);
      nextPossibleNodeID++;
    }

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
    if(nextPossibleWayID == 0 || nextPossibleNodeID == 0) getNextPossibleNodeAndWayID();

    way.setOsm_id(nextPossibleWayID);
    nextPossibleWayID++;

    for(Node n:way.getNodes()){
      n.setOsm_id(nextPossibleNodeID);
      n.setWay(way);
      nextPossibleNodeID++;
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
  @Path("/ways/blacklist")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWaysBlacklist() {
    String result = "";
    List<WayBlacklist> blacklist = null;
    List<Long> blacklist_osmid= new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    JavaType listJavaType = mapper.getTypeFactory().constructCollectionType(List.class, Long.class);
    try {
      blacklist = getDataAsList(WayBlacklist.class);
      result = mapper.writerWithType(listJavaType).writeValueAsString(blacklist);
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

/*
    Stairs stair2 = new Stairs();
    Stairs stair1 = new Stairs("sickness",long1, lat1, long2, lat2, 2, "yes");
    BarriersService bs = new BarriersService();
    bs.postNewStairs(stair1);


    Stairs stairs1 = new Stairs("holy", long1, lat1, 52, 12, true);
    Construction construction1 = new Construction("nothing", long2, lat2, 100, new java.sql.Date(System.currentTimeMillis()));



    ObjectMapper mapper = new ObjectMapper();
    String jsonString = "";
    try {
      jsonString = mapper.writeValueAsString(road);
      System.out.println(jsonString);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    BarriersService bs = new BarriersService();
    bs.postNewWay(road3);
    bs.postNewWay(road1);
    bs.postNewWay(road2);
    bs.postNewStairs(stairs1);
    bs.postNewStairs(construction1);

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
    }*/
  }
}