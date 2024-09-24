/**
* Name: Abstractmodel
* Based on the internal skeleton template. 
* Author: patrick taillandier
* Tags: 
*/

model Abstractmodel

import "../Basic models/Argumentation graph generation.gaml"

global {
	
	int num_possible_adopters <- 100;
	float mean_relatives <- 7.0;
	float std_relatives <- 3.0;
	
	float mean_args <- 0.5;
	float std_args <- 0.5;
	
	int num_arguments <- 50;
	map<string,float> possible_criteria <- ["A"::1.0,"B"::1.0,"C"::1.0,"D"::1.0, "E"::1.0];
	map<string,float> source_types <- ["S1"::1.0,"S2"::1.0,"S3"::1.0,"S4"::1.0,"S5"::1.0];
	float agrument_pro_rate <- 1.0;
	
	map<string,rgb> color_states <- ["knowledge"::#red, "persuasion"::#orange, "decision"::#yellow, "implementation":: #green, "confirmation"::#blue];
	
	float adopter_percentage <- 0.0 update: 100 * (possible_adopter count (each.adoption_state in ["implementation", "confirmation"])) / length(possible_adopter) ;
	init {
		do create_global_argumentation_graph;
		//write global_argumentation_graph.vertices collect argument(each).conclusion;
		create communication_channel number: 1 {
			type <- "communication channel";
			arguments <- world.arguments;
		}
		create possible_adopter number: num_possible_adopters {
			crit_importance <- possible_criteria ;
			source_type_confidence <- source_types;
			weight_attitude <- rnd(1.0);
			weight_social_norm <- rnd(1.0);
			weight_pbc <- 0.0;
			persuasion_threshold <- 0.1;
			decision_threshold <- 5;
			adoption_threshold <- 0.5;
			confirmation_time <- 200.0;
			proba_communication_channel <- communication_channel as_map (each::0.01);
			known_arguments <- [];
			
			convergence_speed <- 0.1;
			sigmoid_coeff <- 1.0;
			argument_lifespan <- 200.0;
			global_argumentation_graph <- world.global_argumentation_graph;
		}
		do generate_network;
		int sumSN <- possible_adopter sum_of length(each.social_network);
		ask possible_adopter {
			probability_exchange <- 1.0;//10 * length(social_network) / sumSN;
		}
		
		ask possible_adopter {
			int num_args <- round(gauss(mean_args, std_args));
			if num_args > 0 {
				list<argument> args <- 	num_args among arguments;
				loop a over: args {
				//	write sample(a.conclusion);
					do new_argument(a);
				}
			}	
			//write sample(argumentation_graph.vertices collect argument(each).conclusion);
		
		}
		
	}
	
	action generate_network {
		ask possible_adopter {
			int num_relatives <- round(gauss(mean_relatives, std_relatives));
			if num_relatives > 0 {
				social_network <- num_relatives among (possible_adopter - self);
			
			}
			influence_factor <- length(social_network) / num_possible_adopters;
		}
	}
}

species communication_channel parent: abstract_communication_channel;

species possible_adopter parent: abstract_adopter {
	aspect default {
		draw circle(1) color: color_states[adoption_state] ; 
	}
	aspect intention_aspect {
		float val <- (intention + 1)/2;
		//write sample(val);
		draw circle(1) color:  rgb(255 * (1 - val), 255 * val, 0);
	}
	
}


experiment explore_arguments_impact type: batch until: cycle = 1000 repeat: 20 keep_seed: true {
	parameter agrument_pro_rate var: agrument_pro_rate min: 0.0 max:1.0 ;
	method exploration  sample:5 ;
	reflex result {
		write sample(agrument_pro_rate) + " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
	}
}


experiment Abstractmodel type: gui {
	output {
		display adopters {
			species possible_adopter;
		}
		display intention {
			species possible_adopter aspect: intention_aspect;
		}
		
		display charts {
			chart "evolution" size: {1,0.3}{
				data "mean attitude" value: possible_adopter mean_of each.attitude color: #magenta;
				data "mean intention" value: possible_adopter mean_of each.intention color: #blue;
			}
			
			chart "adoption percentage" size: {1,0.3} position: {0,0.33} {
				data "adoption percentage" value:adopter_percentage  color: #green;
			}
			
			chart "Num of arguments per ageents" size: {1,0.5} position: {0,0.66} {
				data "num arguments" value: possible_adopter mean_of (length(each.known_arguments)) color: #blue;
			}
		}
	}
}
