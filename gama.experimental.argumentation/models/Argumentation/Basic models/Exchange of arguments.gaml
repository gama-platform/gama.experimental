/**
* Name: DecisionMaking
* Based on the internal skeleton template. 
* Author: patricktaillandier
* Tags: 
*/

model ArgumentExchange

import "Argumentation graph generation.gaml"

global {
		
	init {
		do create_global_argumentation_graph;
		
		create argumentative_agent number: 100 with: (crit_importance:possible_criteria as_map (each::rnd(1.0)),
											source_type_confidence:source_types as_map (each:: rnd(1.0))
		) {
			argumentation_graph <- directed(graph([]));
			loop times: rnd(1,5){
				argument a <- one_of(global_argumentation_graph.vertices);
				bool is_ok <- add_argument(argument:a, graph: global_argumentation_graph);
			
			} 
			do build_opinion;
		} 		 
	}
}

species argumentative_agent skills:[argumenting] {
	float opinion;
	action build_opinion {
		pair decision <- make_decision();
		opinion <- float(decision.value) with_precision 2;
	}
	
	reflex exchange_argument {
		argument a <- one_of(argumentation_graph.vertices);
			
		ask one_of(argumentative_agent - self) {
			bool is_ok <- add_argument(argument:a, graph: global_argumentation_graph);
			do build_opinion;
		}
	}
	
	aspect default {
		draw circle(1) color: rgb(255 * (- opinion) , 255 * opinion, 0);
	}
}
experiment ArgumentExchange type: gui {
	output {
		display map {
			species argumentative_agent;
		}
	}
}
