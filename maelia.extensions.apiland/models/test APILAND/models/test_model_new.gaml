/**
* Name: new
* Author: patrick taillandier
* Description: Describe here the model and its experiments
* Tags: Tag1, Tag2, TagN
*/

model new
global skills: [apiland_territory]{

       file parcelles_file <- shape_file("../includes/parcelles.shp"); 

     	map<string,rgb> color_per_type;
       geometry shape <- envelope(parcelles_file);

      map<string, farm> farms_per_id;

       init {

             create parcelle from:  parcelles_file with: (id_farm:get("ID_EXPL"), name : get("id"));
             map<string,list<parcelle>> group_parcelles <- parcelle group_by each.id_farm;
            loop gp over: group_parcelles.keys {
            	map<string,parcelle> parcs <- group_parcelles[gp] as_map (each.name ::each);
 				create farm with: (name:gp, parcelles:parcs) {
 					farms_per_id[name] <- self;
 				}
 			}
 			shapefile_path <- "../includes/parcelles.shp"; // on peut mettre cette ligne, mais c'est maintenant optionnel 
 			proba_folder_times <- "../includes/proba_times";
 			loop f over: farm {
 				string folder_path <- "../includes/csp/exploitation_" + f.name;
 				do add_farm(f.name);
 				do set_covers_file(farm_id:f.name,group_path: folder_path + "/groups.txt", cover_path:folder_path+"/covers.txt" );
 				do set_system_file(f.name,folder_path+"/contraintes.csv");
 				do set_historic(f.name,folder_path+"/historic.txt");
 			}
 			map<string, map<string, list<string>>> covers <- next_covers(starting_date:date(2020,7,1),ending_date:date(2024,7,1));
 			loop id over: covers.keys {
 				farm f <- farms_per_id[id];
 				loop p over: covers[id].keys {
 					parcelle par <- f.parcelles[p];
 					par.covers <- covers[id][p];
 					write par.covers;
 					par.cover <- first(par.covers);
 				}
 			}
 			
 			map<string,rgb> cols <- remove_duplicates(parcelle collect each.cover) as_map (each::rnd_color(255));
 			ask parcelle {
 				color <- cover = nil ? #white : cols[cover];
 			}

       }

}


species farm  {
	map<string,parcelle> parcelles;
	rgb color <- rnd_color(255);
	
	aspect default {
		loop p over: parcelles.values {
			draw p color: color border: #black;
		}
	}
}


species parcelle{
	string id_farm;
	string cover;
	list<string> covers;
	rgb color <- #white;
	
	aspect default {
		draw shape color: color border: #black;
	}
}

 

experiment main type: gui {
       output {

             display farms {
                species farm;
             }
              display covers {
                species parcelle;
             }

       }

}