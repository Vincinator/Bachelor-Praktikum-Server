package bp17.jerseyserver;

import bp.common.model.obstacles.FastTrafficLight;
import bp.common.model.obstacles.Stairs;
import bp.common.model.ways.Node;
import bp.common.model.ways.Way;
import bp.server.service.BarriersService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bi Banh Bao on 26.09.2017.
 */
public class ManuallTests {
    public static void main(String[] args) {

        // 1 Phase
        Stairs stair1 = new Stairs("Lauteschlage", 8.65908, 49.87768, 8.65935, 49.87773, 10, "yes");
        stair1.setRamp_wheelchair("yes");
        stair1.setId_way(150032847);
        stair1.setId_firstnode(1629692805);
        stair1.setId_lastnode(2623782435L);
        BarriersService bs = new BarriersService();
        bs.postNewStairs(stair1);

/*        Stairs stair3 = new Stairs("Heinrich-Fuhr",8.67131, 49.87122, 8.66793, 49.87148, 124,"no");
        stair3.setId_way(15259487);
        stair3.setId_firstnode(3420827910L);
        stair3.setId_lastnode(207641109);

        FastTrafficLight trafficLight = new FastTrafficLight("Mathe",8.65797,49.87866,0,0,10);
        trafficLight.setId_way(27557892);
        trafficLight.setId_firstnode(531560);
        trafficLight.setId_lastnode(302547910);

        List<Node> nodes = new ArrayList<>();
        Way way1 = new Way("Ma Street", nodes);
        Node node1 = new Node(49.86867, 8.6686);
        Node node2 = new Node(49.86933, 8.66877);
        nodes.add(node1);
        nodes.add(node2);
        way1.setOsmid_firstWay(148850705);
        way1.setOsmid_firstWayFirstNode(2528617495L);
        way1.setOsmid_firstWaySecondNode(2528617475L);
        way1.setOsmid_secondWay(148061763);
        way1.setOsmid_secondWayFirstNode(1215967284);
        way1.setOsmid_secondWaySecondNode(626305);

        BarriersService bs = new BarriersService();
        bs.postNewStairs(stair1);
        bs.postNewStairs(trafficLight);
        bs.postNewWay(way1);


        // 2 Phase - A Stair on our street
        Stairs stair2 = new Stairs("Rossdörfe", 8.66859,49.86887, 8.66874, 49.86915, 5, "");
        stair2.setWidth(10);
        stair2.setTactile_paving("yes");
        stair2.setId_way(511515717);
        stair2.setId_firstnode(5003906400L);
        stair2.setId_lastnode(5003906401L);
        bs.postNewStairs(stair2);
        bs.postNewStairs(stair3);*/
    }
}
