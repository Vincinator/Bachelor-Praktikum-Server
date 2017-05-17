## Introduction
Dummy Jearsey Server is running on Grizzly 2 based on HTTP Protocol.
Requirement is that you have installed Maven on your system.

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



