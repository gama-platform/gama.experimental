/**
* Name: Localization
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model Localization

global {
	shape_file roads_shape_file <- shape_file("../includes/shp/roads.shp");
	geometry shape <- envelope(roads_shape_file);
	list<geometry> geoms;
	init {
		generate species:people number: 5000 
		from:[csv_file("../includes/Age & Sexe-Tableau 1.csv",";"), csv_file("../includes/Rouen_iris.csv",";")] 
		attributes:["Age"::["Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
					  				"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
									"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
									"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"],
						"Sexe"::["Hommes", "Femmes"]];
		
		localize species: people nests: roads_shape_file distribution: "area" nest_attribute: "nest" min_dist: 10 max_dist: 50.0 ;	
		geoms <- remove_duplicates(people collect each.nest);
	}
} 

species people {
	int Age;
	geometry nest;
	string Sexe;
	rgb color <- #red;
	
	aspect default {
		draw circle(15) color: color;
	}
}

experiment Localization type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		display map {
			graphics "nest"{
				loop g over: geoms {
					draw g color: #gray;
				}
			}
			image "Roads" gis:roads_shape_file color: #blue ;
			
			species people;
		}
	}
}
