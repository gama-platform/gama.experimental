/**
* Name: Testconnection
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model Testconnection

global skills:[RSkill]{
	
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
		write "On MACOSX, in the file .bash_profile, add the line 'export R_HOME=\"/Library/Frameworks/R.framework/Resources/\"'";
		write "";
		write "In GAMA, you should change the Preferences to set the path to RScript to the file:" color: #red;
		write " on Mac:      $R_HOME/library/rJava/jri/rlibjri.jnilib";
		write " on Windows:  $R_HOME/library/rJava/jri/jri.dll";
		write " on Ubuntu:   $R_HOME/library/rJava/jri/libjri.so";
		
		do startR;
	}
	
}
experiment RJava type:gui{
	output{
	}
}
