/**
* Name: ImportcityJSon
* Based on the internal skeleton template. 
* Author: patricktaillandier
* Tags: 
*/

model ImportcityJSon

global {
	string lod <- "lod1" among:["lod1", "lod2"] parameter: true on_change: change_lod;
	//cityjson_file cube_cityjson_file <- cityjson_file("../includes/cube.city.json");
	//cityjson_file cube_cityjson_file <- cityjson_file("../includes/twocube.city.json");
	//cityjson_file cube_cityjson_file <- cityjson_file("../includes/twobuildings.city.json");
	cityjson_file cube_cityjson_file <- cityjson_file("../includes/LoD3_Railway.city.json");
	
	geometry shape <- envelope(cube_cityjson_file);
	
	init {
		create object from: cube_cityjson_file;
	}
	string lodStr;
	action change_lod {
		ask object {
			if (shape.attributes[lod] != nil) {
				to_display <- shape.attributes[lod];
			
			}
			ask experiment {
				do update_outputs;
			}
		}
	}
	/** Insert the global definitions, variables and actions here */
}

species object {
	rgb color <- rnd_color(255);
	geometry to_display <- shape;
	aspect default {
		draw to_display color: color;
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
