# Explaination of Tables in Database
| Table Name | Description |
|------------|-------------|
|`planet_osm_line`|contains ALL imported ways and contains all 69 attributes. Key is `osm_id`
|`planet_osm_nodes`|is a light version of `planet_osm_point` and contains only 3 attributes `id`, `lat`, `long`|
|`planet_osm_point`|contains all important nodes with tags, this one is a full version with 69 attributes|
|`planet_osm_polygon`|contains all imported polygons. Relations seem to be resolved for that|
|`planet_osm_rels`| is a table of 6 attributes `id, way_off, rel_off, parts, members, tags` and describe all kind of things like hiking, bus, flixbus, rmv routes, restriction (such as speed limitation, turning prohibition). Example and explaination for each attribute follows|
|`planet_osm_roads`|contains a SUBSET of `planet_osm_line` suitable for rendering at low (weiter weg) zoom levels. `planet_osm_line` contains too many elements to render on overview maps|
|`planet_osm_ways`|contains the roads and streets in `planet_osm_line`, probably describes the same amount. The difference is that `planet_osm_ways` determines of which nodes a way is composed of. The 3 attributes are `id`(id = osm_id from `planet_osm_lines`) `nodes` (is a list of nodes of which a node is composed of) and `tags` (a list of used tags and their values). More information and examples follow|
|`spatial_ref_sys`|TODO: No Idea|

## Attribute Explaination:
#### planet_osm_rels
| Attribute | Explaination | Sample Database | Sample in OSM File|
|-----------|--------------|-----------------|-------------------|
|id|relation id in osm file|7043760|`<relation id="7043760"...>`|
|way_off|TODO|25|not found in osm file|
|rel_off|TODO|143|not found in osm file|
|parts|a list of id of all the member components e.g for bus route this is going to be a list of osm_id of of stations|"{474409457,2591193489,340147841(...)"|`<member type="node" ref="474409457" role="stop"/><member type="way" ref="255736141" role="platform"/>`|
|members|same list as parts but with more information with the shema: 1) Type of component concate with the id + role tag |"{n474409457,stop,w255736141,platform,n2591193489,stop (...)"|same as above|
|tags|a list of used tags and their values|"{from,"Darmstadt Ludwigshöhstraße",name,"Bus U: Darmstadt Ludwigshöhstraße => Urberach",network,RMV(...)}"|`<tag k="type" v="route"/>	<tag k="route" v="bus"/>`|

# Explaination of Tables in Database
| Table Name | Description |
|------------|-------------|
|`planet_osm_line`|contains ALL imported ways and contains all 69 attributes. Key is `osm_id`
|`planet_osm_nodes`|is a light version of `planet_osm_point` and contains only 3 attributes `id`, `lat`, `long`|
|`planet_osm_point`|contains all important nodes with tags, this one is a full version with 69 attributes|
|`planet_osm_polygon`|contains all imported polygons. Relations seem to be resolved for that|
|`planet_osm_rels`| is a table of 6 attributes `id, way_off, rel_off, parts, members, tags` and describe all kind of things like hiking, bus, flixbus, rmv routes, restriction (such as speed limitation, turning prohibition). Example and explaination for each attribute follows|
|`planet_osm_roads`|contains a SUBSET of `planet_osm_line` suitable for rendering at low (weiter weg) zoom levels. `planet_osm_line` contains too many elements to render on overview maps|
|`planet_osm_ways`|contains the roads and streets in `planet_osm_line`, probably describes the same amount. The difference is that `planet_osm_ways` determines of which nodes a way is composed of. The 3 attributes are `id`(id = osm_id from `planet_osm_lines`) `nodes` (is a list of nodes of which a node is composed of) and `tags` (a list of used tags and their values). More information and examples follow|
|`spatial_ref_sys`|TODO: No Idea|

## Attribute Explaination:
#### planet_osm_rels
| Attribute | Explaination | Sample Database | Sample in OSM File|
|-----------|--------------|-----------------|-------------------|
|id|relation id in osm file|7043760|`<relation id="7043760"...>`|
|way_off|TODO|25|not found in osm file|
|rel_off|TODO|143|not found in osm file|
|parts|a list of id of all the member components e.g for bus route this is going to be a list of osm_id of of stations|"{474409457,2591193489,340147841(...)"|`<member type="node" ref="474409457" role="stop"/><member type="way" ref="255736141" role="platform"/>`|
|members|same list as parts but with more information with the shema: 1) Type of component concate with the id + role tag |"{n474409457,stop,w255736141,platform,n2591193489,stop (...)"|same as above|
|tags|a list of used tags and their values|"{from,"Darmstadt Ludwigshöhstraße",name,"Bus U: Darmstadt Ludwigshöhstraße => Urberach",network,RMV(...)}"|`<tag k="type" v="route"/>	<tag k="route" v="bus"/>`|

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
| Attribute | Explaination | Sample Database | Sample in OSM File|
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
http://wiki.openstreetmap.org/wiki/Osm2pgsql/schema#Tables_Created
https://gis.stackexchange.com/questions/37099/in-osm2pgsql-how-is-the-planet-osm-roads-table-populated

# Helpful Information:
- Examples and Meaning of TAGS, KEYS...
https://taginfo.openstreetmap.org/
- How to list all Database using psql
https://dba.stackexchange.com/questions/1285/how-do-i-list-all-databases-and-tables-using-psql
- planet_osm_roads: contains a SUBSET of planet_osm_line suitable for rendering at low (weiter weg) zoom levels. planet_osm_line contains too many elements to render on overview maps.

planet_osm_ways
	- id: id von der Strasse
	- nodes: eine Liste aus Knoten, aus welchen diese Strasse besteht in Form {4645499195,4645499194,4645059531,4645059532,4645059533,4645059534,4645059536}
	- tags: eine Liste aus verwendeten Tags und deren Werten in Form: {barrier,fence,fence_type,metal}
	Code im osm Datei wäre:

	<way id="470309745" version="2" timestamp="2017-01-30T20:14:02Z" changeset="45663381" uid="380943" user="Heidas">
		<nd ref="4645499195"/>
		<nd ref="4645499194"/>
		<nd ref="4645059531"/>
		<nd ref="4645059532"/>
		<nd ref="4645059533"/>
		<nd ref="4645059534"/>
		<nd ref="4645059536"/>
		<tag k="barrier" v="fence"/>
		<tag k="fence_type" v="metal"/>
	</way>

# Source:
- http://wiki.openstreetmap.org/wiki/Osm2pgsql/schema#Tables_Created
- https://gis.stackexchange.com/questions/37099/in-osm2pgsql-how-is-the-planet-osm-roads-table-populated

# Helpful Information:
- Examples and Meaning of TAGS, KEYS...
https://taginfo.openstreetmap.org/
- How to list all Database using psql
https://dba.stackexchange.com/questions/1285/how-do-i-list-all-databases-and-tables-using-psql
