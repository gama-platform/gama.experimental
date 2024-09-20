
model model_argumentation

global {
	map<argument,point> locs;
	list<argument> arguments ;
	graph global_argumentation;
	
	float h <- 10.0;
	int nb_steps <- 2000000;
	
	string scenario <- "vegetarian_vegan" among:[ "omnivorous", "flexitarian", "vegetarian_vegan","random","no argument"]  ;
	bool no_type <- false;
	string type_argument <- "Ethical" among: ["Nutritional", "Health", "Ethical"];
	float proportion <- 0.2 ;
	bool pro_argument <- true  ;
	int nb_attacks <- 5  ;
	int nb_arguments <- 1 ;
	int nb_agents;
	float opinion_init;
	list<int> nb_per_group;
	list<argument> ags;
	
	float evol;
	float mean_opinion;
	int nb_with_a;
	list<list<int>> nb_per_group_prev; 
 	bool convergence <- false;
	int convergence_step_real;
	
	list<float> polarizations;
	list<float> mean_opinions;
	
	list<int> nb_with;
 	
 	init {
 		csv_file arg_file <- csv_file("../includes/Arg_PositiveNegative_Value.csv", ",", true);
		
		file arg_graph_file <- file("../includes/ArgumentsVegAbstract.dl");
 
		arguments <- load_arguments("vegetarian diet",arg_file);	
			
		global_argumentation <- load_graph(arg_graph_file, arguments);
		
		csv_file ag_file <- csv_file("../includes/data.csv", ";", true);
		
		
 		create people from: ag_file with:[option:: get("Opinion Option")];
 		nb_agents <- length(people);
		
 		ask people {
 			int nb_arguments_ags_C;
 			int nb_arguments_ags_P;
 			if (option = "Vegan") {
 				nb_arguments_ags_C <- rnd(0,1);
 				nb_arguments_ags_P <- 7 - nb_arguments_ags_C;
 			}else if (option ="Vegetarian") {
 				nb_arguments_ags_C <- rnd(1,2);
 				nb_arguments_ags_P <- 7 - nb_arguments_ags_C;
 			} else if (option = "Flexitarian") {
 				nb_arguments_ags_C <- rnd(3,4);
 				nb_arguments_ags_P <-  7 - nb_arguments_ags_C;
 			} else {
 				nb_arguments_ags_C <- rnd(5,7);
 				nb_arguments_ags_P <- 7 - nb_arguments_ags_C;
 			}
 			argumentation_graph <-directed(graph([]));
			loop times: nb_arguments_ags_C {
	 			do add_argument(self, one_of((arguments - argumentation_graph.vertices) where (each.conclusion = "-")), global_argumentation);
	 		}
	 		loop times: nb_arguments_ags_P {
	 			do add_argument(self, one_of((arguments - argumentation_graph.vertices) where (each.conclusion = "+")), global_argumentation);
	 		}
	 		
 			arguments_order <- shuffle(argumentation_graph.vertices);
 			
 			crit_importance["Social"] <- social;
 			crit_importance["Ethical"] <- ethical;
 			crit_importance["Nutritional"] <- nutritional;
 			crit_importance["Environmental"] <- environmental;
 			crit_importance["Economic"] <- economical;
 			crit_importance["Health"] <- health;
 			crit_importance["Anthropological"] <- anthropological;
			
 			
 		}
 		int nb_w <- 0;
 		if (scenario != "no argument") {
 			
 			loop times: nb_arguments {
 				if no_type {
 					type_argument <- one_of(first(people).crit_importance.keys);
 				}
 				argument a <- argument(["id":: "new argument","option"::"", "conclusion"::pro_argument ?"+" : "-", "criteria"::[type_argument::1.0]]);
				ags << a;
				add node(a) to: global_argumentation;
				list<argument> args_attacked <- nb_attacks among  (global_argumentation.vertices where ((argument(each).conclusion = (pro_argument ?"-" : "+"))));// and (type_argument in argument(each).criteria.keys)));
				loop ag over: args_attacked {
					global_argumentation <- global_argumentation add_edge(a::ag); 
				}
				list<argument> args_attacks <- nb_attacks among  (global_argumentation.vertices where ((argument(each).conclusion = (pro_argument ?"-" : "+"))));// and (type_argument in argument(each).criteria.keys)));
				loop ag over: args_attacks {
					global_argumentation <- global_argumentation add_edge(ag::a); 
				}
				int nb <- int(proportion * length(people));
				nb_w <- nb;
				list<people> pop;
				if scenario = "random" {
					pop <- (nb among people);
				} else {
					ask people {
	 					pair<list<argument>,float> decision <- pair<list<argument>, float>(make_decision());
						opinion <- decision.value;
	 				}
	 				 if scenario = "omnivorous" {
						list<people> pp_ordered <- people sort_by each.opinion;
						
						pop <- nb first pp_ordered;//(nb among (people where (each.option = "Omnivorous")));
					} 
					else if scenario = "flexitarian" {
						list<people> pp_ordered <- people sort_by abs(each.opinion);
					
						pop <- nb first pp_ordered;//(nb among (people where (each.option = "Flexitarian")));
					}
					else if scenario = "vegetarian_vegan" {
						list<people> pp_ordered <- people sort_by each.opinion;
						pop <- nb last pp_ordered;//(nb among (people where (each.option in ["Vegan", "Vegeratrian"])));
					}
				}
				ask pop  {
					argument r_arg <- first(arguments_order);
					do add_argument(self, a, global_argumentation);
					arguments_order >> r_arg;
					arguments_order << a;
				
				}
				
			}
 		}
 		
 	
		nb_with << nb_w;
 		ask people {
 			pair<list<argument>,float> decision <- pair<list<argument>, float>(make_decision());
			opinion <- decision.value;
			do update_homogeneous_arg;
 		}
		
 		opinion_init <- people mean_of each.opinion;
 		do compute_nb_per_group;
 		
		
		convergence <- false;
		if (h =  0) and convergence {
			convergence <- length(remove_duplicates(people collect (each.opinion))) = 1;
		}
		polarizations << world.polarization();
		mean_opinions << people mean_of each.opinion;
		
			
		
 		
 	}
 	
 	float polarization{
		list<float> dists;
		int N <- length(people) - 1;
		
		ask people {
			ask people {
				if (self != myself) {
					dists << abs(opinion - myself.opinion);
				}
			}
		}
		
		float mean_val <- mean(dists);
		float polarization;
		loop v over: dists {
			polarization <- polarization + ((v - mean_val) ^ 2);
		}
		polarization <- polarization / (1 * (N + 1) * N);
		return polarization;
	}
 	
 	action compute_nb_per_group {
 		nb_per_group <- [];
 		nb_per_group<< people count (each.opinion < -0.75); 
		nb_per_group<< people count ((each.opinion >= -0.75) and (each.opinion < -0.5));
		nb_per_group<< people count ((each.opinion >= -0.5) and (each.opinion < -0.25));
		nb_per_group<< people count ((each.opinion >= -0.25) and (each.opinion < 0.0)); 
		nb_per_group<< people count ((each.opinion >= 0.0) and (each.opinion < 0.25)); 
		nb_per_group<< people count ((each.opinion >= 0.25) and (each.opinion < 0.5)); 
		nb_per_group<< people count ((each.opinion >= 0.5) and (each.opinion < 0.75)); 
		nb_per_group<< people count ((each.opinion > 0.75)); 
		
		
		mean_opinion <- (people mean_of each.opinion) ;
		
		mean_opinions << mean_opinion;
		nb_with_a <- people count (not empty(ags inter each.argumentation_graph.vertices));
		nb_with << nb_with_a;
		evol <-mean_opinion - opinion_init ;
 	}
 	
 	reflex dynamic when: not convergence{
		
		people p <- one_of(people);
		ask p {
			people receiver <- exchange_with_other_argumentation();
			if receiver != nil and receiver.update_decision {
				pair<list<argument>,float> decision <- pair<list<argument>, float>(make_decision());
				opinion <- decision.value;
				do update_homogeneous_arg;
			}
		}

		
		if (cycle > 0 and every(2500#cycle)) {
			do compute_nb_per_group;
			if (every(5000#cycle)) {
				polarizations << world.polarization();
			}
			
		}
		convergence <- h> 0 and ((people first_with !(each.homogeneous)) = nil);
		
		if convergence {
			convergence_step_real <- cycle;
		}
	}
	
 	
 	reflex end_sim when: cycle = nb_steps {
 		//do pause;
 	}
}


species people skills: [argumenting] frequency: 0{
	string option;
	float opinion ;
	float health;
	float ethical;
	float anthropological; 
	float environmental;
	float nutritional;
	float economical <- 3.0;
	float social <- 3.0;
	bool update_decision <- false ;
	list<argument> arguments_order;
	bool homogeneous <- false;
	
	action update_homogeneous_arg {
		if (length(arguments_order) = 1) {homogeneous <- true;}
		else {
			list<string> conclusions <- remove_duplicates(arguments_order collect each.conclusion);
			homogeneous <- length(conclusions) = 1;
		}
	}
	people choice_of_partner {
		if h = 0 {
			people p;
			loop while: true {
				p <- one_of(people);
				if (p != self) {break;}
			}
			return p;
		}
		map<people, float> pps;
		loop p over: people - self {
			pps[p] <- (0.5 * (2 - abs(p.opinion - opinion))) ^ h;
		}
		if (sum(pps.values) = 0) {
				return nil;
		} else {
			return pps.keys[rnd_choice(pps.values)];
		}

	}

	
	
	people exchange_with_other_argumentation {
		people receiver <- choice_of_partner();
		ask receiver {
			argument r_arg <- first(arguments_order);
			list<argument> args;
				list<list<argument>> extensions <- myself.preferred_extensions(myself.argumentation_graph);
			float mv;
			loop ext over: extensions {
				float val <- myself.evaluate_conclusion(ext);
				if (abs(val) > mv) {
					mv <- abs(val);
					args <- ext;
				} else if (abs(val) = mv) {
					args <- args + ext;
				}

			}

			args <- remove_duplicates(args);
			//	write "args2:" + args;
			if not empty(args) {
				argument argt <- one_of(args);
				myself.arguments_order >> argt;
				myself.arguments_order << argt;
				if (argt in arguments_order) {
					arguments_order >> argt;
					arguments_order << argt;
				} else {
					arguments_order << argt;
					bool added <- add_argument(self, argt, global_argumentation);
					if (added) {
						arguments_order >> r_arg;
						do remove_argument(r_arg);
					}

					update_decision <- added or update_decision;
				}

			}

		}
		return receiver;

	}
	
}


experiment argumentation_model type: gui {
	output {
		display chart refresh: every(100 #cycle){
			/*chart "opinon"  series_label_position:none memorize: false style: dot size: {1,0.5}{
				datalist legend:list(people) collect each.name value: list(people) collect each.opinion color:#black;
			}*/
			chart "opinon" size: {1,0.5} y_range: {-1,1}{
				data "Mean" value: people mean_of each.opinion;
			}
			chart "opinion histogram" type: histogram size: {1,0.5} position: {0, 0.5} y_range: {0,500}{
				data "[-1,-0.75[" value: people count (each.opinion < -0.75); 
				data "[-0.75,-0.5[" value: people count ((each.opinion >= -0.75) and (each.opinion < -0.5));
				data "[-0.5,-0.25[" value: people count ((each.opinion >= -0.5) and (each.opinion < -0.25));
				data "[-0.25,0.0[" value: people count ((each.opinion >= -0.25) and (each.opinion < 0.0)); 
				data "[-0.0,0.25[" value: people count ((each.opinion >= 0.0) and (each.opinion < 0.25)); 
				data "[0.25,0.5[" value: people count ((each.opinion >= 0.25) and (each.opinion < 0.5)); 
				data "[0.5,0.75[" value: people count ((each.opinion >= 0.5) and (each.opinion < 0.75)); 
				data "[0.75,1.0]" value: people count ((each.opinion > 0.75)); 
				
			}
		}
		
		
	}
}


experiment batch_model_prop type: batch until: cycle = 200000 repeat: 30 keep_seed: true{
	parameter proportion var: proportion among:[0.0, 0.1, 0.2, 0.5, 1.0 ];
	parameter no_type var: no_type <- true among:[true];
	reflex result {
		list sims <- (simulations where each.convergence);
		string val_ <- "proportion: " + proportion +
		 " mean_opinion:" + (simulations mean_of each.mean_opinion) + " - :" + standard_deviation(simulations collect each.mean_opinion) + 
		" evol:" + (simulations mean_of each.evol)  + " - :" + standard_deviation(simulations collect each.evol) +
		 " nb_with_a: "+ (simulations mean_of each.nb_with_a) + " - :" + standard_deviation(simulations collect each.nb_with_a) 
		 + " nb convergence: " + (simulations count each.convergence) + " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) 
		 + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real))  
		+ " mean polarization:" + (simulations mean_of last(each.polarizations)) + " std polarizations:" + standard_deviation(simulations collect last(each.polarizations)) + " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + " std:" + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])];
		
		write val_;
	
		save val_ to:"Vegan_nb_proportion/result_diff2.csv" rewrite: false;
		ask simulations {
		//write name + " nb_of_attacks:" + self.nb_of_attacks + " nb_per_group:" + self.nb_per_group;
			string val;
			bool first <- true;
			loop p over: self.polarizations {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_nb_proportion/result_" + proportion + "_polarization"+ ".csv" rewrite: false;
			
			val <- "";
			first <- true;
			loop p over: self.mean_opinions {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_nb_proportion/result_" + proportion + "_opinion"+ ".csv" rewrite: false;
			
				val <- "";
			first <- true;
			loop p over: self.nb_with {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_nb_proportion/result_" + proportion + "_nb"+ ".csv" rewrite: false;
		}
		
			
		}
		
}


experiment batch_model_nb type: batch until: cycle = 200000 repeat: 30 keep_seed: true{
	parameter nb_arguments var: nb_arguments among:[1,3,5,7];
	parameter no_type var: no_type <- true among:[true];
	reflex result {
		list sims <- (simulations where each.convergence);
		string val_ <- "nb_arguments: " + nb_arguments +
		 " mean_opinion:" + (simulations mean_of each.mean_opinion) + " - :" + standard_deviation(simulations collect each.mean_opinion) + 
		" evol:" + (simulations mean_of each.evol)  + " - :" + standard_deviation(simulations collect each.evol) +
		 " nb_with_a: "+ (simulations mean_of each.nb_with_a) + " - :" + standard_deviation(simulations collect each.nb_with_a) 
		 + " nb convergence: " + (simulations count each.convergence) + " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) 
		 + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real))  
		+ " mean polarization:" + (simulations mean_of last(each.polarizations)) + " std polarizations:" + standard_deviation(simulations collect last(each.polarizations))
		+ " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + " std:" + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])];
		
		write val_;
	
		save val_ to:"Vegan_nb_args/result_diff2.csv" rewrite: false;
		ask simulations {
		//write name + " nb_of_attacks:" + self.nb_of_attacks + " nb_per_group:" + self.nb_per_group;
			string val;
			bool first <- true;
			loop p over: self.polarizations {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_nb_args/result_" + nb_arguments + "_polarization"+ ".csv" rewrite: false;
			
			val <- "";
			first <- true;
			loop p over: self.mean_opinions {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_nb_args/result_" + nb_arguments + "_opinion"+ ".csv" rewrite: false;
			
		}
		
		
		
	}
}



experiment batch_model_no_evo type: batch until: cycle > 0 repeat: 30 keep_seed: true{
	parameter scenario var: scenario <- "no argument" among:["no argument"];

	reflex result {
		list sims <- (simulations where each.convergence);
		string val_ <- 
		 " mean_opinion:" + (simulations mean_of each.mean_opinion) + " - :" + standard_deviation(simulations collect each.mean_opinion) + 
		" evol:" + (simulations mean_of each.evol)  + " - :" + standard_deviation(simulations collect each.evol) +
		 " nb_with_a: "+ (simulations mean_of each.nb_with_a) + " - :" + standard_deviation(simulations collect each.nb_with_a) 
		 + " nb convergence: " + (simulations count each.convergence) + " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) 
		 + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real))  
		+ " mean polarization:" + (simulations mean_of last(each.polarizations)) + " std polarizations:" + standard_deviation(simulations collect last(each.polarizations)) + " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + " std:" + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])];
		write val_;
	
		save val_ to:"Vegan_init/result_diff2.csv" rewrite: false;
		ask simulations {
		//write name + " nb_of_attacks:" + self.nb_of_attacks + " nb_per_group:" + self.nb_per_group;
			string val;
			bool first <- true;
			loop p over: self.polarizations {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_init/result_polarization"+ ".csv" rewrite: false;
			
			val <- "";
			first <- true;
			loop p over: self.mean_opinions {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_init/result_opinion"+ ".csv" rewrite: false;
			
			val <- "";
			first <- true;
			loop p over: self.nb_with {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_init/result_nb"+ ".csv" rewrite: false;
		}
		
		
		
	}
}


experiment batch_model_no_arg type: batch until: cycle = 500000 repeat: 30 keep_seed: true{
	parameter scenario var: scenario <- "no argument" among:["no argument"];

	reflex result {
		list sims <- (simulations where each.convergence);
		string val_ <- 
		 " mean_opinion:" + (simulations mean_of each.mean_opinion) + " - :" + standard_deviation(simulations collect each.mean_opinion) + 
		" evol:" + (simulations mean_of each.evol)  + " - :" + standard_deviation(simulations collect each.evol) +
		 " nb_with_a: "+ (simulations mean_of each.nb_with_a) + " - :" + standard_deviation(simulations collect each.nb_with_a) 
		 + " nb convergence: " + (simulations count each.convergence) + " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) 
		 + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real))  
		+ " mean polarization:" + (simulations mean_of last(each.polarizations)) + " std polarizations:" + standard_deviation(simulations collect last(each.polarizations))
		+ " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + " std:" + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])];
		
		write val_;
	
		save val_ to:"Vegan_simple/result_diff2.csv" rewrite: false;
		ask simulations {
		//write name + " nb_of_attacks:" + self.nb_of_attacks + " nb_per_group:" + self.nb_per_group;
			string val;
			bool first <- true;
			loop p over: self.polarizations {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_simple/result_polarization"+ ".csv" rewrite: false;
			
			val <- "";
			first <- true;
			loop p over: self.mean_opinions {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_simple/result_opinion"+ ".csv" rewrite: false;
			
			val <- "";
			first <- true;
			loop p over: self.nb_with {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan_simple/result_nb"+ ".csv" rewrite: false;
		}
		
		
		
	}
}


experiment batch_model type: batch until: cycle = 200000 repeat: 30 keep_seed: true{
	parameter scenario var: scenario;
	parameter type_argument var: type_argument;
	
	reflex result {
		list sims <- (simulations where each.convergence);
		string val_ <- "scenario: " + scenario + " type_argument: " + type_argument +
		 " mean_opinion:" + (simulations mean_of each.mean_opinion) + " - :" + standard_deviation(simulations collect each.mean_opinion) + 
		" evol:" + (simulations mean_of each.evol)  + " - :" + standard_deviation(simulations collect each.evol) +
		 " nb_with_a: "+ (simulations mean_of each.nb_with_a) + " - :" + standard_deviation(simulations collect each.nb_with_a) 
		 + " nb convergence: " + (simulations count each.convergence) + " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) 
		 + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real))  
		+ " mean polarization:" + (simulations mean_of last(each.polarizations)) + " std polarizations:" + standard_deviation(simulations collect last(each.polarizations))
		+ " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + " std:" + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])];
		
		write val_;
	
		save val_ to:"Vegan/result_diff2.csv" rewrite: false;
		ask simulations {
		//write name + " nb_of_attacks:" + self.nb_of_attacks + " nb_per_group:" + self.nb_per_group;
			string val;
			bool first <- true;
			loop p over: self.polarizations {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan/result_" + scenario + "_" + type_argument + "_polarization"+ ".csv" rewrite: false;
			
			val <- "";
			first <- true;
			loop p over: self.mean_opinions {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan/result_" + scenario + "_" + type_argument + "_opinion"+ ".csv" rewrite: false;
			
			val <- "";
			first <- true;
			loop p over: self.nb_with {
				if first {
					val <- "" + p;
					first <- false;
				} else {
					val <- val + "," + p;
				}

			} 

			save val to: "Vegan/result_" + scenario + "_" + type_argument + "_nb"+ ".csv" rewrite: false;
		}
		
		
		
	}
}
