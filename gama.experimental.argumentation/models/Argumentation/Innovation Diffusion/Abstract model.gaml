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
	float mean_relatives <- 6.5;
	float std_relatives <- 3.0;
	
	float mean_args <- 0.5;
	float std_args <- 0.5;
	
	float mean_args_influencer <- 10.0;
	float std_args_influencer <- 2.0;
	
	bool random_influencer <- false;
	
	float p <- 0.5;
	int k <- 6;
	int m0 <- 8;
	int m <- 3;
	int nb_nodes <- num_possible_adopters;
	
	bool save_result <- false;
	string result_file <- "evolution_result.csv";
	
	string semantics_type <- "max" ;
	
	float influencer_rate <- 0.0;
	string social_network_type <- "random" among:["no","random", "complete","latice8", "scale-free", "small-world"];
	
	int neighbors_type <- social_network_type = "latice4" ? 4 : 8 ;
	
	bool do_exchange_arguments <- true;
	bool do_use_communication_channel <- true;
	
	float proba_use_communication_chanel <- 0.1;
	bool use_exact_number <- true;
	
	int num_arguments <- 50;
	int agrument_pro_number <- 25;
	
	bool unfiform_individuals <- false;
	
	bool uniform_weights_pbc <- false;
	float g_weight_attitude <- 0.5;
	
	map<string,float> possible_criteria <- unfiform_individuals ?  ["A"::1.0] : ["A"::1.0,"B"::1.0,"C"::1.0,"D"::1.0,"E"::1.0];
	map<string,float> source_types <- unfiform_individuals ? ["S1"::1.0] : ["S1"::1.0,"S2"::1.0,"S3"::1.0,"S4"::1.0,"S5"::1.0];
	
	
	map<string,rgb> color_states <- ["knowledge"::#red, "persuasion"::#orange, "decision"::#yellow, "implementation":: #green, "confirmation"::#blue];
	
	float adopter_percentage <- 0.0 update: 100 * (possible_adopter count (each.adoption_state in ["implementation", "confirmation"])) / length(possible_adopter) ;
	float mean_intention <- 0.0 update: (possible_adopter mean_of (each.intention)) ;
	
	string id_sim <- "" +int(self)+"," + seed;
	
	list<float> adoption_rates;
	list<float> intentions;
	
	list<argument> arguments_get_from_usage;
	
	float polarization_ <- #max_float;
	
	init {
		do create_global_argumentation_graph;
		arguments_get_from_usage <- (0.2 * length(global_argumentation_graph.vertices)) among global_argumentation_graph.vertices;
		//write global_argumentation_graph.vertices collect argument(each).conclusion;
		create communication_channel number: 1 {
			type <- "communication channel";
			arguments <- world.arguments;
		}
		create possible_adopter number: num_possible_adopters {
			semantics <- semantics_type;
			crit_importance <- possible_criteria.keys as_map (each:: (unfiform_individuals ? 0.5 :rnd(1.0)));
			source_type_confidence <- source_types.keys as_map (each::  (unfiform_individuals ? 0.5 : rnd(1.0))); 
			social_norm <- rnd(-1.0, 1.0);
			weight_attitude <-uniform_weights_pbc ? g_weight_attitude : rnd(1.0);
			weight_social_norm <-  1 - weight_attitude;
			weight_pbc <- 0.0;
			persuasion_threshold <- 0.1;
			decision_threshold <- 5;
			adoption_threshold <- 0.5;
			confirmation_time <- 200.0;
			proba_communication_channel <- communication_channel as_map (each::do_use_communication_channel ? proba_use_communication_chanel : 0.0);
			known_arguments <- [];
			probability_innovation_usage <- 1.0;
			usage_arguments <- arguments_get_from_usage;
			convergence_speed <- 0.1;
			sigmoid_coeff <- 1.0;
			argument_lifespan <- 200.0;
			global_argumentation_graph <- world.global_argumentation_graph;
		}
		do generate_network;
		float sum_influence_factor <- possible_adopter sum_of each.influence_factor; 
		ask possible_adopter {
			probability_exchange <- sum_influence_factor = 0 ? 0 : (100 * (influence_factor /sum_influence_factor));
		}
		
		list<possible_adopter> influencers;
		if influencer_rate > 0 {
			int nb <- round(influencer_rate * length(possible_adopter));
			influencers <- nb first (random_influencer ? shuffle(possible_adopter) : (possible_adopter sort_by (-1 * each.influence_factor)));
		}
		ask possible_adopter {
			bool is_influencer <- self in influencers;
			int num_args <-  is_influencer ? round(gauss(mean_args_influencer, std_args_influencer)) : round(gauss(mean_args, std_args));
			if num_args > 0 {
				
				list<argument> args <- 	num_args among (is_influencer ? (arguments where (each.conclusion = "+")) :  arguments);
				loop a over: args {
					do new_argument(a,is_influencer ? argument_lifespan  : rnd(argument_lifespan));
				}
			}		
		}
		
		
	}
	
	action generate_network {
		switch social_network_type {
			match "no" {
				
			}
			match "complete" {
				ask possible_adopter {
					social_network <-(possible_adopter - self);
				}
			}
			match "random" {
				ask possible_adopter {
					int num_relatives <- do_exchange_arguments ? round(gauss(mean_relatives, std_relatives)) : 0;
					if num_relatives > 0 {
						social_network <- num_relatives among (possible_adopter - self);
					}
				}
			} 
			match "latice4" {
				loop i from: 0 to: length(cell) - 1 {
					possible_adopter[i].social_network <- cell[i].neighbors collect (possible_adopter[int(each)]);
				}
			}
			match "latice8" {
				loop i from: 0 to: length(cell) - 1 {
					possible_adopter[i].social_network <- cell[i].neighbors collect (possible_adopter[int(each)]);
				}
			}
			match "scale-free" {
				graph the_graph <- generate_barabasi_albert(m0, m, nb_nodes, true, node_agent, edge_agent);
				loop i from: 0 to: length(cell) - 1 {
					list<node_agent> ags <- the_graph neighbors_of the_graph.vertices[i] ;
					 possible_adopter[i].social_network <- ags collect possible_adopter[int(each)];
				} 	
				ask node_agent + edge_agent{
					do die;
				}
			}
			match "small-world" {
				graph the_graph <- generate_watts_strogatz(nb_nodes, p, k, true,node_agent, edge_agent);
				loop i from: 0 to: length(cell) - 1 {
					list<node_agent> ags <- the_graph neighbors_of the_graph.vertices[i] ;
					 possible_adopter[i].social_network <- ags collect possible_adopter[int(each)];
				} 		
				ask node_agent + edge_agent{
					do die;
				}
			}
		}
		ask possible_adopter {
			influence_factor <-  (length(social_network) / num_possible_adopters);
		}
		
	}
	
	float polarization{
		if (polarization_ != #max_float) {
			return polarization_;
		}
		polarization_ <- 0.0;
		list<float> dists;
		int N <- length(possible_adopter) - 1;
		
		ask possible_adopter {
			ask possible_adopter {
				if (self != myself) {
					dists << abs(intention - myself.intention);
				}
			}
		}
		
		float mean_val <- mean(dists);
		loop v over: dists {
			polarization_ <- polarization_ + ((v - mean_val) ^ 2);
		}
		polarization_ <- polarization_ / (1 * (N + 1) * N);
		return polarization_;
	}
 	
		
	
	reflex save_result_to_file when: save_result and every(10 #cycle){
		save id_sim+"," + cycle +"" + possible_adopter mean_of(each.intention) + ","+ adopter_percentage+"\n" rewrite:false format:"text" to:result_file; 
		
			
	}
	
	reflex save_result_to_file when: !save_result and every(10 #cycle){
		adoption_rates << adopter_percentage/100.0;
		intentions << possible_adopter mean_of (each.intention);	
				
	}
}

species node_agent;

species edge_agent;
grid cell width: 10 height: 10 neighbors: neighbors_type;

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
	
	
	
	/*reflex info {
		write name + " ->  " + known_arguments.keys collect (argument(each).conclusion) + " intention: " + intention + " attitude: " + attitude + " pbc : "+ pbc + " social_norm: " + social_norm + " probability_exchange: " + probability_exchange;
	}*/
	
}


experiment explore_evolution type: batch until: cycle >= 2000 repeat: 100 keep_seed: true {
	
	init {
		save_result <- false;
		
	} 
	reflex result {
		write sample(do_exchange_arguments) + " " +  sample(do_use_communication_channel) + " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
		string fl_a <- "id";
		string fl_i <- "id";
		string fl_ae <- "id";
		string file_a <- "explore_evolution/evolution_adoption.csv";
		string file_i <- "explore_evolution/evolution_intention.csv";
		string file_ae <- "explore_evolution/evolution_adoption_evolution.csv";
		
		int num <- simulations min_of length(each.intentions);
		loop i from: 0 to:  num -1 {
			fl_a <- fl_a +",adoption_" + i;
			fl_i <- fl_i +",intention_" + i;
			if (i < 20) {
				fl_ae <- fl_ae +",adoption_evolution_" + i;
			}
			
			
		}
		save fl_a +"\n" to: file_a format:"text";
		save fl_i +"\n" to: file_i format:"text";
		save fl_ae +"\n" to: file_ae format:"text";
		
		loop sim over: simulations {
			string val_a <- ""+ int(sim);
			string val_i <- ""+ int(sim);
			string val_ae <- ""+ int(sim);
			loop i from: 0 to:  num -1 {
				val_i <- val_i +"," + sim.intentions[i];
			}
			loop i from: 0 to:  num -1 {
				val_a <- val_a +"," + sim.adoption_rates[i];
				if (i = 0) {
					val_ae <- val_ae +"," + sim.adoption_rates[i];
				}else if(i < 20) {
					val_ae <- val_ae +"," + (sim.adoption_rates[i] - sim.adoption_rates[i-1]);
				}
			}
			save val_a +"\n" to: file_a format:"text" rewrite: false;
			save val_i +"\n" to: file_i format:"text" rewrite: false;
			save val_ae +"\n" to: file_ae format:"text" rewrite: false;
		
		}
		string file_t <- "explore_evolution/evolution.csv";
		save "id,polarization,mean intention,adoption rate\n" to: file_t format: "text";
		loop sim over: simulations {
			string val_t <- ""+ int(sim);
			val_t <- val_t + "," + (sim.polarization()) + "," + (last(sim.intentions)) + "," + (last(sim.adoption_rates)) ;	
			save val_t +"\n" to: file_t format:"text" rewrite: false;
		}
	}
}

experiment explore_stochasticity type: batch until: cycle >= 1000 repeat: 200 keep_seed: true {
	parameter do_exchange_arguments var: do_exchange_arguments among: [true];
	parameter do_use_communication_channel var: do_use_communication_channel  among: [true];
	
	init {
		save_result <- false;
		
		agrument_pro_number <- 25;
	} 
	reflex result {
		write sample(do_exchange_arguments) + " " +  sample(do_use_communication_channel) + " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
		string fl_a <- "id";
		string fl_i <- "id";
		string fl_p <- "id";
		string file_a <- "explore_stochasticity/stochasticity_adoption.csv";
		string file_i <- "explore_stochasticity/stochasticity_intention.csv";
		string file_p <- "explore_stochasticity/stochasticity_polarization.csv";
		int num <- simulations min_of length(each.intentions);
		loop i from: 0 to:  num -1 {
			fl_a <- fl_a +",adoption_" + i;
			fl_i <- fl_i +",intention_" + i;
			fl_p <- fl_p +",polarization_" + i;
		}
		save fl_a +"\n" to: file_a format:"text";
		save fl_i +"\n" to: file_i format:"text";
		save fl_p +"\n" to: file_p format:"text";
		loop sim over: simulations {
			string val_a <- ""+ int(sim);
			string val_i <- ""+ int(sim);
			
			loop i from: 0 to:  num -1 {
				val_i <- val_i +"," + sim.intentions[i];
			}
			loop i from: 0 to:  num -1 {
				val_a <- val_a +"," + sim.adoption_rates[i];
			}
			save val_a +"\n" to: file_a format:"text" rewrite: false;
			save val_i +"\n" to: file_i format:"text" rewrite: false;
			save val_i +"\n" to: file_p format:"text" rewrite: false;
		
		}
		string file_t <- "explore_stochasticity/stochasticity.csv";
		save "id,polarization,mean intention,adoption rate\n" to: file_t format: "text";
		loop sim over: simulations {
			string val_t <- ""+ int(sim);
			val_t <- val_t + ","  + (sim.polarization()) + "," + (last(sim.intentions)) + "," + (last(sim.adoption_rates)) ;	
			save val_t +"\n" to: file_t format:"text" rewrite: false;
		}
		
	}
}


experiment explore_semantics type: batch until: cycle >= 1000 repeat: 100 keep_seed: true {
	parameter semantics_type var: semantics_type among: ["max", "card", "categorizer"];
	init {
		save_result <- false;
		agrument_pro_number <- 25;
		attack_num_mean <- 10.0;
	
	} 
	reflex result {
		write "\nADOPTERS - " + sample(semantics_type) + " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
		write "INTENTION - " sample(semantics_type) +  " Mean: " + (simulations mean_of each.mean_intention) + " min: " +  (simulations min_of each.mean_intention)  + " max: " +  (simulations max_of each.mean_intention) ;
		write "POLARIZATION - " sample(semantics_type) + " Mean: " + (simulations mean_of each.polarization()) + " min: " +  (simulations min_of each.polarization())  + " max: " +  (simulations max_of each.polarization()) ;
		string fl_a <- "id";
		string fl_i <- "id";
		string file_a <- "explore_semantics/semantics_adoption_" + semantics_type +".csv";
		string file_i <- "explore_semantics/semantics_intention_" + semantics_type +".csv";
		int num <- simulations min_of length(each.intentions);
		loop i from: 0 to:  num -1 {
			fl_a <- fl_a +",adoption_" + i;
			fl_i <- fl_i +",intention_" + i;
		}
		save fl_a +"\n" to: file_a format:"text";
		save fl_i +"\n" to: file_i format:"text";
		loop sim over: simulations {
			string val_a <- ""+ int(sim);
			string val_i <- ""+ int(sim);
			loop i from: 0 to:  num -1 {
				val_i <- val_i +"," + sim.intentions[i];
			}
			loop i from: 0 to:  num -1 {
				val_a <- val_a +"," + sim.adoption_rates[i];
			} 
			save val_a +"\n" to: file_a format:"text" rewrite: false; 
			save val_i +"\n" to: file_i format:"text" rewrite: false;
		
		}
		string file_t <- "explore_semantics/semantics_" + semantics_type +".csv";
		save "id,polarization,mean intention,adoption rate\n" to: file_t format: "text";
		loop sim over: simulations {
			string val_t <- ""+ int(sim);
			val_t <- val_t + ","  + (sim.polarization()) + "," + (last(sim.intentions)) + "," + (last(sim.adoption_rates)) ;	
			save val_t +"\n" to: file_t format:"text" rewrite: false;
		}
		
	}
}


experiment explore_weight_attitude type: batch until: cycle >= 1000 repeat: 100 keep_seed: true {
	parameter g_weight_attitude var: g_weight_attitude among: [0.0, 0.5,1.0];
	parameter social_network_type var: social_network_type among:["no", "random"];
	init {
		save_result <- false;
		uniform_weights_pbc <- true;
		agrument_pro_number <- 25;
	} 
	reflex result {
		write "\nADOPTERS - " + sample(g_weight_attitude) + " - "+ sample(social_network_type)+ " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
		write "INTENTION - " sample(g_weight_attitude) + " - "+ sample(social_network_type)+ " Mean: " + (simulations mean_of each.mean_intention) + " min: " +  (simulations min_of each.mean_intention)  + " max: " +  (simulations max_of each.mean_intention) ;
		write "POLARIZATION - " sample(g_weight_attitude) + " - "+ sample(social_network_type)+ " Mean: " + (simulations mean_of each.polarization()) + " min: " +  (simulations min_of each.polarization())  + " max: " +  (simulations max_of each.polarization()) ;
		string fl_a <- "id";
		string fl_i <- "id";
		string file_a <- "explore_weight_attitude/weight_attitude_adoption_" + g_weight_attitude +"_"+social_network_type+".csv";
		string file_i <- "explore_weight_attitude/weight_attitude_intention_" + g_weight_attitude +"_"+social_network_type+".csv";
		int num <- simulations min_of length(each.intentions);
		loop i from: 0 to:  num -1 {
			fl_a <- fl_a +",adoption_" + i;
			fl_i <- fl_i +",intention_" + i;
		}
		save fl_a +"\n" to: file_a format:"text";
		save fl_i +"\n" to: file_i format:"text";
		loop sim over: simulations {
			string val_a <- ""+ int(sim);
			string val_i <- ""+ int(sim);
			loop i from: 0 to:  num -1 {
				val_i <- val_i +"," + sim.intentions[i];
			}
			loop i from: 0 to:  num -1 {
				val_a <- val_a +"," + sim.adoption_rates[i];
			} 
			save val_a +"\n" to: file_a format:"text" rewrite: false; 
			save val_i +"\n" to: file_i format:"text" rewrite: false;
		
		}
		string file_t <- "explore_weight_attitude/weight_attitude_" + g_weight_attitude +"_"+social_network_type+".csv";
		save "id,polarization,mean intention,adoption rate\n" to: file_t format: "text";
		loop sim over: simulations {
			string val_t <- ""+ int(sim);
			val_t <- val_t + ","  + (sim.polarization()) + "," + (last(sim.intentions)) + "," + (last(sim.adoption_rates)) ;	
			save val_t +"\n" to: file_t format:"text" rewrite: false;
		}
		
	}
}



experiment explore_heterogeinty type: batch until: cycle >= 1000 repeat: 100 keep_seed: true {
	parameter unfiform_individuals var: unfiform_individuals among: [true, false];
	
	init {
		save_result <- false;
		
		agrument_pro_number <- 25;
	} 
	reflex result {
		write sample(unfiform_individuals) + " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
		write sample(unfiform_individuals) + " Mean: " + (simulations mean_of each.mean_intention) + " min: " +  (simulations min_of each.mean_intention)  + " max: " +  (simulations max_of each.mean_intention) ;
		string fl_a <- "id";
		string fl_i <- "id";
		string file_a <- "explore_heterogeinty/heterogeinty_adoption_" + unfiform_individuals +".csv";
		string file_i <- "explore_heterogeinty/heterogeinty_intention_" + unfiform_individuals +".csv";
		int num <- simulations min_of length(each.intentions);
		loop i from: 0 to:  num -1 {
			fl_a <- fl_a +",adoption_" + i;
			fl_i <- fl_i +",intention_" + i;
		}
		save fl_a +"\n" to: file_a format:"text";
		save fl_i +"\n" to: file_i format:"text";
		loop sim over: simulations {
			string val_a <- ""+ int(sim);
			string val_i <- ""+ int(sim);
			loop i from: 0 to:  num -1 {
				val_i <- val_i +"," + sim.intentions[i];
			}
			loop i from: 0 to:  num -1 {
				val_a <- val_a +"," + sim.adoption_rates[i];
			}
			save val_a +"\n" to: file_a format:"text" rewrite: false;
			save val_i +"\n" to: file_i format:"text" rewrite: false;
		
		}
		string file_t <- "explore_heterogeinty/heterogeinty_" + unfiform_individuals +".csv";
		save "id,polarization,mean intention,adoption rate\n" to: file_t format: "text";
		loop sim over: simulations {
			string val_t <- ""+ int(sim);
			val_t <- val_t + ","  + (sim.polarization()) + "," + (last(sim.intentions)) + "," + (last(sim.adoption_rates)) ;	
			save val_t +"\n" to: file_t format:"text" rewrite: false;
		}
		
	}
}


experiment explore_pro_arguments_impact type: batch until: cycle >= 1000 repeat: 100 keep_seed: true {
	parameter agrument_pro_number var: agrument_pro_number among:[15,25,35,50] <- 15 ;
	
	init {
		save_result <- false;
		//social_network_type <- "complete";
	} 
	
	
	reflex result {
		write sample(agrument_pro_number) + " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
		string fl_a <- "id";
		string fl_ae <- "id";
		string fl_i <- "id";
		string fl_p <- "id";
		string file_a <- "explore_pro_arguments_impact/evolution_adoption_" + agrument_pro_number +".csv";
		string file_ae <- "explore_pro_arguments_impact/evolution_adoption_evolution_" + agrument_pro_number +".csv";
		string file_i <- "explore_pro_arguments_impact/evolution_intention_" + agrument_pro_number +".csv";
		string file_p <- "explore_pro_arguments_impact/evolution_polarization_" + agrument_pro_number +".csv";
		int num <- simulations min_of length(each.intentions);
		loop i from: 0 to:  num -1 {
			fl_a <- fl_a +",adoption_" + i;
			fl_ae <- fl_ae +",adoption_evolution_" + i;
			fl_i <- fl_i +",intention_" + i;
			fl_p <- fl_p +",polarization_" + i;
		}
		save fl_a +"\n" to: file_a format:"text";
		save fl_ae +"\n" to: file_ae format:"text";
		save fl_i +"\n" to: file_i format:"text";
		save fl_p +"\n" to: file_p format:"text";
		loop sim over: simulations {
			string val_a <- ""+ int(sim);
			string val_ae <- ""+ int(sim);
			string val_i <- ""+ int(sim);
			loop i from: 0 to:  num -1 {
				val_i <- val_i +"," + sim.intentions[i];
			}
			loop i from: 0 to:  num -1 {
				val_a <- val_a +"," + sim.adoption_rates[i];
				if (i = 0) {
					val_ae <- val_ae +"," + sim.adoption_rates[i];
				}else {
					val_ae <- val_ae +"," + (sim.adoption_rates[i] - sim.adoption_rates[i-1]);
				}
				
			}
			save val_a +"\n" to: file_a format:"text" rewrite: false;
			save val_ae +"\n" to: file_ae format:"text" rewrite: false;
			save val_i +"\n" to: file_i format:"text" rewrite: false;
			save val_i +"\n" to: file_p format:"text" rewrite: false;
		
		}
		string file_t <- "explore_pro_arguments_impact/evolution_" + agrument_pro_number +".csv";
		save "id,polarization,mean intention,adoption rate\n" to: file_t format: "text";
		loop sim over: simulations {
			string val_t <- ""+ int(sim);
			val_t <- val_t + "," + (sim.polarization()) + "," + (last(sim.intentions)) + "," + (last(sim.adoption_rates)) ;	
			save val_t +"\n" to: file_t format:"text" rewrite: false;
		}
		
	}
}



experiment explore_influencer_impact type: batch until: cycle >= 1000 repeat: 100 keep_seed: true {
	parameter influencer_rate var: influencer_rate among: [0.0, 0.01, 0.05, 0.1, 0.25] ;
	parameter random_influencer var: random_influencer among: [true, false] ;
	
	init {
		save_result <- false;
		agrument_pro_number <- 25;
	} 
	
	
	reflex result {
		write sample(influencer_rate) + " "  + sample(random_influencer)+ " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
		string fl_a <- "id";
		string file_a <- "explore_influencer_impact/influencer_adoption_" + influencer_rate + "_" + random_influencer +".csv";
		int num <- simulations min_of length(each.intentions);
		loop i from: 0 to:  num -1 {
			fl_a <- fl_a +",adoption_" + i;
			
		}
		save fl_a +"\n" to: file_a format:"text";
		loop sim over: simulations {
			string val_a <- ""+ int(sim);
			string val_ae <- ""+ int(sim);
			string val_i <- ""+ int(sim);
			loop i from: 0 to:  num -1 {
				val_i <- val_i +"," + sim.intentions[i];
			}
			loop i from: 0 to:  num -1 {
				val_a <- val_a +"," + sim.adoption_rates[i];
				if (i = 0) {
					val_ae <- val_ae +"," + sim.adoption_rates[i];
				}else {
					val_ae <- val_ae +"," + (sim.adoption_rates[i] - sim.adoption_rates[i-1]);
				}
				
			}
			save val_a +"\n" to: file_a format:"text" rewrite: false;
		
		
		}
		string file_t <- "explore_influencer_impact/influencer_" + influencer_rate+ "_" + random_influencer  +".csv";
		save "id,polarization,mean intention,adoption rate\n" to: file_t format: "text";
		loop sim over: simulations {
			string val_t <- ""+ int(sim);
			val_t <- val_t + "," +  (sim.polarization()) + "," + (last(sim.intentions)) + "," + (last(sim.adoption_rates)) ;	
			save val_t +"\n" to: file_t format:"text" rewrite: false;
		}
		
	}
}

experiment explore_social_network_impact type: batch until: cycle >= 1000 repeat: 100 keep_seed: true {
	parameter social_network_type var: social_network_type ;
	
	init {
		save_result <- false;
		agrument_pro_number <- 25;
		influencer_rate <- 0.1;
	} 
	
	
	reflex result {
		write sample(social_network_type) + " Mean: " + (simulations mean_of each.adopter_percentage) + " min: " +  (simulations min_of each.adopter_percentage)  + " max: " +  (simulations max_of each.adopter_percentage) ;
		string fl_a <- "id";
		string file_a <- "explore_social_network_impact/social_network_adoption_" + social_network_type +".csv";
		int num <- simulations min_of length(each.intentions);
		loop i from: 0 to:  num -1 {
			fl_a <- fl_a +",adoption_" + i;
			
		}
		save fl_a +"\n" to: file_a format:"text";
		loop sim over: simulations {
			string val_a <- ""+ int(sim);
			string val_ae <- ""+ int(sim);
			string val_i <- ""+ int(sim);
			loop i from: 0 to:  num -1 {
				val_i <- val_i +"," + sim.intentions[i];
			}
			loop i from: 0 to:  num -1 {
				val_a <- val_a +"," + sim.adoption_rates[i];
				if (i = 0) {
					val_ae <- val_ae +"," + sim.adoption_rates[i];
				}else {
					val_ae <- val_ae +"," + (sim.adoption_rates[i] - sim.adoption_rates[i-1]);
				}
				
			}
			save val_a +"\n" to: file_a format:"text" rewrite: false;
		
		
		}
		string file_t <- "explore_social_network_impact/social_network_" + social_network_type +".csv";
		save "id,polarization,mean intention,adoption rate\n" to: file_t format: "text";
		loop sim over: simulations {
			string val_t <- ""+ int(sim);
			val_t <- val_t + ","  + (sim.polarization()) + "," + (last(sim.intentions)) + "," + (last(sim.adoption_rates)) ;	
			save val_t +"\n" to: file_t format:"text" rewrite: false;
		}
		
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
