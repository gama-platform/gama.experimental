/***
* Name: GAMA 1.8 is out ! 
* Author: A. Drogoul
* Description: A toy model demonstrating "morphing" technologies in GAMA
* Tags: image, geometries
***/
model GAMA

global control: fsm {
	rgb blue <- rgb(54, 112, 160);
	rgb orange <- rgb(210, 103, 59);
	rgb yellow <- rgb(238, 182, 79);
	rgb blue_transparency <- rgb(156, 183, 204);
	rgb orange_transparency <- rgb(230, 180, 157);
	rgb yellow_transparency <- rgb(247, 217, 167);
	
	list<runner> runners <- list(runner);
	shape_file town_file <- shape_file("../includes/buildings2.shp");
	shape_file road_file <- shape_file("../includes/roads.shp");
	geometry shape <- envelope(town_file);
	float size <- shape.width / 100;
	graph the_graph <- as_edge_graph(road_file.contents);
	
	int distance_between (rgb c1, rgb c2) {
		return abs(c1.red - c2.red) + abs(c1.green - c2.green) + abs(c1.blue - c2.blue);
	}

	rgb closest_color (rgb c1) {
		int db <- distance_between(c1, blue);
		int do <- distance_between(c1, orange);
		int dy <- distance_between(c1, yellow);
		int dyt <- distance_between(c1, yellow_transparency);
		int dot <- distance_between(c1, orange_transparency);
		int dbt <- distance_between(c1, blue_transparency);
		int m <- min(db, do, dy, dyt, dot, dbt);
		if (m > 120) {
			return #black;
		} else if (m = db) {
			return blue;
		} else if (m = do) {
			return orange;
		} else if (m = dy) {
			return yellow;
		} else if (m = dyt) {
			return yellow_transparency;
		} else if (m = dot) {
			return orange_transparency;
		} else if (m = dbt) {
			return blue_transparency;
		} 
	}

	action load_image (string the_path) {
		matrix<int> colors <- (image_file(the_path).contents);
		ask background {
			rgb col <- rgb(colors[grid_x, grid_y]);
			color <- myself.closest_color(col);
		}

		map<rgb, list<background>> by_color <- background group_by (each.color);
		ask runner {
			target <- one_of(by_color at target_color);
			color_transparency <- (target_color in [blue_transparency, orange_transparency, yellow_transparency]);
		}

		runners <- list(runner);
	}

	action one_step {
		ask runners {
			do goto target: target speed: 2 * size / #s;
			if myself.state = "phase1" and location = target.location {
				should_be_transparent <- color_transparency;
			}
		}
		

		runners <- runners select (each.location != each.target.location);
	}

	init {
		ask (list(background) every 16) {
			create runner with: (location: location);
		} }

	state phase0 initial: true {
		enter {
			ask runner {
				my_shape <- cube(rnd(size, size * 2));
				depth <- rnd(size * 2);
			}

		}

		ask runner {
			my_shape <- cube(rnd(size, size * 2));
			depth <- rnd(size * 2);
		}

		transition to: phase1 when: cycle = 10;
	}

	state phase1 {
		enter {
			ask runner {
				my_shape <- cube(rnd(size, size * 2));
				depth <- rnd(size * 2);
			}

			do load_image("../includes/gama.jpg");
		}

		do one_step();
		transition to: phase2 when: empty(runners);
	}



	state phase2 {
		enter {
			ask (runner) {
				if (flip(0.2)) {
					do die;
				}
				

			}
			ask (runner select (each.color = yellow)) {
				create people with: (location: location);
				do die;
			}

			ask (runner select (each.color = orange)) {
				create roads with: (location: location);
				do die;
			}

			ask runner {
				target <- one_of(town_file.contents);
			}

			runners <- list(runner);
		}

		ask runners {
			do goto target: target speed: 2 * size / #s;
				should_be_transparent <- should_be_transparent and flip(0.1);
		
			if (location = target.location) {
				my_shape <- target;
			}

		}

		runners <- runners select (each.location != each.target.location);
	} }

grid background width: 297 height: 297 ;

species roads skills: [moving] {
	geometry target <- one_of(road_file.contents);
	geometry my_shape <- cube(rnd(size, size * 2));

	reflex go when: target != nil {
		do goto target: target speed: 2 * size / #s;
		if (location = target.location) {
			my_shape <- target + size / 8;
		}

	}

	aspect default {
		draw my_shape at: location color: orange;
	}

}

species people skills: [moving] {
	init {
		target <- one_of(road_file.contents);
	}

	geometry target;
	rgb color;

	reflex move when: target != nil {
		do goto on: the_graph target: target speed: 2 * size / #s;
		if (location = target.location) {
			target <- one_of(road_file.contents);
		}

	}

	aspect default {
		draw circle(size / 4) color: yellow at: location + {0, 0, size};
	}

}

species runner skills: [moving] {
	rgb target_color <- one_of(blue, orange, yellow, blue_transparency, orange_transparency, yellow_transparency);
	
	rgb color <- target_color = blue_transparency? blue :(target_color = orange_transparency ? orange : (target_color = yellow_transparency ? yellow : target_color) );
	
	geometry target;
	geometry my_shape;
	float depth;
	bool color_transparency <- false;
	bool should_be_transparent <- false;
	
}

experiment "Run me !" type: gui autorun: true {
	output {
		display "1.8" type: opengl fullscreen: false toolbar: #black synchronized: true background: #black axes: false {
			camera #default location: {1298.0375, 3277.2938, 2177.5545} target: {1261.3366, 1174.7007, 0.0};
			//grid background;
			species roads;
			species runner {
				if should_be_transparent  {
					draw my_shape at: location depth: depth color: rgb(color.red, color.green, color.blue, 0.1);
				} else {
					draw my_shape at: location depth: depth color: color ;
				}
				
			}

			species people;
		}

	}

}