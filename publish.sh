#!/bin/bash
cd msi.gama.experimental.p2updatesite &&
mvn -U clean install -P p2Repo --settings ../settings.xml && 
cd -