#!/bin/bash

deploy(){	

bash $GITHUB_WORKSPACE/decrypt_secret.sh

header=$(<$GITHUB_WORKSPACE/settings_header.xml)
core=$(<$GITHUB_WORKSPACE/settings_auth.xml)
footer=$(<$GITHUB_WORKSPACE/settings_footer.xml) 
echo "$header $core $footer"> $GITHUB_WORKSPACE/settings.xml


echo "DEPLOY" 
mkdir -m 0700 -p ~/.ssh 
chmod 750 ~
chmod 700 ~/.ssh 
echo -e "Host *\n" >> ~/.ssh/config 
echo -e "IdentitiesOnly yes\n" >> ~/.ssh/config 
echo -e "Port 22\n" >> ~/.ssh/config 
echo -e "StrictHostKeyChecking no\n" >> ~/.ssh/config 
echo -e "PubkeyAuthentication no\n" >> ~/.ssh/config 
cat ~/.ssh/config 
ssh-keyscan -H $SSH_HOST >> ~/.ssh/known_hosts

cd msi.gama.experimental.p2updatesite &&
mvn -U clean install -P p2Repo --settings ../settings.xml && 
cd -
}

deploy
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