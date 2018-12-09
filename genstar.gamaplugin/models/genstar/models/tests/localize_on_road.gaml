/**
* Name: localizeiris
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model localizeiris

global {
	file f_IRIS <- file("../../data/Rouen_iris.csv");

	// String constants
	file roads_shp <- file("../../data/shp/roads.shp");

	geometry shape <- envelope(roads_shp);

	init {		
		create road from: roads_shp ;			
		
		gen_population_generator pop_gen;
		pop_gen <- pop_gen with_generation_algo "IS";  //"Sample";//"IS";

		pop_gen <- add_census_file(pop_gen, f_IRIS.path, "ContingencyTable", ",", 1, 1);			
			
		// -------------------------
		// Setup "IRIS" attribute: INDIVIDUAL
		// -------------------------

		list<string> liste_iris <- [
			"765400602", "765400104","765400306","765400201",
			"765400601","765400901","765400302","765400604","765400304",
			"765400305","765400801","765400301","765401004","765401003",
			"765400402","765400603","765400303","765400103","765400504",
			"765401006","765400702","765400401","765400202","765400802",
			"765400502","765400106","765400701","765401005","765400204",
			"765401001","765400405","765400501","765400102","765400503",
			"765400404","765400105","765401002","765400902","765400403",
			"765400203","765400101","765400205"];
		
		pop_gen <- pop_gen add_attribute("iris", string, liste_iris, "P13_POP", int);  
		
		// -------------------------
		// Spatialization 
		// -------------------------
		pop_gen <- pop_gen localize_on_geometries(roads_shp.path);
		pop_gen <- pop_gen localize_around_at(2.0, 10.0, false);

		// -------------------------			
		create people from: pop_gen number: 10000 ;
	}
}

species people {
	string iris;

	aspect default { 
		draw circle(4) color: color border: #black;
	}
}

species road {
	rgb color <- rnd_color(255);
	
	aspect default {
		draw shape color:color  border: #black;
	}
}

experiment Rouentemplate type: gui {
	output {
		display map type: opengl {
			species road;
			species people;
		}
	}
}
