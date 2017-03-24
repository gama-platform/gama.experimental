#!/bin/bash
cd msi.gama.experimental.p2updatesite &&
mvn clean install -X -P p2Repo && 
cd -