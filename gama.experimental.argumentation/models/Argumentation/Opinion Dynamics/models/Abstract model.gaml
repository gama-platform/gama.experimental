/***
* Name: BoundedConfidenceModel
* Author: admin_ptaillandie
* Description: 
* Tags: Tag1, Tag2, TagN
***/
model model_comparison

global {
	graph global_argumentation;
	list<argument> arguments;
	int nb_arguments_ags_C <- 5;
	int nb_arguments_ags_P <- 5;
	int nb_arguments_ags <- 10;
	int nb_arguments_tot <- 60;
	int nb_of_attacks <- 300;	
	
	bool convergence <- false;
	list<string> C;
	list<string> P;
	bool one_per_step <- true ;
	float h <- 9.0;
	int nb_steps <- 30000;
	int nb_agents <- 100;
	list<int> nb_per_group;
	list<list<int>> nb_per_group_prev;
	int convergence_step_real;
	matrix<float> proximity;
	
	list<float> polarizations;
	init {
		csv_file arg_file <- csv_file("../includes/Arg_Value.csv", ",", true);
		list<argument> arguments_tot <- load_arguments("abstract", arg_file);
		arguments <- (nb_arguments_tot /2) among (arguments_tot where (each.conclusion = "+"));
		arguments <- arguments + ((nb_arguments_tot /2) among (arguments_tot where (each.conclusion = "-")));
		global_argumentation <- load_graph(file("../includes/ArgumentsAbstract.dl"), arguments);
		list<argument> arg_pro <- arguments where (each.conclusion = "+");
		list<argument> arg_con <- arguments where (each.conclusion = "-");
		int nb_pro <- length(arg_pro);
		int nb_con <- length(arg_con);
		loop times: nb_of_attacks {
			list<argument> possible_arg <- arg_pro where (out_degree_of(global_argumentation, each) < nb_con) + arg_con where (out_degree_of(global_argumentation, each) < nb_pro);
			argument source <- one_of(possible_arg);
			if (source != nil) {
				argument target <- one_of((source in arg_pro) ? (arg_con where not (global_argumentation contains_edge (source::each))) : (arg_pro where not (global_argumentation
				contains_edge (source::each))));
				if (target != nil) {
					global_argumentation <- global_argumentation add_edge (source::target);
				}

			}

		}

		create people number: nb_agents;
		proximity <- 0.0 as_matrix{nb_agents,nb_agents};
		ask people {
			argumentation_graph <- directed(graph([]));
			if (nb_arguments_ags <= 0) {
				loop times: nb_arguments_ags_C {
					do add_argument(self, one_of((arguments - argumentation_graph.vertices) where (each.conclusion = "-")), global_argumentation);
				}

				loop times: nb_arguments_ags_P {
					do add_argument(self, one_of((arguments - argumentation_graph.vertices) where (each.conclusion = "+")), global_argumentation);
				}

			} else {
				loop times: nb_arguments_ags {
					argument arg <- one_of(arguments - argumentation_graph.vertices);
					do add_argument(self, arg, global_argumentation);
				}

			}

			arguments_order <- shuffle(argumentation_graph.vertices);
			loop cri over: ["Social", "Ethical","Nutritional", "Environmental", "Economic", "Health", "Anthropological", "C1"]{
						crit_importance[cri] <- 1.0;
				
 					}
			pair<list<argument>,float> decision <- pair<list<argument>, float>(make_decision());
			opinion <- decision.value;
			do update_homogeneous_arg;
			
		}

		do compute_nb_per_group;
		loop id from: 0 to: nb_agents - 1 {
			people a <- people(id);	
		
			loop i from: 0 to: nb_agents - 1 {
				if (i != id) {
					proximity[i,id] <- (0.5 * (2 - abs(people(i).opinion - a.opinion))) ^ h;
				}	
			} 
			 
		}
		convergence <- nb_arguments_ags = length(arguments);
		if (h =  0) and convergence {
			convergence <- length(remove_duplicates(people collect (each.opinion))) = 1;
		}
		polarizations << world.polarization();
	}
	
	float polarization{
		list<float> dists;
		int N <- length(people) - 1;
		loop i from: 0 to: N {
			people pi <- people(i);
			loop j from: 0 to: N {
				if (i != j) {
					people pj <- people(j);
					dists << abs(pi.opinion - pj.opinion);
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
		nb_per_group << people count (each.opinion < -0.75);
		nb_per_group << people count ((each.opinion >= -0.75) and (each.opinion < -0.5));
		nb_per_group << people count ((each.opinion >= -0.5) and (each.opinion < -0.25));
		nb_per_group << people count ((each.opinion >= -0.25) and (each.opinion < 0.0));
		nb_per_group << people count ((each.opinion >= 0.0) and (each.opinion < 0.25));
		nb_per_group << people count ((each.opinion >= 0.25) and (each.opinion < 0.5));
		nb_per_group << people count ((each.opinion >= 0.5) and (each.opinion < 0.75));
		nb_per_group << people count ((each.opinion > 0.75));
	}

	reflex dynamic when: not convergence{
		
		people p <- one_of(people);
		ask p {
			people receiver <- exchange_with_other_argumentation();
			if receiver.update_decision {
				pair<list<argument>,float> decision <- pair<list<argument>, float>(make_decision());
				float prev_op <- copy(opinion);
				opinion <- decision.value;
				do update_homogeneous_arg;
				int id <- int (self);
				if (h != 0) and (prev_op != opinion) {
					loop i from: 0 to: nb_agents - 1 {
						if (i != id) {
							proximity[i,id] <- (0.5 * (2 - abs(people(i).opinion - opinion))) ^ h;
						}
						
					} 
				}
			}
		}

		
		
		if (cycle > 0 and every(1000#cycle)) {
			do compute_nb_per_group;
			polarizations << world.polarization();
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

species people skills: [argumenting] frequency: 0 {
	float opinion;
	float uncertainty;
	bool update_decision <- false;
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
				/*loop p over: people - self {
					pps[p] <- (0.5 * (2 - abs(p.opinion - opinion))) ^ h;
				}*/
				list<float> vals <- proximity row_at int(self);
				if (sum(vals) = 0) {
					return one_of(people - self);
				} else {
					return people[rnd_choice(vals)];
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

experiment basic_exp type: gui {
	output {
		display chart refresh: every(100 #cycle) {
			/*chart "opinon" series_label_position: none memorize: false size: {1, 0.5} {
				datalist legend: list(people) collect each.name value: list(people) collect (each.opinion) color: [#black];
			}*/

			chart "opinion histogram" type: histogram size: {1, 0.5} position: {0, 0.5} {
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


experiment batch_test_total_arguments type: batch until: cycle = 1000000 keep_seed: true repeat: 100 {
	parameter nb_arguments_ags var: nb_arguments_ags among: [1, 3, 5, 7, 10, 20, 30, 60];

	reflex result {
		list sims <- (simulations where each.convergence);
		string vv <- name + " nb_arguments_ags:" + nb_arguments_ags + " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + "std: " + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])] + " nb convergence: " + (simulations count each.convergence) 
		+ " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real));
			
		write vv ;
		save vv to: "nb_arguments/result_histogram.csv" rewrite: false;
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

			save val to: "nb_arguments/result_" + nb_arguments_ags + ".csv" rewrite: false;
		}

	}

}


experiment batch_test_argumentation_arguments type: batch until: cycle = 1000000 keep_seed: true repeat: 100 {
	parameter nb_arguments_ags var: nb_arguments_ags among: [1, 3, 5, 7, 10, 20, 30, 60];

	reflex result {
		list sims <- (simulations where each.convergence);
		string vv <- name + " nb_arguments_ags:" + nb_arguments_ags + " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + "std: " + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])] + " nb convergence: " + (simulations count each.convergence) 
		+ " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real));
			
		write vv ;
		save vv to: "nb_arguments/result_histogram.csv" rewrite: false;
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

			save val to: "nb_arguments/result_" + nb_arguments_ags + ".csv" rewrite: false;
		}

	}

}


experiment batch_test_argumentation_attacks type: batch until: cycle = 1000000 keep_seed: true repeat: 100 {
	parameter nb_of_attacks var: nb_of_attacks among: [0, 100, 200,300, 400,500,600,700,800, 900];

	reflex result {
		list sims <- (simulations where each.convergence);
		string vv <- name + " nb_of_attacks:" + nb_of_attacks + " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + "std: " + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])] + " nb convergence: " + (simulations count each.convergence) 
		+ " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real));
			
		write vv ;
		save vv to: "nb_attacks/result_histogram.csv" rewrite: false;
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

			save val to: "nb_attacks/result_" + nb_of_attacks + ".csv" rewrite: false;
		}

	}

}



experiment batch_test_argumentation_h type: batch until: cycle = 1000000 keep_seed: true repeat: 100 {
	parameter h var: h among: [0.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0];

	reflex result {
		list sims <- (simulations where each.convergence);
		string vv <- name + " h:" + h + " nb_per_group:" + [simulations mean_of each.nb_per_group[0], simulations mean_of each.nb_per_group[1], simulations
		mean_of each.nb_per_group[2], simulations mean_of each.nb_per_group[3], simulations mean_of each.nb_per_group[4], simulations mean_of each.nb_per_group[5], simulations mean_of
		each.nb_per_group[6], simulations mean_of each.nb_per_group[7]] + "std: " + [standard_deviation(simulations collect each.nb_per_group[0]), standard_deviation(simulations collect
		each.nb_per_group[1]), standard_deviation(simulations collect each.nb_per_group[2]), standard_deviation(simulations collect each.nb_per_group[3]), standard_deviation(simulations
		collect each.nb_per_group[4]), standard_deviation(simulations collect each.nb_per_group[5]), standard_deviation(simulations collect
		each.nb_per_group[6]), standard_deviation(simulations collect each.nb_per_group[7])] + " nb convergence: " + (simulations count each.convergence) 
		+ " mean convergence step: " + (empty(sims) ? 0.0 : (sims mean_of each.convergence_step_real)) + " std convergence step: " + (empty(sims) ? 0.0 : standard_deviation(sims collect each.convergence_step_real));
			
		write vv ;
		save vv to: "h/result_histogram.csv" rewrite: false;
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

			save val to: "h/result_" + h + ".csv" rewrite: false;
		}

	}

}
