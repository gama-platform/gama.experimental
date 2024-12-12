/**
* Name: Classification
* Based on the internal empty template. 
* Author: baptiste
* Tags: 
*/

/* This model loads a classification model saved in PMML and uses it 
 * to predict the class of some data.
 * The predicted data will be the class to which a point belong in the
 * simulation, according to a spatial clustering.
 * The point to classify is the user's mouse location
 */
model InteractiveClassification

global {
	
	csv_file locations_and_classes_csv_file <- csv_file("../includes/locations_and_classes.csv");

	
	map<int, rgb> class_to_color <- [0::#red, 1::#green, 2::#blue ];
	
	// bounds of the data, preprocessed to get the correct world 
	// shape and make some basic transformations
	float x_min <- 416.82026114139194; 
	float x_max <- 698.5410826464473;
	float y_min <- 201.32880359556793;
	float y_max <- 472.0393039381411;
	float width <- x_max - x_min;
	float height <- y_max - y_min;
	
	geometry shape <- rectangle(width, height);
	
	// This object will contain our trained model stored in the PMML file
	evaluator trained_model;
	// This addition data point represents the current location of 
	// the user's mouse and its predicted class
	user_mouse mouse;
	
	init{
		
		//we extract the initial data and create one simulation object for each point
		create items from:locations_and_classes_csv_file with:[class::int(read("color"))]{
			location <- {float(read("x")) - x_min, float(read("y")) - y_min, 0};
		}	
		
		// we create an additional point in the simulation to represent the mouse
		create user_mouse{
			mouse <- self;
		}
		
		// we load the classification model that was trained on this data
		trained_model <- load_eval_prediction("../includes/clustering.pmml");
		// just to check that everything is fine we display the expected input and output field names
		write "The model has been loaded, the expected input fields are: " + get_input_field_names(trained_model);
		write "And it will output a map containing those fields: " + get_output_field_names(trained_model);
		
	}
	

}

// These items represent the initial training points 
// they have a location (built-in the agents) and a class
// they will be displayed at their initial location with a different color for each class
species items {
	int class;
	
	geometry shape <- circle(1);
	
	aspect default{
		draw shape color: class_to_color[class] at:location;
	}
}

// An additional data point representing current user's mouse
// The only thing that differs from objects of type items will be its shape
species user_mouse parent:items{
	
	geometry shape <- triangle(5);
}


experiment test {
	output{
		display main type:2d{
			species items;
			species user_mouse;
			
			// when the use hovers the display, we create an additional point 
			// located at the position of the user's mouse
			// and ask the trained model to pick the right class 
			// depending on this location
			event #mouse_move {
				// get the mouse location into a map to feed it to the trained model
				point loc <- #user_location;
				map input <- ["x":: loc.x + x_min, "y"::loc.y+y_min];
				//Here we ask the model to predict a class based on that location
				map estimated_class <- evaluate(trained_model, input);
				
				// finally we extract the data that is of interest
				// for us, the "cluster" field, and use it to modify our
				// user_mouse object
				ask world{
					mouse.location <- loc;
					mouse.class <- int(estimated_class["cluster"]);
				}
			}
		}
	}
}

// This experiment aims at checking the execution time of predicting
// by generating a big amount of random input fields and 
// measuring the time it takes for gama to predict an output for each
experiment benchmarking {
	int nb_predictions <- 10000;
	parameter "Number of prediction to do" var:nb_predictions;

	reflex check_duration{
		//First we generate random inputs for the model to do predictions on 
		list<map<string, float>> points <- [];
		loop times:nb_predictions{
			points <+ map("x"::rnd(x_min, x_max) , "y"::rnd(y_min, y_max));
		}
		
		let start <- gama.machine_time;
		loop p over:points{
			let l <- evaluate(trained_model, p);
		}
		let end <- gama.machine_time;
		write "Duration for " + nb_predictions + " evaluations: " + int(end-start) + "ms";
	}

}




