#!/bin/sh
sed -i "s/<\/settings>/<mirrors><mirror><id>insecure-repo<\/id><mirrorOf>p2Repo<\/mirrorOf><url>http:\/\/updates.gama-platform.org\/1.9.3<\/url><blocked>false<\/blocked><\/mirror><mirror><id>insecure-repoip<\/id><mirrorOf>p2Repo<\/mirrorOf><url>http:\/\/$SSH_HOST<\/url><blocked>false<\/blocked><\/mirror><mirror><id>insecure-repoexp<\/id><mirrorOf>p2Repo<\/mirrorOf><url>http:\/\/updates.gama-platform.org\/experimental\/1.9.3<\/url><blocked>false<\/blocked><\/mirror><mirror><id>insecure-buchen-maven-repo<\/id><mirrorOf>buchen-maven-repo<\/mirrorOf><url>http:\/\/buchen.github.io\/maven-repo<\/url><blocked>false<\/blocked><\/mirror><mirror><id>maven-default-http-blocker<\/id><mirrorOf>external:http:*<\/mirrorOf><url>http:\/\/0.0.0.0\/<\/url><blocked>false<\/blocked><\/mirror><\/mirrors><\/settings>/" ~/.m2/settings.xml