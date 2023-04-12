/**
* Name: Eval a R script
* Author: Benoit Gaudou
* Description: Model illustrating the evaluation of a R script in GAML, using R_eval operator.
*    This requieres to loop over the text file lines and to execute each line of the script one by one.
* Tags: R, script
*/

model EvalRScript

global skills:[RSkill]{
	file Rcode <- text_file("../includes/rScript.txt");
	
	init{
		do startR;

		// Loop that takes each line of the R script and execute it.
	 	loop s over: Rcode.contents{
			unknown a <- R_eval(s);
			write "R>"+s color: (s index_of("#") = 0) ? #green : #darkblue;
			write a;
		}
		
		// Note execution line after line does not work when the script contains loops, conditionnals, function definitions ...
		// in this case use the operator R_eval_script instead
	}	
}

experiment RJava type:gui {}