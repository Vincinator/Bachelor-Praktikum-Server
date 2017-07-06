#!/bin/bash
docker rm bpserver || true
docker build -t bpserver:latest .
docker run --name bpserver -p 8082:8080 -d -it bpserver:latest
docker exec -d bpserver service tomcat8 start