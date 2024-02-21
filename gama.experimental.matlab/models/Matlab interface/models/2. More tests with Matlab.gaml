/***
* Name: MatlabAgent
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model MatlabAgent

global {
	matlabb engine;
	init {
		create matlabb number: 1;
		engine <- first(matlabb);
		
		write "============== Test Engine access ==============";
		write engine.test_engine();

		do test_basic;
		do test_list_matrix;	
		do test_file;
	}

	action test_basic {
		write "============== Basic tests ==============";
		
		ask engine {
			do eval("T2 = table2array(table([38;49],[71;64]));");		
			do eval("a=true;");
			do eval("b=3.12;");
		}	

		unknown t2 <- engine.value_of("T2");
		unknown a <- engine.value_of("a");
		unknown b <- engine.value_of("b");
		
		write sample(t2)  + " - type = " + type_of(t2);
		write sample(a) + " - type = " + type_of(a);
		write sample(b)  + " - type = " + type_of(b);
	}

	action test_list_matrix {
		write "";
		write "============== Test List Matrix ==============";		
		
		ask engine {
			do eval("IC=[0;45];");
			do eval("IC2=[0 45];");
			do eval("As = [1,3;4,6;8,9];");
			do eval("As2 = [1 3;4 6;8 9];");
		}		
		
		unknown ic  <- engine.value_of("IC");
		unknown ic2 <- engine.value_of("IC2");		
		unknown As 	<- engine.value_of("As") ;
		unknown As2 <- engine.value_of("As2") ;
		
		write sample(ic)  + " - type = " + type_of(ic);
		write sample(ic2) + " - type = " + type_of(ic2);
		write sample(As)  + " - type = " + type_of(As);
		write sample(As2) + " - type = " + type_of(As2);
		write sample(matrix(As).rows);
		write sample(matrix(As).columns);	
		write sample(matrix(As2).rows);
		write sample(matrix(As2).columns);			
	}
	
	action test_file {
		write "";
		write "============== Test Files scripts ==============";		
		
		matlab_file fcts_matlab_file <- matlab_file("../includes/fcts_matlab.m");
		matlab_file vars_fcts_matlab_file <- matlab_file("../includes/vars_fcts_matlab.m");

		ask engine {
			do eval file: fcts_matlab_file;
			do eval file: vars_fcts_matlab_file;
		}
		write engine.value_of("z");
		write engine.value_of("n_renew");

	}	
}



species matlabb parent: agent_MATLAB {
	string path_to_matlab <- "/Applications/MATLAB_R2019a.app/bin/maci64/";
}

experiment MatlabAgent type: gui { }
