/**
* Name: new
* Author: bgaudou
* Description: Describe here the model and its experiments
* Tags: Tag1, Tag2, TagN
*/

model new

global {
	file building_file <- shape_file("../includes/building.shp");
	
	file covers_file <- file("../includes/csp/covers.txt");
	file next_covers_file <- file("../includes/csp/next_covers.txt");
	file constraints_file <- csv_file("../includes/csp/system.csv",";");
	file proba_times_folder <- folder("../includes/proba_times");
	
	geometry shape <- envelope(building_file);
	
	init { 
		create building from:  building_file;
		create happyAPI number: 1;
		
		save building to:"../results/buildings.shp" type:"shp" attributes:["farm","id","facility","type","area"];
		
		file shp_file <- shape_file("../results/buildings.shp");
		
		write covers_file;
		
		ask happyAPI {
			map m <- self firstCAPFarm(
				shapefile:shp_file.path,
				constraints: constraints_file.path,
				covers:covers_file.path,
				next_covers:next_covers_file.path,
				proba_times_folder:proba_times_folder.path,
				farm:"f0"
			);		
			write m;
		}
		
		
		
	}
	
//	reflex saveSHP {
//		save building to:"../results/buildings.shp" type:"shp" with:[farm::"farm", id::"id",facility::"facility",type::"type",area::"area"];
//		
//		
//	}
}

species happyAPI skills: [APILandExtension] {
	
}

species building {
	int id -> {int(self)};
	string farm -> {"f0"};
	string facility -> {nil};
	string type -> {"parcel"};
	string area -> {"AA"};
	
}

experiment new type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		display d {
			species building;
		}
	}
}
