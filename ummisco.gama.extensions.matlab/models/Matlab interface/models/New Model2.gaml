/***
* Name: NewModel2
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model NewModel2

global {
	matlab_engine eng;



	init {
		eng <- get_matlab_engine(false);
		unknown T2 <- eng eval("T2 = table2array(table([38;49],[71;64]));","T2");		
		
		write sample(T2);
		
		unknown aTrue <- eng eval("a=true;","a");
		write sample(aTrue) + " - " + type_of(aTrue);
		
		unknown bFloat <- eng eval("a=3.12;","a");
		write sample(bFloat) + " - " + type_of(bFloat);
		
	}

	action listMatrix {
		unknown ic <- eng eval("IC=[0;45];","IC");
		unknown ic2 <- eng eval("IC2=[0 45];","IC2");
		
		unknown As <- eng eval("As = [1,3;4,6;8,9];","As") ;
		unknown As2 <- eng eval("As2 = [1 3;4 6;8 9];","As2") ;
		
		write sample(ic);
		write sample(ic2);
		write sample(As);
		write sample(As2);
		write sample(matrix(As).rows);
		write sample(matrix(As).columns);	
		write sample(matrix(As2).rows);
		write sample(matrix(As2).columns);			
	}

	action benchmark_Matlab {
		unknown m;
		unknown mAsync;	
		float D;	
		
		benchmark {
			write "Test connection" + test_matlab(true);
		}
		
		benchmark {			
			eng <- get_matlab_engine(false);
			write "Get Matlab Engine";
		}	
		benchmark repeat: 100 {
			m <- eng eval("B=1+3;","B");
		//	write "eval";
		}
		benchmark repeat: 100 {
			mAsync <- eng eval("C=1+3;","C");
		//	write "eval Async";
		}
		benchmark repeat: 100{
			D <- eng eval("D=1+3;",D);
		// 	write "eval Type";
		}
					
			write m;
			write mAsync;
			write D;
			write type_of(m);
			write type_of(mAsync);
			write type_of(D);
			
			
		benchmark {
			bool b <- close_matlab_engine(eng,false);				
		}			
	}
	
	action test_file {
		matlab_file test_110620190_matlab_file_file <- matlab_file("../includes/test_11062019.m");

		write first(test_110620190_matlab_file_file.contents);
		
		string scriptMatlabb <- string(first(test_110620190_matlab_file_file.contents));

			
		benchmark {
			unknown n <- eng eval(scriptMatlabb,"n_renew");
			write n;
		}
		
	}
}



experiment name type: gui {
	output { }
}