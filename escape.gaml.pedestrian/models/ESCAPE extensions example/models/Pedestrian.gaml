/***
* Name: Openenvironment
* Author: admin_ptaillandie
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model Openenvironment

global {
	bool snapsave <- true;
	float environment_size <- 50.0 parameter: true;
	float margin <- 2.0;
	string scenario <- "frontal crossing" among: ["big crowd", "frontal crossing", "perpandicular crossing"] ;
	float step <- 0.3 min: 0.1 max: 1.0 step: 0.1 parameter: true;
				
	int nb_people <-  500 ;
	int nb_obstacles <- 0 parameter: true;
	float P_shoulder_length <- 0.45 parameter: true;
	float P_body_depth <- 0.28 parameter: true;
	bool P_use_body_geometry <- false parameter: true ;
	float P_proba_detour <- 0.5 parameter: true ;
	bool P_avoid_other <- true parameter: true ;
	float P_obstacle_consideration_distance <- 1.0 parameter: true ;
	
	
	string P_pedestrian_model among: ["simple", "SFM"] <- "SFM" parameter: true ;
	float P_obstacle_distance_repulsion_coeff <- 5.0 category: "simple model" parameter: true ;
	float P_overlapping_coefficient <- 2.0 category: "simple model" parameter: true ;
	float P_perception_sensibility <- 1.0 category: "simple model" parameter: true ;
	
	float P_A_SFM parameter: true <- 4.5 category: "SFM" ;
	float P_relaxion_SFM parameter: true <- 0.54 category: "SFM" ;
	float P_gama_SFM parameter: true <- 0.35 category: "SFM" ;
	float P_n_SFM <- 2.0 parameter: true category: "SFM" ;
	float P_n_prime_SFM <- 3.0 parameter: true category: "SFM";
	float P_lambda_SFM <- 2.0 parameter: true category: "SFM" ;
	
	geometry shape <- square(environment_size);
	geometry free_space <- copy(shape);
	geometry left_space;
	geometry right_space;
	geometry bottom_space;
	geometry top_space;
	init {
		left_space <- polygon([{0,0}, {0, environment_size}, {environment_size/10, environment_size}, {environment_size/10,0}]);
		right_space <- polygon([{environment_size,0}, {environment_size, environment_size}, {9 * environment_size/10, environment_size}, {9 * environment_size/10,0}]);
		bottom_space <- polygon([{0, environment_size}, {0, 9 * environment_size/10}, {environment_size,9*  environment_size/10}, {environment_size, environment_size}]);	
		top_space <- polygon([{0, 0}, {0, environment_size/10}, {environment_size, environment_size/10}, {environment_size, 0.0}]);	
		create obstacle number:nb_obstacles {
			shape <- sphere(2+rnd(environment_size/20.0));
			location <- any_location_in(myself.shape scaled_by 0.8);
			free_space <- free_space - shape;
		}
		
		create people number: nb_people {
			pedestrian_model <- P_pedestrian_model;
			obstacle_distance_repulsion_coeff <- P_obstacle_distance_repulsion_coeff;
			obstacle_consideration_distance <-P_obstacle_consideration_distance;
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
			
			obstacle_species <- [people];
			if (P_use_body_geometry) {shape <- compute_body();}
			if (nb_obstacles > 0) {obstacle_species<<obstacle;}
			switch scenario {
				match "frontal crossing" {
					int id <- int(self);
					location <- any_location_in(even(id) ? left_space : right_space);
					current_target <- closest_points_with(location, even(id) ? right_space : left_space)[1];
				} match "perpandicular crossing" {
					int id <- int(self);
					location <- any_location_in(even(id) ? left_space : bottom_space);
					current_target <- closest_points_with(location, (even(id) ? right_space : top_space))[1];
				} match "big crowd" {
					location <- any_location_in(free_space);
					current_target <- any_location_in(world.shape.contour);
				}
			}
		}
	}
	
	reflex end_simulation when: empty(people) {
		do pause;
	}
}

species people skills: [pedestrian] schedules: shuffle(people) {
	rgb color <- rnd_color(255);
	point current_target;
	float speed <- 3 #km/#h;
	bool avoid_other <- true;
	geometry shape update: P_use_body_geometry ? compute_body(): shape;
	
	reflex move when: current_target != nil{
		if (nb_obstacles > 0) {
			do walk target: current_target bounds: free_space;
		} else {
			do walk target: current_target;
		}
		if (self distance_to current_target < 0.5) {
			do die;
		}
	}
	aspect default {
		if (P_use_body_geometry) {
			draw shape color: color border: #black;
		} else {
			draw triangle(shoulder_length) color: color rotate: heading + 90.0;
		}	
	}
}

species obstacle {
	aspect default {
		draw shape color: #gray border: #black;
	}
}
experiment big_crowd type: gui {
	float minimum_cycle_duration <- 0.1;
	action _init_ {
		create simulation with: [scenario :: "big crowd", nb_people::1000];
	}
	output {
		display map synchronized:true autosave:snapsave {
			species obstacle;
			species people;
		}
	}
}

experiment frontal_crossing type: gui {
	float minimum_cycle_duration <- 0.1;
	action _init_ {
		create simulation with: [scenario :: "frontal crossing", nb_people::200];
	}
	output {
		display map synchronized:true autosave:snapsave {
			graphics "areas" transparency: 0.5{
				draw right_space color: #green border: #black;
				draw left_space color: #red border: #black;
			}
			species obstacle;
			species people;
		}
	}
}
experiment perpandicular_crossing type: gui {
	float minimum_cycle_duration <- 0.1;
	action _init_ {
		create simulation with: [scenario :: "perpandicular crossing", nb_people::200];
	}
	
	output {
		display map synchronized:true autosave:snapsave {
			graphics "areas" transparency: 0.7{
				draw right_space color: #green border: #black;
				draw left_space color: #red border: #black;
				draw bottom_space color: #yellow border: #black;
				draw top_space color: #magenta border: #black;
			}
			species obstacle;
			species people;
		}
	}
}
