# Docker Installation


## Vorraussetzungen

- docker
- container.jar (BP Server Anwendung)

RUN service postgresql start && \
    sudo -u postgres createuser -s hibernateuser && \
    sudo -u postgres createdb hibernatedb && \
    sudo rm -rf /var/lib/tomcat8/webapps/ROOT && \
    sudo cp /opt/ROOT.war /var/lib/tomcat8/webapps/ && \
    sudo chown tomcat8:tomcat8 /var/lib/tomcat8/webapps/ROOT.war && \

    service postgresql stop