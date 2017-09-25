#!/bin/bash




mvn package
mv target/ROOT.war install/docker/ROOT.war

cd install/docker

docker stop bpserver || true
docker rm bpserver || true

echo 'DEBUG: Start Dockerbuilding'

# In order to test the server, you need geodata. 
# For Development and Testing purposes we have limited our geodata to germany/hessen
wget http://download.geofabrik.de/europe/germany/hessen-latest.osm.pbf

# In order to Export the database, the ExportTool.jar must be compiled.
# https://github.com/skisssbb/BP-ExportTool/
git clone https://github.com/skisssbb/BP-ExportTool/
cd BP-ExportTool
mvn clean package
mv target/ExportTool.jar ../
cd ..

docker build -t bpserver:latest . 
echo 'DEBUG: End Dockerbuilding'

docker run --name bpserver -p 80:8080 -d -it bpserver:latest
docker exec -d bpserver service postgresql start
docker exec -d bpserver service tomcat8 start


