/**
* Name: Localization
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model Localization

global {
	shape_file buildings_residential_shape_file <- shape_file("../includes/shp/buildings_residential.shp");
	shape_file Rouen_iris_shape_file <- shape_file("../includes/shp/Rouen_iris_number.shp");
	grid_file occsol_rouen_grid_file <- grid_file("../includes/raster/occ-sol_rouen2.tif");
	geometry shape <- envelope(occsol_rouen_grid_file);
	

	string number_property <-  "POP";
	
	init {
		create building from: buildings_residential_shape_file;
		create iris from: Rouen_iris_shape_file;
		
		generate species:people number: 10000 
		from:[csv_file("../includes/Age & Sexe-Tableau 1.csv",";"), csv_file("../includes/Rouen_iris.csv",";")] 
		attributes:["Age"::["Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
					  				"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
									"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
									"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"],
						"Sexe"::["Hommes", "Femmes"]];
		
		
		localize species: people nests: building nest_attribute: "nest" max_dist_loc: 500.0 step_dist_loc: 100.0 distribution: "area" 
			mapper:["entities"::iris, "data_id"::number_property, "fields"::[field(occsol_rouen_grid_file)]]; 
	}
}

species building {
	aspect default {
		draw shape color: #gray;
	}
} 


species iris {
	string CODE_IRIS;
	aspect default {
		draw shape color: #lightgray;
	}
}

species people {
	int Age;
	geometry nest;
	string Sexe;
	string iris_id;
	rgb color <- #red;
	
	aspect default {
		draw circle(15) color: color;
	}
}

experiment Localization type: gui {
	output {
		display map {
			species iris;
			species building;
			species people;
		}
	}
}
