/**
* Name: Eval a R script
* Author: Benoit Gaudou
* Description: Model illustrating the evaluation of a R script in GAML, using R_eval operator.
*    This requieres to loop over the text file lines and to execute each line of the script one by one.
* Tags: R, script
*/

model EvalRScript

global skills:[RSkill]{
	file Rcode <- text_file("../includes/rScript_with_loop.txt");
	
	init{
		do startR;

		// Loop that takes each line of the R script and display it.
	 	loop s over: Rcode.contents{
			write "R>"+s color: (s index_of("#") = 0) ? #green : #darkblue;
		}
		
		// Execute the script in the file Rcode as a whole.		
		write R_eval_script(Rcode.path);
		
		// Get values of the variables defined in the script
		write "----";
		write sample(R_eval("x")) + " - x should be equals to 5";
		write sample(R_eval("y")) + " - y should be equals to 2";
		write sample(R_eval("s1")) + " - s1 should be equals to 22";
		write sample(R_eval("s")) + " - s should be equals to 192";		
	}	
}

experiment RJava type:gui {}