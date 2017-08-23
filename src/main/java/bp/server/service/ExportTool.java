package bp.server.service;

import bp.common.model.ObstacleTypes;
import bp.common.model.obstacles.Obstacle;
import bp.common.model.obstacles.Ramp;
import bp.common.model.obstacles.Stairs;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.postgresql.util.HStoreConverter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static bp.common.model.ObstacleTypes.STAIRS;

/**
 * This class handles all the export tool related tasks
 * Retrieve POJOs from hibernatedb
 * adjust these information and then add them to osm DB
 * finally export the osm database into an osm file
 * Created by Bi on 22.08.17.
 */
public class ExportTool {
    private long nextPossibleNodeId;
    private static volatile ExportTool instance;
    private Connection c;
    private String time;

    /**
     * Constructor sets global c, formated time and nextPossibleNodeId
     */
    public ExportTool(){
        this.c = PostgreSQLJDBC.getInstance().getConnection();
        LocalDateTime tmp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.time = tmp.format(formatter);
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select max(id) from nodes");
            rs.next();
            this.nextPossibleNodeId = rs.getLong("max")+1 ;
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static ExportTool getInstance(){
        if(instance == null){
            instance = new ExportTool();
        }
        return instance;
    }

    /**
     * @return a linked list of all Obstacle from hibernatedb
     */
    public List<Obstacle> getAllObstacles(){
        // Session Factory is created only once in the life span of the application. Get it from the Singleton
        SessionFactory sessionFactory = DatabaseSessionManager.instance().getSessionFactory();

        Session session = sessionFactory.openSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Obstacle> criteria = builder.createQuery(Obstacle.class);
        Root<Obstacle> root = criteria.from(Obstacle.class);
        criteria.select(root);
        List<Obstacle> datalist = session.createQuery(criteria).getResultList();
        session.close();
        return datalist;
    }

    /**
     * update the right IDs for all Obstacles POJO corresponding to osm DB
     * @param obslist list of obstacles retrieved from hibernatedb
     */
    public void updateIdsAllObstacles(List<Obstacle> obslist){
        for(Obstacle o:obslist){
            o.setId(nextPossibleNodeId);
            this.nextPossibleNodeId++;
        }
    }

    /**
     * write all the Obstacle Objects retrieved from hibernatedb in osm DB
     */
    public void writeInOsmDatabase(){
        List<Obstacle> obstacleList = getAllObstacles();
        updateIdsAllObstacles(obstacleList);
        try {
            Statement stmt = c.createStatement();
            for(Obstacle o:obstacleList){
                insertInTableNode(o,stmt);
                insertInTableWays(o,stmt);
                insterInTableWay_nodes(o,stmt);
            }
            stmt.close();
            c.commit();
            c.close();
            System.out.println("UPDATE OSM DB COMPLETED");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /* RECHEREEEEEEEEEEEEEEEEEEEE TODO remove
        for(Obstacle o:obstacleList){
            System.out.print("Class: "+o.getClass()+" ");
            System.out.print("Obstacle Type:"+o.getTypeCode()+" ID:");
            System.out.println(o.getId());
        }
        System.out.println("Next Possible ID: "+nextPossibleNodeId);*/
    }

    //TODO Javadoc
    private void insertInTableNode(Obstacle o, Statement stmt) {
        long id = o.getId();
        int version = -1;
        int userID = -1;
        long changesetID = -1;
        String tags = getHStoreValue(o);
        //TODO


    }

    //TODO Javadoc
    private void insertInTableWays(Obstacle o, Statement stmt) {
        //TODO
    }

    //TODO Javadoc
    private void insterInTableWay_nodes(Obstacle o, Statement stmt) {
        //TODO
    }

    //TODO Javadoc and Docu about every single self defined Tags
    private String getHStoreValue(Obstacle o){
        HashMap<String,String> tags = new HashMap<>();
        String result = "";
        switch (o.getTypeCode()){
            case STAIRS:
                Stairs stair = (Stairs)o;
                tags.put("barrier","stairs");
                tags.put("number_of_stairs",String.valueOf(stair.getNumberOfStairs()));
                tags.put("height_of_stair",String.valueOf(stair.getHeightOfStairs()));
                tags.put("handle_available",String.valueOf(stair.getHandleAvailable()));
                break;
            case RAMP:
                Ramp ramp = (Ramp)o;
                //TODO
                break;
            case UNEVENNESS:
                break;
            case CONSTRUCTION:
                break;
            case FAST_TRAFFIC_LIGHT:
                break;
            case ELEVATOR:
                break;
            case TIGHT_PASSAGE:
                break;
            default:
                break;
        }
        return result;
    }

    public static void main(String[] args) {
        /*ExportTool ins = ExportTool.getInstance();
        ins.writeInOsmDatabase();*/
        // Test from Bi
        HashMap<String,String> tags = new HashMap<>();
        tags.put("barrier","stairs");
        tags.put("number_of_stairs","10");
        System.out.println(HStoreConverter.toString(tags));
    }
}
