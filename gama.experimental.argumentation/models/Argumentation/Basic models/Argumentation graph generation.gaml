/**
* Name: Argumentationgraphgeneration
* Based on the internal empty template. 
* Author: patricktaillandier
* Tags: 
*/


model Argumentationgraphgeneration

global {
	int num_arguments <- 30;
	map<string,float> possible_criteria <- ["A"::0.5,"B"::0.25,"C"::0.1,"D"::0.05];
	map<string,float> source_types <- ["S1"::0.7,"S2"::0.2,"S3"::0.1];
	float agrument_pro_rate <- 0.7;
	float attack_num_mean <- 2.0;
	float attack_num_std <- 0.5;
	list<argument> arguments;
	list<pair<argument,argument>> attacks;
	graph global_argumentation_graph <- directed(graph([]));
	
	action create_global_argumentation_graph {
		loop i from: 1 to: num_arguments  {
			argument a <- argument(["id":: ""+i, 
									"option"::"choose A",
									"conclusion"::flip(agrument_pro_rate) ? "+":"-",
									"criteria"::[possible_criteria.keys[rnd_choice(possible_criteria.values)] :: 1.0],
									"source_type"::source_types.keys[rnd_choice(source_types.values)]
									
				
			]);
			arguments << a;
			
			bool is_ok <- global_argumentation_graph add_argument a;
		}
		
		loop a over:arguments  {
			int num_attacks <- round(gauss(attack_num_mean, attack_num_std));
			if num_attacks > 0 {
				list<argument> possible_args <- (arguments where (each.conclusion != a.conclusion));
				if not empty(possible_args) {
					list<argument> args <- num_attacks among possible_args;
				
					loop a2 over: args 
					{
						attacks << a::a2;
						bool is_ok <- global_argumentation_graph add_attack (a,a2);
					} 
				}
				
			}
		}
	}
}