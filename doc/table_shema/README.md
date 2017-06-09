#Explaination of Tables in Database
- planet_osm_line: contains ALL imported ways
- planet_osm_point: contains all imported nodes with tags
- planet_osm_polygon: contains all imported polygons. Relations seem to be resolved for that.
- planet_osm_roads: contains a SUBSET of planet_osm_line suitable for rendering at low (weiter weg) zoom levels. planet_osm_line contains too many elements to render on overview maps.

Slim Tables (slim, small version):

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

 
planet_osm_nodes: ist eine light Version und umfasst alle Knoten von Tablelle planet_osm_point. Diese hat jedoch nur 3 Attributen
	- id
	- lat
	- long
spatial_ref_sys

##Source:
http://wiki.openstreetmap.org/wiki/Osm2pgsql/schema#Tables_Created
https://gis.stackexchange.com/questions/37099/in-osm2pgsql-how-is-the-planet-osm-roads-table-populated

##Helpful Information:
- Examples and Meaning of TAGS, KEYS...
https://taginfo.openstreetmap.org/
- How to list all Database using psql
https://dba.stackexchange.com/questions/1285/how-do-i-list-all-databases-and-tables-using-psql
