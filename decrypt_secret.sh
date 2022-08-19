#!/bin/sh
gpg --quiet --batch --yes --decrypt --passphrase="$SSH_USER_PWD" --output settings_auth.xml settings_auth.xml.gpg


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
