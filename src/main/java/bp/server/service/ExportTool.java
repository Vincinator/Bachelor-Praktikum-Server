package bp.server.service;

import bp.common.model.obstacles.*;
import bp.server.exceptions.SequenceIDNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.jaxb.SourceType;
import org.postgresql.util.HStoreConverter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.xml.transform.Result;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

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
    private String current_time;
    private Timestamp current_timestamp;

    /**
     * Constructor sets global c, formated current_time and nextPossibleNodeId
     */
    public ExportTool(){
        this.c = PostgreSQLJDBC.getInstance().getConnection();
        LocalDateTime tmp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.current_time = tmp.format(formatter);
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        current_timestamp = new Timestamp(now.getTime());
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT max(id) FROM nodes");
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

        //TODO remove println
        System.out.println("Number of Obstacles before check:"+datalist.size());
        PreparedStatement select_obstacle = null;
        String sql_select_obstacle = "SELECT Count(id) FROM nodes WHERE id = ? ;";
        try {
            select_obstacle = c.prepareStatement(sql_select_obstacle);
            Iterator<Obstacle> iter = datalist.iterator();
            while(iter.hasNext()){
                Obstacle o = iter.next();
                if(obstacleExistInDB(o, select_obstacle)) iter.remove();
            }
            if(select_obstacle != null) select_obstacle.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Number of Obstacles after check:"+datalist.size());
        return datalist;
    }

    /**
     * check if Obstacle o already exist in Database OSM
     * @param o
     * @param pstmt
     * @return
     */
    private boolean obstacleExistInDB(Obstacle o, PreparedStatement pstmt) throws SQLException {
        int count = 0;
        pstmt.setLong(1, o.getId());
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) count = rs.getInt(1);
        if(rs != null) rs.close();
        return count > 0;
    }

    /**
     * update the right IDs for all Obstacles POJO corresponding to osm DB
     * @param obslist list of obstacles retrieved from hibernatedb
     */
    public void updateIdsAllObstacles(List<Obstacle> obslist){
        Connection hibernate_con = null;
        String sql_update_id = "UPDATE obstacle SET id = ? WHERE id = ? ;";
        PreparedStatement pstmt;
        try{
            Class.forName("org.postgresql.Driver");
            hibernate_con = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/hibernatedb","postgres","password");
            hibernate_con.setAutoCommit(false);
            System.out.println("Hibernate Connected.");
            pstmt = hibernate_con.prepareStatement(sql_update_id);
            for(Obstacle o:obslist){
                pstmt.setLong(1, nextPossibleNodeId);
                pstmt.setLong(2, o.getId());
                pstmt.execute();
                o.setId(nextPossibleNodeId);
                this.nextPossibleNodeId++;
            }
            pstmt.close();
            hibernate_con.commit();
            hibernate_con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("UPDATE OBSTACLE ID COMPLETED.");
    }

    /**
     * write all the Obstacle Objects retrieved from hibernatedb in osm DB
     */
    public void writeInOsmDatabase(){
        List<Obstacle> obstacleList = getAllObstacles();
        if(obstacleList.isEmpty()) return;
        System.out.println("Number of Obstacles: " + obstacleList.size());
        updateIdsAllObstacles(obstacleList);
        try {
            PreparedStatement updateNodes = null;
            PreparedStatement updateWays = null;
            PreparedStatement updateWay_Nodes = null;
            PreparedStatement updateWay_Nodes2 = null;
            PreparedStatement insertWay_Nodes = null;
            PreparedStatement getSequenceId = null;

            String sql_updateNodes = "INSERT INTO nodes (id, version, user_id, tstamp, changeset_id, tags, geom) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326));";
            String sql_updateWays = "UPDATE ways SET nodes = ? WHERE id = ?;";
            String sql_updateWay_Nodes = "UPDATE way_nodes SET sequence_id = (sequence_id + 1)*(-1) "+
                    "WHERE sequence_id >= ? AND way_id = ?;";
            String sql_updateWay_Nodes2 = "UPDATE way_nodes SET sequence_id = sequence_id*(-1) "+
                    "WHERE (sequence_id)*(-1) >= ? AND way_id = ?;";
            String sql_insertWay_Nodes ="INSERT INTO way_nodes (way_id, node_id, sequence_id) "
                    +"VALUES (?, ?, ?);";
            String sql_getSequenceId = "SELECT sequence_id FROM way_nodes WHERE way_id = ? AND node_id = ?;";

            updateNodes = c.prepareStatement(sql_updateNodes);
            updateWays = c.prepareStatement(sql_updateWays);
            updateWay_Nodes = c.prepareStatement(sql_updateWay_Nodes);
            updateWay_Nodes2 = c.prepareStatement(sql_updateWay_Nodes2);
            insertWay_Nodes = c.prepareStatement(sql_insertWay_Nodes);
            getSequenceId = c.prepareStatement(sql_getSequenceId);
            for(Obstacle o:obstacleList){
                insertInTableNode(o,updateNodes);
                updateTableWays(o,updateWays);
                updateTableWay_nodes(o,updateWay_Nodes, updateWay_Nodes2, insertWay_Nodes, getSequenceId);
            }
            updateNodes.close();
            updateWays.close();
            updateWay_Nodes.close();
            updateWay_Nodes2.close();
            insertWay_Nodes.close();
            getSequenceId.close();
            c.commit();
            c.close();
            System.out.println("UPDATE OSM DB COMPLETED");
        } catch (SQLException e){
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

    /**
     * insert object o in node Table
     * @param o
     * @param pstmt a prepared statement: id, version, userID, tstamp, changesetID, tags, long and lat are to be filled in
     */
    private void insertInTableNode(Obstacle o, PreparedStatement pstmt) {
        long id = o.getId();
        int version = -1;
        int userID = -1;
        long changesetID = -1;
        String tags = getHStoreValue(o);
        try {
            pstmt.setLong(1, id);
            pstmt.setInt(2, version);
            pstmt.setInt(3, userID);
            pstmt.setTimestamp(4, current_timestamp);
            pstmt.setLong(5, changesetID);
            pstmt.setObject(6, tags,Types.OTHER);
            pstmt.setDouble(7, o.getLongitude());
            pstmt.setDouble(8, o.getLatitude());
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * update table ways for Obstacle Object o
     * to do is to insert the id of Obstacle o in the "nodes" entry of the suited way item
     * which is a array of NodeIDs
     * @param o the Obstacle Object to be inserted to the table
     * @param pstmt the prepared statement
     */
    private void updateTableWays(Obstacle o, PreparedStatement pstmt) {
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nodes FROM ways WHERE id = "
                    +"'"+String.valueOf(o.getId_way())+"';");
            rs.next();
            Array nodes = rs.getArray(1);
            Long[] a_nodes = (Long[])nodes.getArray();
            Long[] result = insertNodeInArray(a_nodes, o);
            Array result_nodes = c.createArrayOf("BIGINT", result);
            pstmt.setArray(1,result_nodes);
            pstmt.setLong(2, o.getId_way());
            pstmt.execute();
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * help method for updateTableWays
     * @param array
     * @param o
     * @return new array list of ID, with ID of o at the right posision
     */
    private Long[] insertNodeInArray(Long[] array, Obstacle o){
        Long[] tmp = new Long[array.length + 1];
        ArrayList<Long> list_nodes = new ArrayList<>();
        for(Long n:array){
            list_nodes.add(n);
        }
        list_nodes.add(list_nodes.indexOf(o.getId_firstnode()) + 1, o.getId());
        return list_nodes.toArray(tmp);
    }

    /**
     * Update Table way_nodes. Important is to update the sequence_id of each affected item
     * @param o the Obstacle Object
     * @param updatestmt Prepare Update Statement
     * @param updatestmt2 second part of Prepare Statement
     * @param insertstmt preparestatement to insert new instance in way_nodes table
     * @param getseqstmt Prepare SELECT Statement to retrieve sequence ID
     */
    private void updateTableWay_nodes(Obstacle o, PreparedStatement updatestmt, PreparedStatement updatestmt2,
            PreparedStatement insertstmt , PreparedStatement getseqstmt) {
        long sequence_id_firstNode;
        long way_id = o.getId_way();
        long obstacle_id = o.getId();
        long sequence_id_obstacle;
        try {
            getseqstmt.setLong(1, o.getId_way());
            getseqstmt.setLong(2, o.getId_firstnode());
            ResultSet rs = getseqstmt.executeQuery();
            if(rs.next()){
                sequence_id_firstNode = rs.getInt("sequence_id");
                sequence_id_obstacle = sequence_id_firstNode + 1;
                System.out.println("Debug: SequenceID Obstacle:"+sequence_id_obstacle);
                updatestmt.setLong(1, sequence_id_obstacle);
                updatestmt.setLong(2, way_id);
                updatestmt2.setLong(1, sequence_id_obstacle);
                updatestmt2.setLong(2, way_id);
                insertstmt.setLong(1, way_id);
                insertstmt.setLong(2, obstacle_id);
                insertstmt.setLong(3, sequence_id_obstacle);
                updatestmt.execute();
                updatestmt2.execute();
                insertstmt.execute();
                rs.close();
            }
            else{
                rs.close();
                throw new SequenceIDNotFoundException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (SequenceIDNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     *
     * @param o an Obstacle Object
     * @return a string in HStore format which contains all tag of the Obstacle o
     */
    private String getHStoreValue(Obstacle o){
        // TODO Docu about every single self defined Tags
        HashMap<String,String> tags = new HashMap<>();
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
                tags.put("barrier","ramp");
                break;
            case UNEVENNESS:
                Unevenness uneven = (Unevenness)o;
                tags.put("barrier","uneveness");
                tags.put("length", String.valueOf(uneven.getLength()));
                break;
            case CONSTRUCTION:
                Construction constuction = (Construction)o;
                tags.put("barrier","construction");
                tags.put("size", String.valueOf(constuction.getSize()));
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                //tags.put("expire", String.valueOf(formatter.format(constuction.getValidUntil())));
                // TODO Change code in Construction.class, either standard ValidUntil
                // or set the value while creating obstacle object FRONT END Job
                tags.put("expire", "2017-08-28");
                break;
            case FAST_TRAFFIC_LIGHT:
                FastTrafficLight tflight = (FastTrafficLight)o;
                tags.put("barrier","fasttrafficlight");
                tags.put("duration",String.valueOf(tflight.getDuration()));
                break;
            case ELEVATOR:
                // TODO maybe remove from to attribute from BP.Common
                Elevator ele = (Elevator)o;
                tags.put("barrier","elevator");
                break;
            case TIGHT_PASSAGE:
                TightPassage tpass = (TightPassage)o;
                tags.put("barrier","tightpassage");
                tags.put("passagewidth", String.valueOf(((TightPassage) o).getWidth()));
                break;
            default:
                break;
        }

        return HStoreConverter.toString(tags);
    }

    public static void main(String[] args) {
        ExportTool.getInstance().writeInOsmDatabase();
        System.out.println("DONEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
    }
}
