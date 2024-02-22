/**
* Name: ImportcityJSon
* Based on the internal skeleton template. 
* Author: patricktaillandier
* Tags: 
*/

model ImportcityJSon

global {
	cityjson_file cube_cityjson_file <- cityjson_file("../includes/cube.city.json");
	//cityjson_file cube_cityjson_file <- cityjson_file("../includes/twocube.city.json");
	geometry shape <- envelope(cube_cityjson_file);
	init {
		create object from: cube_cityjson_file;
	}
	/** Insert the global definitions, variables and actions here */
}

species object {
	rgb color <- rnd_color(255);
	aspect default {
		draw shape color: color;
	}
}

experiment ImportcityJSon type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		display map type:3d {
			species object;
		}
	}
}
