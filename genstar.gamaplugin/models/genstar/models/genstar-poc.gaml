/**
* Name: Rouentemplate
* Author: administrateur
* Description: Describe here the model and its experiments
* Tags: Tag1, Tag2, TagN
*/

model Rouentemplate

global {
	
	
	file f_AC <- file("../data/Age & Couple-Tableau 1.csv");	
	file f_AS <- file("../data/Age & Sexe-Tableau 1.csv");
	file f_ASCSP <- file("../data/Age & Sexe & CSP-Tableau 1.csv");
	file f_IRIS <- file("../data/Rouen_iris.csv");

	// String constants
	file iris_shp <- file("../data/shp/Rouen_iris_number.shp");
	file buildings_shp <- file("../data/shp/buildings.shp");
	
	
	//name of the property that contains the id of the census spatial areas in the shapefile
	string stringOfCensusIdInShapefile <- "CODE_IRIS";

	//name of the property that contains the id of the census spatial areas in the csv file (and population)
	string stringOfCensusIdInCSVfile <- "iris";

	geometry shape <- envelope(iris_shp);

	list<string> tranches_age <- ["Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
				  				"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
								"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
								"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"];

	list<string> list_CSP <- ["Agriculteurs exploitants", "Artisans. commerçants. chefs d'entreprise", 
							"Cadres et professions intellectuelles supérieures", "Professions intermédiaires", 
							"Employés", "Ouvriers", "Retraités", "Autres personnes sans activité professionnelle"];

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

	graph<people> graph_friends;
	graph<people> graph_colleagues;

				
	init {	
		create building from: buildings_shp ;				
		create iris from: iris_shp with: [code_iris::string(read('CODE_IRIS'))];			
		
		gen_population_generator pop_gen;
		pop_gen <- pop_gen with_generation_algo "IS";  //"Sample";//"IS";

		pop_gen <- add_census_file(pop_gen, f_AC.path, "ContingencyTable", ";", 1, 1); 
		pop_gen <- add_census_file(pop_gen, f_ASCSP.path, "ContingencyTable", ";", 2, 1);
		pop_gen <- add_census_file(pop_gen, f_AS.path, "ContingencyTable", ";", 1, 1);
		pop_gen <- add_census_file(pop_gen, f_IRIS.path, "ContingencyTable", ",", 1, 1);			
		
		
		// --------------------------
		// Setup "AGE" attribute: INDIVIDUAL
		// --------------------------		
		
		pop_gen <- pop_gen add_attribute("Age", gen_range, tranches_age);
		
		map mapper1 <- [
			["15 à 19 ans"]::["15 à 19 ans"], ["20 à 24 ans"]::["20 à 24 ans"], ["25 à 39 ans"]::["25 à 29 ans", "30 à 34 ans", "35 à 39 ans"],
			["40 à 54 ans"]::["40 à 44 ans", "45 à 49 ans", "50 à 54 ans"], ["55 à 64 ans"]::["55 à 59 ans", "60 à 64 ans"],
			["65 à 79 ans"]::["65 à 69 ans", "70 à 74 ans", "75 à 79 ans"], ["80 ans ou plus"]::["80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"]
		];
		pop_gen <- pop_gen add_mapper("Age", gen_range, mapper1);					
				
		map mapper2 <- [
			["15 à 19 ans"]::["15 à 19 ans"], ["20 à 24 ans"]::["20 à 24 ans"], ["25 à 39 ans"]::["25 à 29 ans","30 à 34 ans","35 à 39 ans"],
			["40 à 54 ans"]::["40 à 44 ans","45 à 49 ans","50 à 54 ans"], ["55 à 64 ans"]::["55 à 59 ans", "60 à 64 ans"],
			["65 ans ou plus"]::["65 à 69 ans","70 à 74 ans","75 à 79 ans","80 à 84 ans","85 à 89 ans","90 à 94 ans","95 à 99 ans","100 ans ou plus"]
		];
		pop_gen <- pop_gen add_mapper("Age", gen_range, mapper2);


		// -------------------------
		// Setup "CSP" attribute: INDIVIDUAL
		// -------------------------

		pop_gen <- pop_gen add_attribute("CSP", string, list_CSP);
	

		// --------------------------
		// Setup "COUPLE" attribute: INDIVIDUAL
		// --------------------------				
				
		pop_gen <- pop_gen add_attribute("Couple", string, ["Vivant en couple", "Ne vivant pas en couple"]);
				
				
		// -------------------------
		// Setup "SEXE" attribute: INDIVIDUAL
		// -------------------------
		
		pop_gen <- pop_gen add_attribute("Sexe", string, ["Hommes", "Femmes"]);


		// -------------------------
		// Setup "IRIS" attribute: INDIVIDUAL
		// -------------------------

		pop_gen <- pop_gen add_attribute("iris", string, liste_iris, "P13_POP", int);  


		// -------------------------
		// Spatialization 
		// -------------------------
		pop_gen <- pop_gen localize_on_geometries(buildings_shp.path);
		pop_gen <- pop_gen localize_on_census(iris_shp.path);
		pop_gen <- pop_gen add_spatial_mapper(stringOfCensusIdInCSVfile,stringOfCensusIdInShapefile);

		// -------------------------
		// Social 
		// -------------------------
		pop_gen <- pop_gen add_network("friends","spatial",1000.0);
		pop_gen <- pop_gen add_network("colleagues","random",0.02);
		

		// -------------------------			
		create people from: pop_gen number: 100 ;
		pop_gen <- pop_gen associate_population_agents(people);
		graph_friends <- pop_gen get_network("friends");			
		graph_colleagues <- pop_gen get_network("colleagues");			
		
	}
}

species people {
	int Age;
	string Sexe;
	string iris;
	string Couple;
	string CSP;

	aspect default { 
		draw circle(4) color: #red border: #black;
		loop neigh over: graph_friends neighbors_of(self) {
			draw line([self.location,people(neigh).location]) color: #blue;
		}
		loop neigh over: graph_colleagues neighbors_of(self) {
			draw line([self.location,people(neigh).location]) color: #red;
		}		
	}
}

species iris {
	string code_iris;
	rgb color <- rnd_color(255);
	aspect default {
		draw shape color:color  border: #black;		
	}
}

species building {
	
	aspect default {
		draw shape color:#lightgrey  border: #black;
	}
}

experiment Rouentemplate type: gui {
	output {
		display map type: opengl {
			species iris;
			species building;
			species people;
		}
		
//		display c {
//			chart "ages" type: histogram {
//				loop i from: 0 to: 110 {
//					data ""+i value: people count(each.Age = i);
//				}
//			}
//		}
//		
//		display chart_csp {
//			chart "csp" type: histogram {
//				loop csp over: list_CSP {
//					data ""+csp value: people count(each.CSP = csp);
//				}
//			}
//		}		
//		
//		display s {
//			chart "sex" type: pie {
//				loop se over: ["Hommes", "Femmes"] {
//					data se value: people count(each.Sexe = se);
//				}
//			}
//		}
	}
}
