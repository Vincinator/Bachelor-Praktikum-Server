#!/bin/bash
echo "Installscript started"

APP_DB_PASS="password"
webapps_dir=/var/lib/tomcat8/webapps

echo "installing postgresql"
#apt-get update
#apt-get install -y postgresql postgresql-contrib sudo default-jre

###############################################################
#                           Postgresql
###############################################################
service postgresql start

echo "CREATE ROLE hibernateuser LOGIN ENCRYPTED PASSWORD '$APP_DB_PASS';" | sudo -u postgres psql
su postgres -c "createdb hibernatedb --owner hibernateuser"

service postgresql reload

###############################################################
#                           Tomcat
###############################################################


sudo rm -rf /var/lib/tomcat8/webapps/ROOT
cp /opt/ROOT.war $webapps_dir
chown tomcat8:tomcat8 $webapps_dir/ROOT.war

#service tomcat8 start

# For Development: the server will be started after everything is setup.
