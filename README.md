[![Build Status](https://jenkins.vincinator.de/buildStatus/icon?job=bp17)](https://jenkins.vincinator.de/job/bp17)

## Introduction
Server exposing an HTTP API for collecting routing relevant informations.

## Dependencies

- jersey
- maven
- jackson
- hibernate
- postgresql (can be changed to an other Database supported by hibernate)


## IntelliJ Integration 
Setup Tomcat 

1) mvn clean package


## Docker Deploy

In order to install the server into a Docker container, you can use the Dockerfile provided in this repo install/docker.


