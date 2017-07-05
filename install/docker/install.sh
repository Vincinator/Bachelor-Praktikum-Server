#!/bin/bash
echo "Installscript started"
$APP_DB_PASS="password"

echo "installing postgresql"
apt-get update
apt-get install -y postgresql postgresql-contrib sudo default-jre

service postgresql start

echo "Configure postgresql"


echo "CREATE ROLE hibernateuser LOGIN ENCRYPTED PASSWORD '$APP_DB_PASS';" | sudo -u postgres psql

su postgres -c "createdb hibernatedb --owner hibernateuser"

service postgresql reload

# java -jar /opt/container.jar
