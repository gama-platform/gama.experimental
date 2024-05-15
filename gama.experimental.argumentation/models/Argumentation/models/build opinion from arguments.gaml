/**
* Name: buildopinionfromarguments
* show how to build by hand a simple argumentation graph and to define the opinion of the agent according 
* Author: patricktaillandier
* Tags: 
*/


model buildopinionfromarguments

global {
	int nb_arguments_Con <- 5;
	int nb_arguments_Pro <- 5;
	int nb_attacks <- 10;
	
	
	graph global_argumentation_graph;
	
	map<argument, argument_display> argument_to_agent ;
	
	init {		
		do create_argumentation_graph;
		create argumentative_agent with: (argumentation_graph:global_argumentation_graph) {
			pair<list<argument>, float> decision <- pair<list<argument>, float>(make_decision());
			write "Opinion: " + decision.value + "\nlist of arguments used to build this opinion: " + decision.key;
		}
		do argumentation_graph_display;
	}
	
	//action defined to create the argumentation graph
	action create_argumentation_graph {
		//define an empty graph
		global_argumentation_graph <- graph([]);
		int cpt <- 0;
		
		//add arguments with the corresponding conclusion ("-" for con, "+" for pro)
		loop times: nb_arguments_Con {
			bool ok <- global_argumentation_graph add_argument argument(["id"::"c_" +cpt, "conclusion":: "-"]);
			cpt <- cpt + 1;
		}
		loop times: nb_arguments_Pro {
			bool ok <- global_argumentation_graph add_argument argument(["id"::"p_" +cpt, "conclusion":: "+"]);
			cpt <- cpt + 1;
		}
		
		//add random attacks between the arguments
		loop times: nb_attacks {
			argument source_arg <- one_of(global_argumentation_graph.vertices);
			argument target_arg <- one_of(global_argumentation_graph.vertices where (source_arg.conclusion != argument(each).conclusion));
			bool ok <- global_argumentation_graph add_attack (source_arg, target_arg);
			
		}
		
	}
	
	//action defined to be able to display the argumentation graph (using a specific species of agents)
	action argumentation_graph_display {
		loop a over: global_argumentation_graph.vertices  {
			create argument_display with:(argument_displayed:a, name:argument(a).id);
		}
		argument_to_agent <- argument_display as_map (each.argument_displayed::each);
		
		graph agent_graph <- spatial_graph([]);
		loop ag over: argument_display {
			agent_graph <- agent_graph add_node ag  ;
		}
		loop e over: global_argumentation_graph.edges {
			argument_display ag1 <- argument_to_agent[argument(pair(e).key)];
			argument_display ag2 <-  argument_to_agent[argument(pair(e).value)];
			agent_graph <- agent_graph add_edge (ag1::ag2);
		}
		agent_graph <- layout_force(agent_graph, world.shape * 0.8,0.8 , 0.1, 1000);
	}
}

//agents with the capabilities of argumenting
species argumentative_agent skills:[argumenting];


//species just used to display the argumentation graph
species argument_display {
	argument argument_displayed;
	aspect default {
		draw circle(2) color: argument_displayed.conclusion = "+" ? #lightgreen : #salmon;
		draw name color: #black font:font(12) anchor:#center;
	}
}

experiment main {
	output {
		display argumentation_graph_view type: 3d axes: false{
			graphics "attacks" {
				loop e over: global_argumentation_graph.edges {
					draw line([argument_to_agent[argument(pair(e).key)],argument_to_agent[argument(pair(e).value)]]) color:#black end_arrow: 2;
				}
			}
			species argument_display;
			
		}
	}
	
}