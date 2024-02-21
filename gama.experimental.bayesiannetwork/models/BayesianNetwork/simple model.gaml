/***
* Name: simplemodel
* Author: Patrick Taillandier
* Description: Example of use of bayesian network
* Tags: Bayesian Network
***/
model simplemodel

global torus: true {
	int cost_to_hunt <- 2;
	int nb_hunters <- 50;
	float prey_growth <- 0.2;

	init {
		ask patch {
			prey <- rnd(10.0);
			color <- rgb(255 * (1 - prey / 20), 255, 255 * (1 - prey / 20));
		}

		create hunter number: nb_hunters {
			my_patch <- one_of(patch);
			location <- my_patch.location;
		}

	}

}

species scheduler schedules: shuffle(hunter) + patch;

grid patch width: 30 height: 30 schedules: [] {
	float prey min: 0.0 max: 20.0;

	reflex pop_dynamic {
		prey <- prey + prey * prey_growth * ((20 - prey) / 20);
	}

	reflex update_color {
		color <- rgb(255 * (1 - prey / 20), 255, 255 * (1 - prey / 20));
	}

}

species hunter schedules: [] {
	rgb color <- #blue;
	bayesian_network BN;
	int energy <- rnd(1,20);
	bool intention_hunt <- false;
	bool hunger <- false;
	patch my_patch;
	int hunting_capacity <- rnd(100);
	int exploitation_rate <- rnd(100);

	// each hunter starts with its own BN parametrization
	float Hunger_high_prey_low <- rnd(1.0);
	float Hunger_high_prey_high <- rnd(1.0);
	float Hunger_low_prey_low <- rnd(1.0);
	float Hunger_low_prey_high <- rnd(1.0);

	init {
		BN <- bayesian_network("behavior");
		BN <- BN create_node ("Hunger");
		BN <- BN add_node_outcome ("Hunger", "High");
		BN <- BN add_node_outcome ("Hunger", "Low");
		BN <- BN add_node_probabilities ("Hunger", ["High"::0.5, "Low"::0.5]);
		BN <- BN create_node ("Prey");
		BN <- BN add_node_outcome ("Prey", "High");
		BN <- BN add_node_outcome ("Prey", "Low");
		BN <- BN add_node_probabilities ("Prey", ["High"::0.5, "Low"::0.5]);
		BN <- BN create_node ("Hunting");
		BN <- BN add_node_outcome ("Hunting", "No");
		BN <- BN add_node_outcome ("Hunting", "Yes");
		BN <- BN add_node_parent ("Hunting", "Hunger");
		BN <- BN add_node_parent ("Hunting", "Prey");
		BN <- BN add_node_probabilities
		("Hunting", [["Hunger"::"High", "Prey"::"High"]::["No"::Hunger_high_prey_high, "Yes"::(1 - Hunger_high_prey_high)], ["Hunger"::"High", "Prey"::"Low"]::["No"::Hunger_high_prey_low, "Yes"::(1 - Hunger_high_prey_low)], ["Hunger"::"Low", "Prey"::"High"]::["No"::Hunger_low_prey_high, "Yes"::(1 - Hunger_low_prey_high)], ["Hunger"::"Low", "Prey"::"Low"]::["No"::Hunger_low_prey_low, "Yes"::(1 - Hunger_low_prey_low)]]);
	}

	reflex move {
		my_patch <- my_patch.neighbors with_max_of (each.prey);
		location <- my_patch.location;
		energy <- energy - 1;
	}

	reflex determine_hunger {
		if (energy <= 5) {
			hunger <- true;
			color <- #red;
		} else {
			hunger <- false;
			color <- #blue;
		}

	}

	reflex define_intention_hunt {
		BN <- BN add_node_evidence ("Hunger", hunger ? "High" : "Low");
		BN <- BN add_node_evidence ("Prey", (my_patch.prey > 10) ? "High" : "Low");
		float proba_Yes <- float((BN get_beliefs "Hunting")["Yes"]);
		intention_hunt <- flip(proba_Yes);
	}

	reflex to_hunt when: intention_hunt {
		if (intention_hunt) {
			if ((rnd(100) - 100 * (20 - my_patch.prey) / 20) > hunting_capacity) {
				int prey_collected <- int(my_patch.prey * exploitation_rate / 100);
				energy <- energy - cost_to_hunt + prey_collected;
				ask my_patch {
					prey <- prey - prey_collected;
				}

			}

		}

	}

	reflex starving when: energy < 0 {
		do die;
	}

	reflex reproduce when: energy > 50 {
		energy <- 25;
		create hunter {
			energy <- 25;
			hunting_capacity <- myself.hunting_capacity + rnd(2) - 1;
			exploitation_rate <- myself.exploitation_rate + rnd(2) - 1;
			my_patch <- myself.my_patch;
			location <- my_patch.location;
		}

	}

	aspect default {
		draw circle(1) color: color;
	}

}

experiment simplemodel type: gui {
	output {
		display map {
			grid patch border: #black;
			species hunter;
		}

		display evolution {
			chart "Evolution of preys and hunters" type: series {
				data "hunter" value: length(hunter) color: #blue;
				data "prey" value: patch sum_of (each.prey) / 10 color: #green;
			}

		}

		display genetic {
			chart "Genetic evolution" type: series {
				data "hunting capacity" value: hunter sum_of (each.hunting_capacity) / length(hunter) color: #red;
				data "exploitation rate" value: hunter sum_of (each.exploitation_rate) / length(hunter) color: #pink;
			}

		}

	}

	parameter "Nombre de chasseurs" var: nb_hunters category: "Hunters";
	parameter "Cost of hunting" var: cost_to_hunt category: "Hunters";
	parameter "Growth of preys" var: prey_growth category: "Preys";
}

