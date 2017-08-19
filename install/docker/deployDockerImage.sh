#!/bin/bash
docker stop bpserver || true
docker rm bpserver || true
pwd
ls -la
echo 'DEBUG: Start Dockerbuilding'
docker build -t bpserver:latest .
echo 'DEBUG: END Dockerbuilding'
docker run --name bpserver -p 8082:8080 -d -it bpserver:latest
docker exec -d bpserver service postgresql start
docker exec -d bpserver service tomcat8 start
