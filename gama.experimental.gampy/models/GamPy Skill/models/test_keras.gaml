/**
* Name: testkeras
* Based on the internal empty template. 
* Author: hdang
* Tags: 
*/


model testkeras

/* Insert your model definition here */

global {
	int nb_columns <- 4;
	
	init {
		create dl_agent number: 1 {
			model_type <- "keras";
			model_path <- "../includes/model.h5";
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
		
		list output <- predict(input_value: input_data, nb_features: nb_columns);
		write output;
	}
}

experiment load_keras {
}