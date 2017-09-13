#!/bin/bash
osmosis --read-pgsql host="localhost" database="osm" user="postgres" password="password" outPipe.0=pg --dd inPipe.0=pg outPipe.0=dd --write-xml inPipe.0=dd file=~/export.osm
