#!/bin/sh


 
sed -i "s/<\/settings>/<mirror><id>insecure-repo<\/id><mirrorOf>p2Repo<\/mirrorOf><url>http:\/\/51.255.46.42\/updates<\/url><blocked>false<\/blocked><\/mirror><\/settings>/" ~/.m2/settings.xml