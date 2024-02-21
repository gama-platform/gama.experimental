/**
* Name: Localization
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model Localization

global {
	shape_file Rouen_iris_shape_file <- shape_file("../includes/shp/Rouen_iris_number.shp");
	geometry shape <- envelope(Rouen_iris_shape_file);
	
		
	shape_file buildings_residential_shape_file <- shape_file("../includes/shp/buildings_residential.shp");
	shape_file schools_shape_file <- shape_file("../includes/shp/schools_fusion.shp");
	
	init {
		create residential_building from: buildings_residential_shape_file;
		create school from: schools_shape_file;
		create iris from: Rouen_iris_shape_file;
		
		generate species:people number: 10000 
		from:[csv_file("../includes/Age & Sexe-Tableau 1.csv",";")] 
		attributes:["Age"::["Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
					  				"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
									"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
									"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"],
						"Sexe"::["Hommes", "Femmes"]];
		
		
		
		
		localize species: people nests: residential_building nest_attribute: "home"  
			mapper: ["entities"::iris, "data_id"::"POP"]
			constraints:[["type"::"density", "priority"::10, "step_increase"::0.002, "max_increase"::0.01, "max_density"::0.0005]]; 
		
			
		list<people> children <- people where (each.Age >= 3 and each.Age <= 18); 
		link entities: children nests: school nest_attribute: "my_school" distribution: "gravity" parameters:["friction_coeff"::3.0];
		ask children {
			color <- #yellow;
			location <- any_location_in(my_school);
		}
	}
} 
   
species building {
	rgb color;
	aspect default {
		draw shape color: color;
	}
}

species residential_building parent: building{
	rgb color <- #gray;
}

species school {
	rgb color <- #pink;
}  


species iris {
	int POP;
	aspect default {
		draw shape color: #lightgray border:#gray;
	}
}

species people {
	int Age;
	building home;
	string Sexe;
	building my_school;
	rgb color <- #red;
	
	aspect default {
		draw circle(15) color: color;
	}
}

experiment Localization type: gui {
	output {
		display map {
			species iris;
			species residential_building;
			species school;
			species people;
		}
	}
}
