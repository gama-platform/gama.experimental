/**
* Name: R_eval
* Author: Benoit Gaudou
* Description: Model illustrating the R evaluation of some simple expresions.
* Tags: R, R_eval
*/

model Reval

global skills: [RSkill] {
	
	init{
		do startR;
		
		write R_eval("x<-1");
		write R_eval("rnorm(50,0,5)");
	}
	
}

experiment RJava type: gui {}