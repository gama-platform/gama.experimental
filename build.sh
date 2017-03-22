cd ../ummisco.gama.annotations &&
mvn clean install &&
cd - &&
cd ../msi.gama.processor &&
mvn clean install &&
cd - && cd msi.gama.experimental.parent &&
mvn clean install
