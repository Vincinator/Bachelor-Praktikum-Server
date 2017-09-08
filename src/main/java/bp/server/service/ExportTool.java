package bp.server.service;

import bp.common.model.obstacles.*;
import bp.common.model.ways.Node;
import bp.common.model.ways.Way;
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
    private long nextPossibleWayId;
    private static volatile ExportTool instance;
    private Connection c;
    private Connection hibernate_con;
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
            Class.forName("org.postgresql.Driver");
            hibernate_con = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/hibernatedb","postgres","password");
            hibernate_con.setAutoCommit(false);
            System.out.println("Hibernate Connected.");

            Statement stmt1 = c.createStatement();
            ResultSet rs = stmt1.executeQuery("SELECT max(id) FROM nodes");
            rs.next();
            this.nextPossibleNodeId = rs.getLong("max")+1 ;
            rs.close();
            stmt1.close();

            Statement stmt2 = c.createStatement();
            ResultSet rs2 = stmt2.executeQuery("SELECT max(id) FROM ways");
            rs2.next();
            this.nextPossibleWayId = rs2.getLong("max")+1 ;
            rs2.close();
            stmt2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
     * the first method from Exporttool to be executed
     */
    public void startExportProcess(){
        writeObstaclesInOsmDatabase();
        writeWaysInOSMDatabase();
        closeUpAllConnections();
    }

    /**
     * write all the Obstacle Objects retrieved from hibernatedb in osm DB
     */
    private void writeObstaclesInOsmDatabase(){
        //TODO check if insert Obstacle working, not tested yet after small changes
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
                insertObstacleInTableNode(o,updateNodes);
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
     * @return a linked list of all Obstacle from hibernatedb
     */
    private List<Obstacle> getAllObstacles(){
        List<Obstacle> datalist = BarriersService.getDataAsList(Obstacle.class);

        //TODO remove println
        System.out.println("Number of Obstacles before check:"+datalist.size());
        Iterator<Obstacle> iter = datalist.iterator();
        while(iter.hasNext()){
            Obstacle o = iter.next();
            if(o.getOsm_id() != 0) iter.remove();
        }

        System.out.println("Number of Obstacles after check:"+datalist.size());
        return datalist;
    }

    /**
     * update the right IDs for all Obstacles POJO corresponding to osm DB
     * @param obslist list of obstacles retrieved from hibernatedb
     */
    private void updateIdsAllObstacles(List<Obstacle> obslist){
        String sql_update_id = "UPDATE obstacle SET osm_id = ? WHERE id = ? ;";
        PreparedStatement pstmt;
        try{
            pstmt = hibernate_con.prepareStatement(sql_update_id);
            for(Obstacle o:obslist){
                pstmt.setLong(1, nextPossibleNodeId);
                pstmt.setLong(2, o.getId());
                pstmt.execute();
                o.setOsm_id(nextPossibleNodeId);
                this.nextPossibleNodeId++;
            }
            pstmt.close();
            hibernate_con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("UPDATE OBSTACLE ID COMPLETED.");
    }

    /**
     * insert object o in node Table
     * @param o
     * @param pstmt a prepared statement: id, version, userID, tstamp, changesetID, tags, long and lat are to be filled in
     */
    private void insertObstacleInTableNode(Obstacle o, PreparedStatement pstmt) {
        long id = o.getOsm_id();
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
        list_nodes.add(list_nodes.indexOf(o.getId_firstnode()) + 1, o.getOsm_id());
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
        long obstacle_id = o.getOsm_id();
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
     * write all the ways in OSM Database
     */
    private void writeWaysInOSMDatabase(){
        List<Way> waysList = getAllWays();
        if(waysList.isEmpty()) return;
        System.out.println("Number of Ways: " + waysList.size());
        updateIdsAllWays(waysList);
        try {
            PreparedStatement insertInTableWays = null;
            PreparedStatement insertInTableWay_nodes = null;
            PreparedStatement insertInTableNodes = null;

            String sql_insertInTableWays = "INSERT INTO ways (id, version, user_id, tstamp, changeset_id, tags, nodes) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?);";
            String sql_insertInTableWay_nodes = "INSERT INTO way_nodes (way_id, node_id, sequence_id) "
                    + "VALUES (?, ?, ?);";
            String sql_insertInTableNodes = "INSERT INTO nodes (id, version, user_id, tstamp, changeset_id, tags, geom) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326));";

            insertInTableWays = c.prepareStatement(sql_insertInTableWays);
            insertInTableWay_nodes = c.prepareStatement(sql_insertInTableWay_nodes);
            insertInTableNodes = c.prepareStatement(sql_insertInTableNodes);

            for(Way w:waysList){
                insertWayInTableWay(w, insertInTableWays);
                insertWayInTableWayNodes(w, insertInTableWay_nodes);
                insertNodeInTableNodes(w, insertInTableNodes);
            }
            insertInTableWays.close();
            insertInTableWay_nodes.close();
            insertInTableNodes.close();
            c.commit();
            System.out.println("UPDATE OSM DB COMPLETED");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private List<Way> getAllWays() {
        List<Way> datalist = BarriersService.getDataAsList(Way.class);

        //TODO remove println
        System.out.println("Number of Ways before check:"+datalist.size());
        Iterator<Way> iter = datalist.iterator();
        while(iter.hasNext()){
            Way w = iter.next();
            if(w.getOsm_id() != 0) iter.remove();
        }
        System.out.println("Number of Ways after check:"+datalist.size());
        return datalist;
    }


    /**
     * Update all osm_id of retrieved Way objects and IDs of their nodes in hibernateDB
     * to save osm_id in hibernateDB as well and to show that certain way and node are already saved in osm DB
     * @param waysList
     */
    private void updateIdsAllWays(List<Way> waysList) {
        PreparedStatement update_way_id;
        PreparedStatement update_node_id;
        String sql_update_way_id = "UPDATE ways SET osm_id = ? WHERE id = ? ;";
        String sql_update_node_id = "UPDATE nodes SET osm_id = ? WHERE id = ? ;";

        try{
            update_way_id = hibernate_con.prepareStatement(sql_update_way_id);
            update_node_id = hibernate_con.prepareStatement(sql_update_node_id);
            for(Way w:waysList){
                update_way_id.setLong(1, nextPossibleWayId);
                update_way_id.setLong(2, w.getId());
                update_way_id.execute();
                w.setOsm_id(nextPossibleWayId);
                this.nextPossibleWayId++;

                List<Node> nodesList = w.getNodes();
                for(Node n: nodesList){
                    update_node_id.setLong(1, nextPossibleNodeId);
                    update_node_id.setLong(2, n.getId());
                    update_node_id.execute();
                    n.setOsm_id(nextPossibleNodeId);
                    this.nextPossibleNodeId++;
                }
            }
            update_way_id.close();
            update_node_id.close();
            hibernate_con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("UPDATE WAY and NODE ID COMPLETED.");
    }

    private void insertWayInTableWay(Way w, PreparedStatement insertInTableWays) {
        long id = w.getOsm_id();
        int version = -1;
        int userID = -1;
        long changesetID = -1;
        String tags = getHStoreValue(w);
        List<Node> nodesList = w.getNodes();
        Long[] nodeIDsArray = new Long[nodesList.size()];
        for(int i = 0; i < nodeIDsArray.length; i++){
            nodeIDsArray[i] = Long.valueOf(nodesList.get(i).getOsm_id());
        }
        try {
            Array nodes_result = c.createArrayOf("BIGINT", nodeIDsArray);
            insertInTableWays.setLong(1,id);
            insertInTableWays.setInt(2,version);
            insertInTableWays.setInt(3,userID);
            insertInTableWays.setTimestamp(4,current_timestamp);
            insertInTableWays.setLong(5, changesetID);
            insertInTableWays.setObject(6, tags,Types.OTHER);
            insertInTableWays.setArray(7,nodes_result);
            insertInTableWays.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertWayInTableWayNodes(Way w, PreparedStatement insertInTableWay_nodes) {
        long id = w.getOsm_id();
        int index = 0;
        for(Node n:w.getNodes()){
            try {
                insertInTableWay_nodes.setLong(1, id);
                insertInTableWay_nodes.setLong(2, n.getOsm_id());
                insertInTableWay_nodes.setInt(3, index);
                index++;
                insertInTableWay_nodes.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertNodeInTableNodes(Way w, PreparedStatement insertInTableNodes) {
        int version = -1;
        int userID = -1;
        long changesetID = -1;

        for(Node n:w.getNodes()){
            try {
                insertInTableNodes.setLong(1, n.getOsm_id());
                insertInTableNodes.setInt(2, version);
                insertInTableNodes.setInt(3, userID);
                insertInTableNodes.setTimestamp(4, current_timestamp);
                insertInTableNodes.setLong(5, changesetID);
                insertInTableNodes.setObject(6, getHStoreValue(n),Types.OTHER);
                insertInTableNodes.setDouble(7, n.getLongitude());
                insertInTableNodes.setDouble(8, n.getLatitude());
                insertInTableNodes.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param ob an Obstacle Object
     * @return a string in HStore format which contains all tag of the Obstacle o
     */
    private String getHStoreValue(Object ob){
        // TODO Docu about every single self defined Tags
        HashMap<String,String> tags = new HashMap<>();
        if(ob instanceof Obstacle){
            Obstacle o = (Obstacle)ob;
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
                    Construction construction = (Construction)o;
                    tags.put("barrier","construction");
                    tags.put("size", String.valueOf(construction.getSize()));
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    if (construction.getValidUntil() != null){
                        tags.put("expire", String.valueOf(formatter.format(construction.getValidUntil())));
                    }
                    // TODO Change code in Construction.class, either standard ValidUntil
                    // or set the value while creating obstacle object FRONT END Job
                    else tags.put("expire", "2017-08-28");
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
        }
        else if(ob instanceof Way){
            Way w = (Way)ob;
            if(!w.getName().equals("")) tags.put("name",w.getName());
            tags.put("highway","*");
        }

        return HStoreConverter.toString(tags);
    }


    /**
     * close up all opened connections. This is called at last.
     */
    private void closeUpAllConnections() {
        try {
            c.close();
            hibernate_con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ExportTool.getInstance().startExportProcess();
        System.out.println("DONEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
    }
}
