/***
* Name: 1ComputeWithMatlab
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model ComputeWithMatlab

global {	
	matlab_file fcts_matlab_file <- matlab_file("../includes/fcts_matlab.m");
	
	matlab_agent matlab;

	init {
		create matlab_agent number: 1;
		matlab <- first(matlab_agent);
	
		ask matlab {
			do eval("T = 45 * 12;");
			do eval file: fcts_matlab_file;
		}
		
		write matlab.value_of("T");
		write matlab.value_of("z");	
		
	}
}

species matlab_agent parent: agent_MATLAB {
//	string path_to_matlab <- "/Applications/MATLAB_R2019a.app/bin/maci64/";
}

experiment ComputeWithMatlab type: gui { }
