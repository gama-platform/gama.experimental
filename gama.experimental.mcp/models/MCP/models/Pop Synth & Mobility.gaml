/**
* Name: Model to illustrate LLM use on synthetique population generation and mobility choice
* Author: Tutorial 3 & Benoit Gaudou
* Description: third part of the tutorial: Road Traffic with LLM access
* Tags: agent_movement, LLM
*/

model tutorial_gis_city_traffic

global {
	file shape_file_buildings <- file("../includes/building.shp");
	file shape_file_roads <- file("../includes/road.shp");
	file shape_file_bounds <- file("../includes/bounds.shp");
	geometry shape <- envelope(shape_file_bounds);
	float step <- 10 #mn;
	date starting_date <- date("2019-09-01-00-00-00");
	
	int nb_people <- 10;
	float ratio_genderM <- 0.4;
	
	int min_work_start <- 6;
	int max_work_start <- 8;
	int min_work_end <- 16; 
	int max_work_end <- 20; 
	float min_speed <- 1.0 #km / #h;
	float max_speed <- 5.0 #km / #h; 
	graph the_graph;
	
	string role <- "I am a data scientist, I do not know any programing language, I want to build mobility simulation";
	string msg <- "I want to generate the synthetic population of the small city of PetiteVille, composed of "+nb_people+" inhabitants.
I want to generate a list of "+nb_people+" items in the format [[name, gender]], where:
		- name is a common French first name.
		- gender is either 'M' for male or 'F' for female. The overall distribution must be "+int(nb_people*ratio_genderM)+" 'M' and "+(nb_people-int(nb_people*ratio_genderM))+" 'F'.
Names must match their appropriate gender (e.g., 'Jean' → 'M', 'Claire' → 'F').
Return only the list in this format:
[['Jean', 'M'], ['Paul', 'M'], ['Claire', 'F'], ...]
Do not include any explanatory text—only the list in the specified format.";

	
	init {
		create building from: shape_file_buildings with: [type::string(read ("NATURE"))] {
			if type="Industrial" {
				color <- #blue ;
			}
		}
		create road from: shape_file_roads ;
		the_graph <- as_edge_graph(road);
		
		list<building> residential_buildings <- building where (each.type="Residential");
		list<building> industrial_buildings <- building  where (each.type="Industrial") ;
				
		write "NOTE: if 0 agent is created or an error is triggered, it may be due to formatting of the answer provided by the LLM." color: #red;	
		
		write "Information transmitted to the LLM." color: #blue;
		write sample(role);
		write sample(msg);
		create genPop {
			llm <- create_ollama_chat_model( url: "http://localhost:11434", model_name: "llama3.1");
		//	chat_memory <- create_chat_memory(role:role);	
		}	
		
		ask genPop {
			string answer <- send_to_llm(llm, msg);
			write "Answer of the LLM." color: #blue;		
			write sample(answer);
			
			try {
				list<list<string>> l <- (answer contains "[[")?eval_gaml(answer):eval_gaml("["+answer+"]");
				write l;
				
				loop elt over: l {
					create people with:[name::elt[0],gender::elt[1]];
				}	
			} catch {
				create people number: nb_people with:[gender::(flip(0.5)?'M':'F')];				
			}
			
			ask people {
				speed <- rnd(min_speed, max_speed);
				start_work <- rnd (min_work_start, max_work_start);
				end_work <- rnd(min_work_end, max_work_end);
				living_place <- one_of(residential_buildings);
				working_place <- one_of(industrial_buildings);
				objective <- "resting";
				location <- any_location_in (living_place); 
				
				float distance_to_work <- 0.0; 
				using(topology(the_graph)) {
					distance_to_work <- self distance_to working_place * rnd(10.0);
				}		
				
				role <- "I am " + name + ". I am " + ((gender = 'M') ? "a man. " : "a woman. ");
				role <- role + "My workplace is " + distance_to_work + " meters away. ";
				
				llm_people <- create_ollama_chat_model( url: "http://localhost:11434", model_name: "llama3.1");
				
				write role + " has been created.";
			}
			
			write ""+length(people) + " created: " + ( (people count(each.gender = 'M')) /length(people)) + " M " + ( (people count(each.gender = 'F')) /length(people)) + " F.";
		}
	}
	
	string weather <- "it is raining." among: ["it is raining.","the weather is nice."];
		
	reflex test when: every(1#day){
		write "Today, " + current_date + " " + weather;
		write "car  : " + (people count( each.mobility_mode = "car" ));
		write "bike : " + (people count( each.mobility_mode = "bike" ));
		write "bus  : " + (people count( each.mobility_mode = "bus" ));
		weather <-  (flip(0.5))?"it is raining.":"the weather is nice.";
	}

}

species genPop skills: [llm] ;


species building {
	string type; 
	rgb color <- #gray  ;
	
	aspect base {
		draw shape color: color ;
	}
}

species road  {
	rgb color <- #black ;
	aspect base {
		draw shape color: color ;
	}
}

species people skills:[moving,llm] {
	string gender;
	rgb color <- (gender ='F')?#yellow:#green ;
	building living_place <- nil ;
	building working_place <- nil ;
	int start_work ;
	int end_work  ;
	string objective ; 
	point the_target <- nil ;
	
	chat_model llm_people;
	memory chat_memory_people;
	string role;
	
	string mobility_mode;

	reflex time_to_work when: current_date.hour = start_work and objective = "resting" {
		objective <- "working" ;
		the_target <- any_location_in (working_place);
				
		string prompt_mob <- "Today, the weather is: " + weather ;
		prompt_mob <- prompt_mob + "What is for me the best mode of transportation today? ";
		prompt_mob <- prompt_mob + "Answer with only one of the following words, and nothing else: car, bike, or bus.";
		
//		write prompt_mob color: #green;
		
		string answer <- lower_case(send_to_llm(llm_people, role+prompt_mob));
		mobility_mode <- (answer contains "car")?"car": ((answer contains "bike")?"bike":"bus");
		
		write role+prompt_mob color: #blue;
		write ""+self + " - " + answer + " -> " + mobility_mode;
		color <- (answer = "car") ? #red : ((answer = "bike") ? #green : #pink);
			
//		do add_to_chat_memory 
//			message: "The "+current_date.day+"-"+current_date.month+"-"+current_date.year+", I took the " + mobility_mode + "." 
//			memory: chat_memory_people;		
	
	//	role <- role + "The "+current_date.day+"-"+current_date.month+"-"+current_date.year+", I took the " + mobility_mode + ".";
	}
		
	reflex time_to_go_home when: current_date.hour = end_work and objective = "working"{
		objective <- "resting" ;
		the_target <- any_location_in (living_place); 
	} 
	 
	reflex move when: the_target != nil {
		do goto target: the_target on: the_graph ; 
		if the_target = location {
			the_target <- nil ;
		}
	}
	
	aspect base {
		draw circle(10) color: color border: #black;
	}
}


experiment road_traffic type: gui {
	parameter "Number of people agents" var: nb_people category: "People" ;

	
	output {
		display city_display type: 3d {
			species building aspect: base ;
			species road aspect: base ;
			species people aspect: base ;
		}
	}
}
