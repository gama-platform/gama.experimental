#!/bin/sh


search='</settings>'
replace='<mirror><id>insecure-repo</id><mirrorOf>external:http:*</mirrorOf><url>http://51.255.46.42/updates</url><blocked>false</blocked></mirror></settings>'
sed -i "s/<\/settings>/<mirror><id>insecure-repo<\/id><mirrorOf>external:http:*<\/mirrorOf><url>http:\/\/51.255.46.42\/updates<\/url><blocked>false<\/blocked><\/mirror><\/settings>/" settings.xml