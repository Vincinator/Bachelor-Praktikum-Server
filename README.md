qd18jemy-bp17-jerseyserver

Dummy Jearsey Server is running on Grizzly 2 based on HTTP Protocol.
Requirement is that you have installed Maven on your system.

To start, run following commands:
1) mvn clean compile
2) mvn exec:java
3) The Server can be reached on browser under: http://0.0.0.0:8081/myapp/myresource
To create a stand alone jar file for deploying, run following command:
1) mvn clean compile
2) mvn package
3) The jar file can be found under target dir