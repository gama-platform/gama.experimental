/**
* Name: Localization
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model Localization

global {
	shape_file Rouen_iris_shape_file <- shape_file("../includes/shp/Rouen_iris.shp");
	geometry shape <- envelope(Rouen_iris_shape_file);
	string code_iris <- "CODE_IRIS";
	
	list<string> liste_iris <- remove_duplicates(Rouen_iris_shape_file collect (each.attributes[code_iris]));
	
	init {
		generate species:people number: 10000 
		from:[csv_file("../includes/Age & Sexe-Tableau 1.csv",";"), csv_file("../includes/Rouen_iris.csv",";")] 
		attributes:["Age"::["Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
					  				"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
									"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
									"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"],
						"Sexe"::["Hommes", "Femmes"]];
		
		//@Kevin: SOLUTION TEMPORAIRE A CORRIGER
		ask people {
			iris <- one_of(liste_iris);
		}
		localize species: people nests: Rouen_iris_shape_file nest_attribute: "nest" matcher:["entities"::Rouen_iris_shape_file, "data_id"::"CODE_IRIS", "pop_id"::"iris"]; 
	}
} 

species people {
	int Age;
	geometry nest;
	string Sexe;
	string iris;
	rgb color <- #red;
	
	aspect default {
		draw circle(15) color: color;
	}
}

experiment Localization type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		display map {
			image "Iris" gis:Rouen_iris_shape_file color: #lightgray ;
			species people;
		}
	}
}
