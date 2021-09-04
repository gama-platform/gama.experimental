#!/bin/sh


 
sed -i "s/<\/settings>/<mirrors><mirror><id>insecure-repo<\/id><mirrorOf>p2Repo<\/mirrorOf><url>http:\/\/51.255.46.42\/updates/v1.8.2<\/url><blocked>false<\/blocked><\/mirror><mirror><id>insecure-buchen-maven-repo<\/id><mirrorOf>buchen-maven-repo<\/mirrorOf><url>http:\/\/buchen.github.io\/maven-repo<\/url><blocked>false<\/blocked><\/mirror><\/mirrors><\/settings>/" ~/.m2/settings.xml