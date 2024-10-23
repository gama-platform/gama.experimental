/**
* Name: readinputfields
* Based on the internal empty template. 
* Author: baptiste
* Tags: 
*/


model readinputfields

/* Insert your model definition here */

global {
	init{
		create big_brain;
	}
	
	
}

species big_brain skills:[predicting] {
	
	file my_model <- file('../includes/single_audit_kmeans.xml');
	
	init {
		do load_evaluator(my_model);
		
	}
	
	reflex what_can_i_do {
		
		write "=========== LOADED MODELS ===========";
//		loop m over:models{
//			write m;
//		}
	}
	
}

experiment b{
	
}

