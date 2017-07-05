#!/bin/bash

docker build -t testimage:latest .
docker run testimage 
