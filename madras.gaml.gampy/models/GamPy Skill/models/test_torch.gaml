/**
* Name: testtorch
* Based on the internal empty template. 
* Author: hdang
* Tags: 
*/


model testtorch

/* Insert your model definition here */
global {
	int nb_columns <- 4;
	
	init {
		create dl_agent number: 1 {
			model_type <- "pytorch";
			model_path <- "../includes/iris.onnx";
		}
	}
}

species dl_agent skills: [gampy] {
	reflex predict {
		list<list<float>> input_data;
		loop i from: 0 to: 10 {
			list<float> data;
			loop j from: 0 to: nb_columns - 1 {
				data <- data + rnd(1.0);
			}
			input_data <- input_data + [data];
		}
		
		list output <- predict(input_value: input_data, nb_features: nb_columns, input_var: "input", output_var: "output");
		write output;
	}
}

experiment load_keras {
}
