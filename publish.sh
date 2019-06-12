#!/bin/bash

deploy(){	
cd msi.gama.experimental.p2updatesite &&
mvn -U clean install -P p2Repo --settings ../settings.xml && 
cd -
}


MESSAGE=$(git log -1 HEAD --pretty=format:%s)
echo $MESSAGE
if  [[ ${MESSAGE} == *"ci ext"* ]]; then			
	MSG+=" ci ext " 
fi	

if [[ "$TRAVIS_EVENT_TYPE" == "cron" ]] || [[ $MSG == *"ci cron"* ]]; then 	
	
	deploy
 
else
	if  [[ ${MESSAGE} == *"ci deploy"* ]] || [[ $MSG == *"ci deploy"* ]]; then		
		if  [[ ${MESSAGE} == *"ci clean"* ]] || [[ $MSG == *"ci clean"* ]]; then
			clean
			MSG+=" ci ext "
			echo $MSG
		fi 
		deploy 
	fi
fi