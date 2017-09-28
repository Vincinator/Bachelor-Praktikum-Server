package bp.server.service;

import bp.common.model.WayBlacklist;
import bp.common.model.obstacles.FastTrafficLight;
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
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.crypto.Data;
import java.io.Serializable;
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
   * Creates new stairs and add them to Hibernate and OSM DB
   *
   * @return HTTP Result
   */
  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response postNewStairs(Obstacle obstacle) {
    // update next possible NodeID
    getNextPossibleNodeAndWayID();

    // Give new Obstacle new OSM ID for Node
    obstacle.setOsm_id_start(nextPossibleNodeID);
    nextPossibleNodeID++;
    if(obstacle.getLatitudeEnd() != 0 && obstacle.getLongitudeEnd() != 0){
      obstacle.setOsm_id_end(nextPossibleNodeID);
      nextPossibleNodeID++;
    }
    ExportTool.getInstance().setNextPossibleNodeId(nextPossibleNodeID);

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
    ExportTool.getInstance().startExportProcess();
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
    getNextPossibleNodeAndWayID();

    way.setOsm_id(nextPossibleWayID);
    nextPossibleWayID++;

    for(Node n:way.getNodes()){
      n.setOsm_id(nextPossibleNodeID);
      n.setWay(way);
      nextPossibleNodeID++;
    }
    ExportTool.getInstance().setNextPossibleWayId(nextPossibleWayID);
    ExportTool.getInstance().setNextPossibleNodeId(nextPossibleNodeID);
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
    ExportTool.getInstance().startExportProcess();
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
    BarriersService bs = new BarriersService();

    Stairs stair1 = new Stairs("Lauteschlage", 8.65908, 49.87768, 8.65935, 49.87773, 10, "yes");
    stair1.setRamp_wheelchair("yes");
    stair1.setId_way(150032847);
    stair1.setId_firstnode(1629692805);
    stair1.setId_lastnode(2623782435L);
    bs.postNewStairs(stair1);


    FastTrafficLight trafficLight = new FastTrafficLight("Mathe",8.65797,49.87866,0,0,10);
    trafficLight.setId_way(27557892);
    trafficLight.setId_firstnode(531560);
    trafficLight.setId_lastnode(302547910);
    bs.postNewStairs(trafficLight);

    List<Node> nodes = new ArrayList<>();
    Way way1 = new Way("Ma Street", nodes);
    nodes.add(new Node(49.86867, 8.6686));
    nodes.add(new Node(49.86933, 8.66877));
    way1.setOsmid_firstWay(148850705);
    way1.setOsmid_firstWayFirstNode(2528617495L);
    way1.setOsmid_firstWaySecondNode(2528617475L);
    way1.setOsmid_secondWay(148061763);
    way1.setOsmid_secondWayFirstNode(1215967284);
    way1.setOsmid_secondWaySecondNode(626305);
    bs.postNewWay(way1);

    Stairs stair3 = new Stairs("Heinrich-Fuhr",8.67131, 49.87122, 8.66793, 49.87148, 124,"no");
    stair3.setId_way(15259487);
    stair3.setId_firstnode(3420827910L);
    stair3.setId_lastnode(207641109);
    bs.postNewStairs(stair3);

  }
}