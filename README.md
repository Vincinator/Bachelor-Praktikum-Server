[![Build Status](https://jenkins.vincinator.de/buildStatus/icon?job=bp17)](https://jenkins.vincinator.de/job/bp17)

## Introduction
Server exposing an HTTP API for collecting routing relevant informations.

## Dependencies

- postgresql
- grizzly2
- jersey
- maven


## Test the Server locally

1) mvn clean compile
2) mvn exec:java
3) A Demo resource is available at: http://localhost:8081/myapp/demobarriere



## Deploy the Server 
The Grizzly standalone jar file is deployed and runs on our server. 
Just commit to the Master Branch in order to deploy a new version to the server.

## CI Guide
To create a stand alone jar file for deploying, run following command:
1) mvn clean compile
2) mvn package
3) The jar file can be found under target dir



