/**
* Name: Rouentemplate
* Author: administrateur
* Description: Describe here the model and its experiments
* Tags: Tag1, Tag2, TagN
*/

model Rouentemplate

global {
	file f <- file("../data/Age & Sexe-Tableau 1.csv");
	
	init {		
		write f.path;
		gen_population_generator pop_gen;
		pop_gen <- pop_gen with_generation_algo "IS";  //"Sample";//"IS";
		pop_gen <- pop_gen add_census_file(f.path, "ContingencyTable", ";", 1, 1);
				
		list<string> tranches_age <- ["Moins de 5 ans", "5 à 9 ans", "10 à 14 ans", "15 à 19 ans", "20 à 24 ans", 
					  				"25 à 29 ans", "30 à 34 ans", "35 à 39 ans", "40 à 44 ans", "45 à 49 ans", 
									"50 à 54 ans", "55 à 59 ans", "60 à 64 ans", "65 à 69 ans", "70 à 74 ans", "75 à 79 ans", 
									"80 à 84 ans", "85 à 89 ans", "90 à 94 ans", "95 à 99 ans", "100 ans ou plus"];
		
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
				
		
		pop_gen <- pop_gen add_attribute("Sexe", string, ["Hommes", "Femmes"]);
				
		create people from: pop_gen number: 10000;
	}
}

species people {
	int Age;
	string Sexe;

	aspect default { 
		draw circle(2) color: #red border: #black;
	}
}

experiment Rouentemplate type: gui {
	output {
		display map {
			species people;
		}
		
		display c {
			chart "ages" type: histogram {
				loop i from: 0 to: 110 {
					data ""+i value: people count(each.Age = i);
				}
			}
		}
		
		display s {
			chart "sex" type: pie {
				loop se over: ["Hommes", "Femmes"] {
					data se value: people count(each.Sexe = se);
				}
			}
		}
	}
}
