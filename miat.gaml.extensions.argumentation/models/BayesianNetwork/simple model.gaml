/***
* Name: simplemodel
* Author: Patrick Taillandier
* Description: Example of use of bayesian network
* Tags: Bayesian Network
***/

model simplemodel

global torus: true{
	float cost_to_hunt <- 2.4;
	int nb_hunters <- 50;
	init {
		ask patch {
			prey <- rnd(10.0);
			color <- rgb(255, 255 * (1 - prey/20),255 * (1 - prey/20));
		}
		create hunter number: nb_hunters{
			my_patch <- one_of(patch);
			location <- my_patch.location;
		}
	}
}

species scheduler schedules: shuffle(hunter) + patch;

grid patch width: 30 height: 30 schedules: []{
	float prey min: 0.0 max:20.0;
	
	reflex pop_dynamic when: prey <= 18{
		prey <- prey + 0.1;
	}
	reflex update_color {
		color <- rgb(255, 255 * (1 - prey/20),255 * (1 - prey/20));
	}
}

species hunter schedules: [] {
	rgb color <- #blue;
	bayesian_network BN;
	float energy;
	bool intention_hunt <- false;
	bool hunger <- false;
	patch my_patch;
	
	init {
		BN <- bayesian_network("behavior");
			
			BN <- BN create_node("Hunger");
			BN <- BN add_node_outcome("Hunger", "High");
			BN <- BN add_node_outcome("Hunger", "Low");
			BN <- BN add_node_probabilities("Hunger", ["High"::0.5, "Low"::0.5]);
			
			
			BN <- BN create_node("Prey");
			BN <- BN add_node_outcome("Prey", "High");
			BN <- BN add_node_outcome("Prey", "Low");
			BN <- BN add_node_probabilities("Prey", ["High"::0.5, "Low"::0.5]);
			
			BN <- BN create_node("Hunting");
			BN <- BN add_node_outcome("Hunting", "true");
			BN <- BN add_node_outcome("Hunting", "false");
			BN <- BN add_node_parent("Hunting","Hunger");
			BN <- BN add_node_parent("Hunting","Prey");
			
			BN <- BN add_node_probabilities("Hunting", ["Hunger"::["High"::["true"::1.0, "false"::0.0], "Low"::["true"::0.9, "false"::0.1]],
				"Prey"::["High"::["true"::0.2, "false"::0.8], "Low"::["true"::1.0, "false"::0.0]]
			]);
	}
	
	reflex move {
		my_patch <- my_patch.neighbors with_max_of(each.prey);
		location <- my_patch.location;
		energy <- energy - 1;
	}
	
	reflex determine_hunger {
		if (energy <= 5) {
			hunger <- true;
			color <- #red;
		} else {
			hunger <- false;
			color <- #green;
		}
	}
	
	
	reflex define_intention_hunt {
		BN <- BN add_node_evidence("Hunger",hunger ? "High" : "Low");
		BN <- BN add_node_evidence("Prey", (my_patch.prey > 5) ? "High" : "Low");
		float proba <- float((BN get_beliefs "Hunting")["true"]);
		intention_hunt <- flip(proba);
	}

	reflex to_hunt when: intention_hunt{
		energy <- energy - cost_to_hunt + my_patch.prey;
 	 	ask my_patch {prey <- 0.0;}
	}
	

	reflex starving when: energy < 0 {
		do die;
	}
	
	reflex reproduce when: energy > 50 {
		energy <- 25.0;	
  		create hunter {
  				energy <- 25.0;	
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
			grid patch lines:#black;
			species hunter;
		}
	}
}
