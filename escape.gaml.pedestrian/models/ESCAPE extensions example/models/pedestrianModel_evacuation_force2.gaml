/***
* Name: pedestrianModel
* Author: admin_ptaillandie
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model pedestrianModel

global {
	//si false, petite zone de test, sinon, grosse zone
	bool all_data <- false parameter:true;
	
	//si jamais on veut sauvegarder les plus courts chemins pour les réutiliser
	bool save_shortest_path <- false;
	//utilisation ou non de la matrise des plus courts chemins
	bool use_shortest_path_file <- false;
	
	//batiments et routes
	file building_shapefile <- all_data ? file("../includes/buildings.shp"): file("../includes/bds.shp");
	file pedestrian_shapefile <- all_data ? file("../includes/pedestrian.shp") : file("../includes/bds_pedestrian2.shp");
	
	//c'est l'enveloppe extérieure de la zone d'étude (ils ne peuvent pas en sortir)
	file bounds_shapefile <- all_data ? file("../includes/buildings_bounds.shp") : file("../includes/bds_bounds.shp");
	
	graph network;
	
	geometry shape <- envelope(pedestrian_shapefile);
	
	float P_shoulder_length <- 0.45 parameter: true;
	float P_body_depth <- 0.28 parameter: true;
	bool P_use_body_geometry <- false parameter: true ;
	float P_proba_detour <- 0.5 parameter: true ;
	bool P_avoid_other <- true parameter: true ;
	float P_obstacle_consideration_distance <- 1.0 parameter: true ;
	
	
	string P_pedestrian_model among: ["simple", "SFM"] <- "simple" parameter: true ;
	float P_obstacle_distance_repulsion_coeff <- 5.0 category: "simple model" parameter: true ;
	float P_overlapping_coefficient <- 2.0 category: "simple model" parameter: true ;
	float P_perception_sensibility <- 1.0 category: "simple model" parameter: true ;
	
	float P_A_SFM parameter: true <- 4.5 category: "SFM" ;
	float P_relaxion_SFM parameter: true <- 0.54 category: "SFM" ;
	float P_gama_SFM parameter: true <- 0.35 category: "SFM" ;
	float P_n_SFM <- 2.0 parameter: true category: "SFM" ;
	float P_n_prime_SFM <- 3.0 parameter: true category: "SFM";
	float P_lambda_SFM <- 2.0 parameter: true category: "SFM" ;
	
	
	//une distance qui represente la distance à laquelle une personne peut s'éloigner d'une route (linéaire) au max 
	float dist_segments <- 5.0;
	
	int nb_people <- all_data ? 15700: 1000;
	
	//nombre de personne pouvant sortir par la(es) sortie(s) en un pas de temps
	int max_exit_flow <- 10;
	
	//juste pour tester l'influence du fait de prendre en compte les autres dans le déplacement
	bool avoid_other <- true;
	
	//proba de quitter sa maison pour la zone de sortie
	float proba_leave_house <- 0.01;
	
	//la, j'ai qu'une sortie, du coup, je l'ai mis en variable globale par simplicité
	point exit_point;
	
	bool verbose <- false;
	
	//juste utilise pour la visu
	geometry simulation_outside;
	
	init {
		if (verbose) {write "Debut de la simulation";}
		
		//chargement de la zone d'étude
		geometry bounds <- first(file(bounds_shapefile).contents);
		simulation_outside <- shape - bounds;
		//on crée les batiments et on calcule pour chacun la zone autour (pour savoir au faire popper les bonhommes quand ils sortent de chez eux
		create building from:building_shapefile {
			free_space <- ((shape buffer (dist_segments, 8)) - shape) inter bounds;
			ask building at_distance dist_segments {
				free_space <- free_space - shape;
			}
		}
		if (verbose) {write "Batiments importes";}
		
		//je nettoie un peu les routes avant de créer les agents correspondant : on enleve les passages trop etroit et on garde que la composante connexe principale
		list<geometry> rds; 
		loop r over:pedestrian_shapefile.contents {
			if (bounds covers r) {
				geometry s <- r buffer(P_shoulder_length /2.0, 10);
				if (empty(building overlapping s)){
					rds << r;
				} 
			}
			
		}
		rds <- main_connected_component(as_edge_graph(rds)).edges;
		
		if (verbose) {write "Fichier routes nettoye";}
		
		//creation des routes, pour chaque route je créé des agents segements qui vont servir au déplacement des agents
		create road from: rds  {
			do initialize obstacles:[building] distance: 1.0;
		}
		
		if (verbose) {write "import road ok";}
		
		//pour chaque agent segment, je calcule les batiments et les segments voisins, ainsi que l'espace libre autour.
		
		if (verbose) {write "creation segment ok";}
		network <- as_edge_graph(road);
		
		//je choisi comme sortie le noeud le plus proche de {0,0}
		create exit_place with: [location:: network.vertices closest_to {0,0}];
		
		//je creer des people que je place aléatoirement dans des batiments
		create people number:nb_people{
			location <- any_location_in(one_of(building));
			pedestrian_model <- P_pedestrian_model;
			obstacle_distance_repulsion_coeff <- P_obstacle_distance_repulsion_coeff;
			obstacle_consideration_distance <- P_obstacle_consideration_distance;
			overlapping_coefficient <- P_overlapping_coefficient;
			perception_sensibility <- P_perception_sensibility ;
			shoulder_length <- P_shoulder_length;
			avoid_other <- P_avoid_other;
			proba_detour <- P_proba_detour;
			A_SFM <- P_A_SFM;
			relaxion_SFM <- P_relaxion_SFM;
			gama_SFM <- P_gama_SFM;
			n_SFM <- P_n_SFM;
			n_prime_SFM <- P_n_prime_SFM;
			lambda_SFM <- P_lambda_SFM;
			
			obstacle_species <- [people, building];
			
		}
		
		if (verbose) {write "creation people ok";}
	
		//sauvegarde et import de la matrise de ppc si nécessaire	
		string path_shortest_path <- all_data ? "../includes/pedestrian_ssp_all.csv": "../includes/pedestrian_ssp.csv";
		if (save_shortest_path) {
			network <-  use_cache(network, false) with_optimizer_type "FloydWarshall";
			matrix ssp <- all_pairs_shortest_path(network);
			save ssp type:"text" to:path_shortest_path;
		}
		if (not save_shortest_path and use_shortest_path_file ) {
			network <- network load_shortest_paths matrix<int>(file(path_shortest_path));
		}
	}
	
	reflex stop when: empty(people) {
		do pause;
	}
	
}

species exit_place {
	int mex_people_flow <- max_exit_flow;
	int count update: 0;
	aspect default {
		draw circle(all_data? 5 :2 ) color: #green border: #black;
	}
	aspect demo {
		draw sphere(5) color: #green ;
	}
}

species road skills: [pedestrian_road]{
	aspect default {
		if(free_space != nil) {draw free_space color: #lightpink border: #black;}
		draw shape color: #black;
	}
}

species building {
	geometry free_space;
	float high <- rnd(10.0, 20.0);
	
	aspect demo {
		draw shape border: #black depth: high texture: ["../includes/top.png","../includes/texture5.jpg"];
	}
	
	aspect default {
		draw shape color: #gray border: #black;
	}
}

species people skills: [escape_pedestrian]{
	rgb color <- rnd_color(255);
	float speed <- gauss(5,1.5) #km/#h min: 2 #km/#h;
	
	bool in_building <- true; //only for display purpose
	
	//lieu de sortie choisi (ici il n'y en a qu'un)
	exit_place target_exit;
	//a-t-il atteint le lieu de sortie ?
	bool reach_exit <- false;
	
	
	
	//comportement de choix de la cible
	reflex choose_target when: final_target = nil and not reach_exit and flip(proba_leave_house) {
		target_exit <- first(exit_place);
		final_target <- target_exit.location;
		do compute_virtual_path pedestrian_graph:network final_target: final_target ;		
		
	}
	
	 
	
	reflex move when: final_target != nil and not reach_exit {
		do walk ;
		if final_target = nil {reach_exit <- true;}
	}	
	
	reflex wait_for_exiting when: reach_exit {
		if (target_exit.count < target_exit.mex_people_flow) {
			target_exit.count <- target_exit.count + 1;
			do die;
		}
	}
	
	aspect default {
		float size_triangle;
		if (all_data) {
			size_triangle <- (#zoom > 7.0) ? P_shoulder_length : (#zoom > 5 ? 2.0 : 4.0);
		} else {
			size_triangle <- (#zoom > 3.0) ? P_shoulder_length : 2.0;
		}
		draw triangle(P_shoulder_length) rotate: heading + 90 color: color depth: 0.3;
		if (current_target != nil) {draw circle(0.2) color: color at: current_target;}
	}
	
	
	aspect demo {
		if (not in_building) {
			draw triangle(P_shoulder_length) rotate: heading + 90 color: color depth: 1;
		}
	}
	
}


experiment normal_sim type: gui {
	output {
		display map type: java2D{
			graphics "bounds" refresh: false {
				draw simulation_outside color: #black;
			}
			species building refresh: false;
			species exit_place refresh: false;
			species road refresh: false;
			species people;
		}
		
		display charts {
			chart "nb of people" {
				data "nb of people" value: length(people) color: #blue;
			}
		}
	}
}

experiment test type: batch repeat: 4 keep_seed: true until: empty(people) or cycle = 100000 {
	parameter avoid_other var:avoid_other among: [false, true];
	//parameter max_exit_flow var:max_exit_flow among: [2,200];

	reflex result {
		ask simulations {
			write "avoid_other: " + avoid_other + " max_exit_flow: " + max_exit_flow + " -> " + self.cycle + ":" + length(self.people);
		}
	}
}
