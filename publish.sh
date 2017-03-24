#!/bin/bash
cd msi.gama.experimental.p2updatesite &&
mvn clean deploy --settings ../settings.xml -DskipTests=true -B  && 
cd -