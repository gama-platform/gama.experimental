//Allow to test the definition of mapper number for the localization of agents
// 2 ways of defining these numbers: through vector geometries or throgh a field 
model Localization

global {
	bool field_number_mapper <- true parameter: true;
	field field_from_matrix;
	
	init {
		create building with: (shape: square(20) at_location {25,50});
		create building with: (shape: square(20) at_location {75,50});
		create people number: 100;
		
		if (not field_number_mapper) {
			create area with:(shape:rectangle(45,90) at_location {25,50}, number: 10);
			create area with:(shape:rectangle(45,90) at_location {75,50}, number: 90);
			// use the area entities to define the number of agents to located in the different area - id of th number property given by data_id
			localize species: people nests: building nest_attribute: "nest" mapper: ["entities"::area, "data_id"::"number"] ;	
		} else {
			//build a field from a 1x1 matrix with 9999.0 as noData
			field_from_matrix  <- field(matrix([[10],[90]]), 9999.0);
			// use a field to define the number of agents to located in the different area
			
			localize species: people nests: building nest_attribute: "nest" mapper: ["field"::field_from_matrix] ;	
		}
	}	
} 


species area {
	int number;
	aspect default {
		draw shape color: #skyblue;
	}
}


species building {
	aspect default {
		draw shape color: #gray;
	}
}

species people {
	int Age;
	geometry nest;
	string Sexe;
	string iris;
	rgb color <- rnd_color(255);
	
	aspect default {
		draw circle(0.2) color: color;
	}
}

experiment Localization type: gui {
	output {
		display map {
			graphics "field" {
				if (field_from_matrix != nil) {
					draw image(field_from_matrix) ;
				}
			}
			species area;
			species building;
			species people;
		}
	}
}
