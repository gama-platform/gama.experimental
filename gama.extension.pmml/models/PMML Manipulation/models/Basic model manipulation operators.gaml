/**
* Name: readinputfields
* Based on the internal empty template. 
* Author: baptiste
* Tags: 
*/


model readinputfieldswithoperator

/* This model is showcasing the multiple operators that can be used to manipulate PMML models */
global {
	
	
	string model_path <- "../includes/svm_iris.pmml";
	
	init{
		
			
		write "First we load the model located at: " + model_path;
		evaluator eval <- load_eval_prediction(model_path);
		if eval != nil{
			write "Loading successful";
		}
		else {
			write "Something failed while loading the model, please check the path and the format";
			return;
		}

		write "If the model has some tests included, we can run them to check that they are still working inside of GAMA: " + verify_evaluator(eval);
		
		list<string> input_fields <- get_input_field_names(eval);
		write "To make an evaluation, the model require those input fields: " + input_fields;

		list<string> output_fields <- get_output_field_names(eval);
		write "The output fields will be: " + output_fields;
		
		list<string> target_fields <- get_target_field_names(eval);
		write "The target fields will be: " + target_fields;
		
		
		let random_input <- input_fields as_map (each::rnd(0.0,5.0));
		write "We generated those random inputs: " + random_input;
		
		write "Evaluating the model with them gives us this map: " + evaluate(eval, random_input);
			
	}
	
	
}

experiment b{
	parameter "Model file" var:model_path;
}





