/***
* Name: Openenvironment
* Author: admin_ptaillandie
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model Openenvironment

global {
	float environment_size <- 50.0 parameter: true;
	float margin <- 2.0;
	
	
				
	int nb_people <- 50 parameter: true;
	int nb_obstacles <- 0 parameter: true;
	bool random <- true parameter: true;
	float P_obstacle_distance_repulsion_coeff <- 5.0 parameter: true;
	float P_obstacle_repulsion_intensity <- 1.0 parameter: true;
	float P_overlapping_coefficient <- 2.0 parameter: true;
	float P_perception_sensibility <- 1.0 parameter: true;
	float P_shoulder_length <- 0.5 parameter: true;
	float P_proba_detour <- 0.5 parameter: true;
	bool P_avoid_other <- true parameter: true;
	
	geometry shape <- square(environment_size);
	geometry free_space <- copy(shape);
	
	init {
		create obstacle number:nb_obstacles {
			shape <- sphere(2+rnd(environment_size/20.0));
			location <- any_location_in(myself.shape scaled_by 0.8);
			free_space <- free_space - shape;
		}
		
		create people number: nb_people {
			obstacle_distance_repulsion_coeff <- P_obstacle_distance_repulsion_coeff;
			obstacle_repulsion_intensity <-P_obstacle_repulsion_intensity;
			overlapping_coefficient <- P_overlapping_coefficient;
			perception_sensibility <- P_perception_sensibility ;
			shoulder_length <- P_shoulder_length;
			avoid_other <- P_avoid_other;
			proba_detour <- P_proba_detour;
			obstacle_species <- [people];
			if (nb_obstacles > 0) {obstacle_species<<obstacle;}
			
			if (not random) {
				int id <- int(self);
				float environment_size_wm <- environment_size - margin;
				float y <- margin + int(id/2) * environment_size_wm / (nb_people/2.0) ;
				location <- {even(id) ? margin : environment_size_wm , y + rnd(1.0)};
				if (even(id)) {
					current_target <- {environment_size_wm,location.y};
				} else {
					current_target <- {margin,location.y};
				}
			} else {
				location <- any_location_in(free_space);
			} 
		}
	}
}

species people skills: [pedestrian] schedules: shuffle(people) {
	rgb color <- rnd_color(255);
	point current_target;
	float speed <- 3 #km/#h;
	bool avoid_other <- true;
	
	reflex choose_target when: random and current_target = nil {
		current_target <- 
				any_location_in(free_space);
	}
	reflex move when: current_target != nil{
		if (nb_obstacles > 0) {
			do walk target: current_target bounds: free_space;
		} else {
			do walk target: current_target;
		}
		if (self distance_to current_target < 0.1) {
			current_target <- nil;
		}
	}
	aspect default {
		draw triangle(shoulder_length) color: color rotate: heading + 90.0;
	}
}

species obstacle {
	aspect default {
		draw shape color: #gray border: #black;
	}
}
experiment Openenvironment type: gui {
	float minimum_cycle_duration <- 0.07;
	output {
		display map synchronized: true{
			species obstacle;
			species people;
		}
	}
}
