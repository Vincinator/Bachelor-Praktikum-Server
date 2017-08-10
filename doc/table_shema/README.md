# Explanation of Tables in Database
| Table Name | Description |
|------------|-------------|
|`nodes`|contains ALL imported nodes, consists of 7 attributes. Key is `id`
|`relation_members`| is a table of 5 attributes `relation_id, member_id, member_type, member_role, sequence_id` and describe all kind of things like hiking, bus, flixbus, rmv routes, restriction (such as speed limitation, turning prohibition). Example and explaination for each attribute follows. This table give information about which node belongs to which relation. All items belonging to the same relation also have identical `relation_id`|
|`relations`|is a table of 6 attributes `id, version, user_id, tstamp, changeset_id, tags`. The only important things are `id, tags`. Tags contain all tags and its value e.g name = 'Bus 5515'|
|`schema_info`|Internal information for parsing. Not interesting for us|
|`users`|Empty. Not interesting for us|
|`way_nodes`|is a table of 3 attributes `way_id, node_id, sequence_id` and contains all information about streets and road, itd `id` and and member nodes. `sequence_id` tells the order of the member nodes.|
|`ways`|is a table of 7 atributes `id, version, user_id, tstamp, changeset_id, tags, nodes` and contains the more precise information about each way object. The interesting thing here are `tags, nodes`. `tags` tells information about the street e.g streetname, speedlimit etc and `nodes` is a list of id of nodes members|
|`spatial_ref_sys`|TODO: No Idea|




## Attribute Explanation:
#### nodes
| Attribute | Explanation | Sample Database | Sample in OSM File|
|-----------|--------------|-----------------|-------------------|
|id|relation id in osm file |'2502785849'|`<node id="2502785849" lat="49.9837076" lon="8.7870278"/>`|
|version|irrelevant|-1 is default||
|user_id|irrelevant|-1 is default||
|tstamp|irrelevant|1970-01-01 00:59:59 is default||
|changeset_id|irrelevant|-1 is default|
|tags|a string from type `hstore` containing the used tags and their values|"natural"=>"tree"|`<node id="2502785880" lat="49.9855201" lon="8.7933324"><tag k="natural" v="tree"/></node>`|
|geom|PostGIS own datatype `geometry` used to encode geometry. Encode with `ST_MakePoint(longitude, latitude)`. To read this as text in Postgresql use `SELECT ST_AsText(the_geom)`|0101000020E6100000E2B611AA2F9621407C86CB8525FE4840|lat="49.9855201" lon="8.7933324"|

#### relation_members
| Attribute | Explanation | Sample Database | Sample in OSM File|
|-----------|--------------|-----------------|-------------------|
|relation_id|relation id in osm file. Items belonging to the same relation have identical relation_id |7043760|`<relation id="7043760"...>`|
|member_id|the id of the member, this could be the id from a way, node, etc. element|'474409457'|`<member type="node" ref="474409457" role="stop"/>`|
|member_type|1 character attribut telling which type this member has e.g node, way|W|`<member type="way" ref="255736141" role="platform"/>`|
|member_role|telling which role this member has e.g is it a plattform, a stop, etc|plattform, stop, from, via, ect.|`<member type="node" ref="474409457" role="stop"/>`|
|sequence_id|tell the order of the member item|1(means the first element)|this item will be at the first spot|

#### relations
| Attribute | Explanation | Sample Database | Sample in OSM File|
|-----------|--------------|-----------------|-------------------|
|id|relation id in osm file. Items belonging to the same relation have identical relation_id |7043760|`<relation id="7043760"...>`|
|version|irrelevant|-1 is default||
|tstamp|irrelevant|1970-01-01 00:59:59 is default||
|changeset_id|irrelevant|-1 is default|
|tags|a string from type `hstore` containing the used tags and their values|"to"=>"Urberach Bahnhof", "ref"=>"U", "from"=>"Darmstadt Ludwigshöhstraße", "name"=>"Bus U: Darmstadt Ludwigshöhstraße => Urberach", "type"=>"route", "route"=>"bus", "state"=>"alternate", "network"=>"RMV", "public_transport:version"=>"2"|`<tag k="type" v="route"/>	<tag k="route" v="bus"/>`|

```
<relation id="7043760" version="3" timestamp="2017-03-12T10:28:28Z" changeset="46782076" uid="286452" user="TetiSoft">
		<member type="node" ref="474409457" role="stop"/>
		<member type="way" ref="255736141" role="platform"/>
		<member type="node" ref="2591193489" role="stop"/>
		<member type="way" ref="253142911" role="platform"/>
		<member type="node" ref="340147841" role="stop"/>
		<member type="way" ref="150135955" role="platform"/>
		<member type="node" ref="603151140" role="stop"/>
        (...)
		<tag k="to" v="Urberach Bahnhof"/>
		<tag k="ref" v="U"/>
		<tag k="from" v="Darmstadt Ludwigshöhstraße"/>
		<tag k="name" v="Bus U: Darmstadt Ludwigshöhstraße =&#62; Urberach"/>
		<tag k="type" v="route"/>
		<tag k="route" v="bus"/>
		<tag k="state" v="alternate"/>
		<tag k="network" v="RMV"/>
		<tag k="public_transport:version" v="2"/>
	</relation>
```

#### planet_osm_ways
| Attribute | Explanation | Sample Database | Sample in OSM File|
|-----------|--------------|-----------------|-------------------|
|id|id of the way object|14643870|`<way id="14643870" (...)>`|
|nodes|a list of `osm_id` of nodes, which make this way|"{42026498,45087388,283324560,43362414,43362424,41788329,41972120}"|`<nd ref="42026498"/><nd ref="45087388"/>`|
|tags|a list of used tags and their values|"{access,yes,bicycle,no,foot,yes,highway,tertiary,maxspeed,30(...)}"|`<tag k="ref" v="K 165"/><tag k="foot" v="yes"/>`|
```
<way id="14643870" version="7" timestamp="2017-04-07T15:52:20Z" changeset="47544133" uid="1650009" user="Geo Dät">
		<nd ref="42026498"/>
		<nd ref="45087388"/>
		<nd ref="283324560"/>
		<nd ref="43362414"/>
		<nd ref="43362424"/>
		<nd ref="41788329"/>
		<nd ref="41972120"/>
		<tag k="ref" v="K 165"/>
		<tag k="foot" v="yes"/>
		<tag k="name" v="Hauptstraße"/>
		<tag k="access" v="yes"/>
		<tag k="oneway" v="yes"/>
		<tag k="bicycle" v="no"/>
		<tag k="highway" v="tertiary"/>
		<tag k="maxspeed" v="30"/>
		<tag k="motor_vehicle" v="no"/>
</way>
```

# Source:
- http://wiki.openstreetmap.org/wiki/Osm2pgsql/schema#Tables_Created
- https://gis.stackexchange.com/questions/37099/in-osm2pgsql-how-is-the-planet-osm-roads-table-populated

# Helpful Information:
- Examples and Meaning of TAGS, KEYS...
https://taginfo.openstreetmap.org/
- How to list all Database using psql
https://dba.stackexchange.com/questions/1285/how-do-i-list-all-databases-and-tables-using-psql
