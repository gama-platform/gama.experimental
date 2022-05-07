/**
* Name: Test connection
* Author: Benoit Gaudou
* Description: Model testing the connection to R. 
* 		It expresses the various configurations steps the user should follow to install the connection between R and GAMA.
* 		In particular the installation of the rJava package in R, the definition of the (system-wide) environment variables.
* Tags: R, connection
*/

model Testconnection

global skills: [RSkill] {
	
	init{
		write "Before running this model, you should install the rJava library in R." color: #red;
		write "In the R (RStudio) console, write:";
		write "'install.packages(\"rJava\")' to install the library,";
		write "'library(rJava)', to check the install is correct.";
		
		write "\nNote on MacOSX, in case of troubles in recent versions of MacOXS, you should first write in a terminal:" color:#red;
		write "R CMD javareconf";
		write "sudo ln -f -s $(/usr/libexec/java_home)/jre/lib/server/libjvm.dylib /usr/local/lib";
		write "";
		
		write "Configure the Environment Variable R_HOME (depenging on your OS)." color: #red;
		write "On MACOSX, you need to create (or update) the file 'environment.plist' in the folder: ";
		write "'~/Library/LaunchAgents/' (for the current user, note that this folder is a hidden folder) or in '/Library/LaunchAgents/' (for all users)";
		write "It should look like:";
		write "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" color: #black;
		write "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" color: #black;
		write "<plist version=\"1.0\">" color: #black;
		write "  <dict>" color: #black;
		write "    <key>Label</key>" color: #black;
		write "    <string>my.startup</string>" color: #black;
		write "    <key>ProgramArguments</key>" color: #black;
		write "    <array>" color: #black;
		write "      <string>sh</string>" color: #black;
		write "      <string>-c</string>" color: #black;
		write "      <string> launchctl setenv R_HOME /Library/Frameworks/R.framework/Resources/ </string>" color: #black;
		write "    </array>" color: #black;
		write "    <key>RunAtLoad</key>" color: #black;
		write "    <true/>" color: #black;
		write "  </dict>" color: #black;
		write "</plist>" color: #black;	
		write "";
		
		write "In GAMA 1.8.2, you need to specify the path to the R connector library in the GAMA launching arguments."  color: #red;
		write "To this purpose, you need to add to either (1) the GAMA.ini file if you use the release version of GAMA, or (2) to the launching configuration (if you use the source code version)" color:#red;
		write "the following line: (replace PATH_TO_R by the path to R, i.e. the value in $R_PATH)" color: #red;
		write " on Mac:      -Djava.library.path=PATH_TO_R/library/rJava/jri/rlibjri.jnilib";
		write " on Windows:  -Djava.library.path=PATH_TO_R/library/rJava/jri/jri.dll";
		write " on Ubuntu:   -Djava.library.path=PATH_TO_R/library/rJava/jri/libjri.so";
		write "";
		write "As an example, under macOS, you need to add:";
		write "-Djava.library.path=/Library/Frameworks/R.framework/Resources/library/rJava/jri/";
		
		
		do startR;
		
		write "Connection to R succesful!" color: #darkgreen;
	}
	
}
experiment RJava type: gui {}
