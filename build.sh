travis encrypt -r hqnghi88/gamaExpClone "CI_DEPLOY_USERNAME=hqnghi88" &&
travis encrypt -r hqnghi88/gamaExpClone "CI_DEPLOY_PASSWORD=23111988" &&
cd msi.gama.experimental.parent &&
mvn clean install && cd - 
