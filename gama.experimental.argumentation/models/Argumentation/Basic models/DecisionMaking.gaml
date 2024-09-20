/**
* Name: DecisionMaking
* Based on the internal skeleton template. 
* Author: patricktaillandier
* Tags: 
*/

model DecisionMaking

import "Argumentation graph generation.gaml"

global {
	list<argument> arguments_used;
	float opinion;
	init {
		do create_global_argumentation_graph;
		create argumentative_agent with: (argumentation_graph: global_argumentation_graph,
											crit_importance:possible_criteria as_map (each::rnd(1.0)),
											source_type_confidence:source_types as_map (each:: rnd(1.0))
		) {
			do build_opinion;
		}  
	}
}

species argumentative_agent skills:[argumenting] {
	
	action build_opinion {
		pair decision <- make_decision();
		opinion <- float(decision.value) with_precision 2;
		arguments_used <- decision.key;
		write name + " " sample(opinion) + " " + sample(arguments_used);
	}
}
experiment DecisionMaking type: gui {
	output {
		display map {
			graphics "nodes" {
				loop i from: 0 to: num_arguments - 1 {
					argument a <- arguments[i];
					if (a in arguments_used) {
						draw square(3.0) color:opinion >= 0.0 ? #lightgreen : #orange at: {a.conclusion = "+" ? 25 : 75, 5 + i*4};
					}
					draw circle(1.0) color: a.conclusion = "+" ? #green : #red border: #black at: {a.conclusion = "+" ? 25 : 75, 5 + i*4};
					
				}
			}
			graphics "attacks" {
				loop i from: 0 to: length(attacks) {
					pair att <- attacks[i];
					argument a0 <- att.key;
					argument a1 <- att.value;
					draw line([{a0.conclusion = "+" ? 25 : 75, 5 + (arguments index_of a0)*4},{a1.conclusion = "+" ? 25 : 75, 5 + (arguments index_of a1)*4}]) color:#black end_arrow: 1;
				}
			}
		}
	}
}
