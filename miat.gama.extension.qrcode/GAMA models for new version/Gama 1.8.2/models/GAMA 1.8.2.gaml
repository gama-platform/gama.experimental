/**
* Name: GAMA182
* Based on the internal skeleton template. 
* Author: Patrick Taillandier
* Tags: 
*/

model GAMA182

global control: fsm{
	float maximal_speed <- 1.0 min: 0.1 max: 15.0;
	//Factors for the group of boids
	int cohesion_factor <- 400;
	int alignment_factor <- 100; 
	//Variables for the movement of the boids
	float minimal_distance <- 5.0; 
	
	float evaporation_per_cycle <- 5.0 min: 0.0 max: 240.0 ;
	//Diffusion rate of the pheromon among the grid
	float diffusion_rate <- 0.2 min: 0.0 max: 1.0 ;
	
	rgb background const: true <- #black;
	rgb food_color const: true <- orange;
	rgb nest_color const: true <- blue;
	
	list<cell> free_places;
	
		
	float density_of_people <- 0.7 ;
	//Percentage of similar wanted for segregation
	float percent_similar_wanted <- 0.6 ;
	//Neighbours distance for the perception of the agents
	int neighbours_distance <- 10;
	//Number of people agents
	
	
	file shape_file_buildings <- file("../includes/building.shp");
	file shape_file_roads <- file("../includes/road.shp");
	file shape_file_bounds <- file("../includes/bounds.shp");
	geometry shape <- envelope(shape_file_bounds);
	float step <- 10 #s;
	int nb_people <- 200;
	float min_speed <- 10.0 #km / #h;
	float max_speed <- 20.0 #km / #h; 
	graph the_graph;
	
	int width_and_height_of_environment <- int(shape.width);  
	
	
	int bounds <- int(width_and_height_of_environment / 50); 
	int xmin <- bounds;   
	int ymin <- bounds;  
	int xmax <- round(width_and_height_of_environment /2 - bounds);     
	int ymax <- round(width_and_height_of_environment /2 - bounds);   


	int time_turn_off_light <- 110;
	int time_begin <- 130;
	int time_to_step2 <- time_begin+ 50;
	int time_to_step3 <- time_begin+ 100;
	int time_to_step4 <- time_begin+ 150;
	int time_to_step5 <- time_begin+ 200;
	
	int time_to_step6 <- time_begin+ 250;
	int step_sim <- 1;
	
	rgb blue <- rgb(54, 112, 160);
	rgb orange <- rgb(210, 103, 59);
	rgb yellow <- rgb(238, 182, 79);
	list<runner> runners <- list(runner);
	float size <- shape.width / 100;
	
	
	geometry cell2;
	geometry cell3;
	geometry cell4;
	geometry cell5;
	geometry the_cell;
	
	bool end_sim <- false;
	bool turn_off_light <- false update: cycle >= time_turn_off_light;
	
	init {
		cell2 <- rectangle(shape.width /2, shape.height/ 2) at_location {location.x * 3 /2, location.y/2};
		cell3 <- rectangle(shape.width /2, shape.height/ 2) at_location {location.x * 3 /2, 3/2 * location.y};
		cell4 <- rectangle(shape.width /2, shape.height/ 2) at_location {location.x /2,3 /2 * location.y};
		cell5 <- rectangle(shape.width /1.5, shape.height/ 1.5) ;
		create boids_goal {
			location <- {rnd(xmin,xmax),rnd(ymin,ymax)};
			init_target <- copy(location);
		}
		create boids number: 100 {
			location <- {rnd(xmin,xmax),rnd(ymin,ymax)};
			init_target <- copy(location);
		}
		
		ask cell(cell2.location) {
			is_nest <- true;
			loop c over: (self neighbors_at 2) {
				c.is_nest <- true;
			}
		}

		//Creation of the food places placed randomly with a certain distance between each
		geometry g_p <- cell2 - (circle(world.shape.width/5) at_location cell2.location);
		loop times: 4 {
			point loc <- any_location_in(g_p);
			list<cell> food_places <- (cell where ((each distance_to loc) < 10));
			ask food_places {
				if food = 0 {
					food <- 100;
					color <- food_color;
				}

			}

		}
		ask cell {
			init_is_nest <- is_nest;
			init_food <- food;
		}
		//Creation of the ants that will be placed in the nest
		geometry c <- circle(world.shape.width/10) at_location cell2.location;
		create ant number: 100  {
			location <- any_location_in(c);
			init_target <- copy(location);
		}
		
		free_places <- shuffle(cell where (each.grid_x < 50 and each.grid_y > 50));
		int number_of_people <- int( length (free_places) * density_of_people);
		//Initialization of the people
		create people number: number_of_people {
			my_place <- one_of(free_places);
			location <- my_place.location;	
			free_places >> my_place;
			
			init_target <- copy(location);	
		}	
		
		create building from: shape_file_buildings with: [type::string(read ("NATURE"))]  {
			shape <- polygon(shape.points collect {each.x /2.0, each.y /2.0});
			location <- location + {world.location.x, world.location.y};
			
			init_target <- copy(location);
		}
		create road_ag from: shape_file_roads  {
			shape <- line(shape.points collect {each.x /2.0, each.y /2.0});
			location <- location + {world.location.x, world.location.y};
			
			init_target <- copy(location);
		}
		the_graph <- as_edge_graph(road_ag);
		
		list<building> residential_buildings <- building where (each.type="Residential");
		list<building> industrial_buildings <- building  where (each.type="Industrial") ;
		create people_moving number: nb_people {
			speed <- rnd(min_speed, max_speed);
			location <- any_location_in (one_of(building)); 
			
			init_target <- copy(location);
		}
	}
	//Reflex to diffuse the pheromon among the grid
	reflex diffuse when: step_sim in [1,2]{
		diffuse var: road on: cell proportion: diffusion_rate radius: 3 propagation: gradient method: convolution;
	} 
	
	reflex to_step2 when: cycle = time_to_step2 {
		step_sim <- 2;
		the_cell <- cell2;
	}
	reflex to_step3 when: cycle = time_to_step3 {
		step_sim <- 3;
		the_cell <- cell3;
		ask cell {
			do manage_cell;
		}
		
	}
	reflex to_step4 when: cycle = time_to_step4 {
		step_sim <- 4;
		the_cell <- cell4;
	}
	reflex to_step5 when: cycle = time_to_step5 {
		step_sim <- 5;
		the_cell <- cell5;
	}
		
	int distance_between (rgb c1, rgb c2) {
		return abs(c1.red - c2.red) + abs(c1.green - c2.green) + abs(c1.blue - c2.blue);
	}

	rgb closest_color (rgb c1) {
		int db <- distance_between(c1, blue);
		int do <- distance_between(c1, orange);
		int dy <- distance_between(c1, yellow);
		int m <- min(db, do, dy);
		if (m > 100) {
			return #black;
		} else if (m = db) {
			return blue;
		} else if (m = do) {
			return orange;
		} else if (m = dy) {
			return yellow;
		} }

	action load_image (string the_path) {
		matrix<int> colors <- (image_file(the_path).contents);
		ask cell {
			color <- myself.closest_color(rgb(colors[grid_x, grid_y]));
		}

		map<rgb, list<cell>> by_color <- cell group_by (each.color);
		ask runner {
			target <- one_of(by_color at color).location;
			
		}

		runners <- list(runner);
	}

	action one_step {
		ask runners {
			do goto target: target ;
		}

		runners <- runners select ((each.location distance_to each.target.location) > 0.1);
	}


	state phase0 initial: true {
		transition to: phase1 when: cycle = time_to_step6;
	}

	state phase1 {
		enter {
			ask boids + boids_goal + ant + base_change_behavior + people + people_moving + road_ag + building{
				create runner with:(shape:to_build, color:color, final_target:init_target);
				do die;
			}
			do load_image("../images/logo.png");
		}

		do one_step();
		transition to: phase2 when: empty(runners);
	}

	state phase2 {
		enter {
			do load_image("../images/version.png");
		}

		do one_step();
		transition to: phase3 when: empty(runners);
	}
	list<cell> cell_to_reinit;
	state phase3 {
		enter {
			ask runner {
				if final_target = nil {do die;}
				else {
					target <- final_target;
				}
			}
			cell_to_reinit <- cell where (each.init_is_nest or (each.init_food > 0));
			runners <- list(runner);
			
			
		}

		do one_step();
		ask cell_to_reinit where flip(0.01){
			is_nest <- init_is_nest;
			food <- init_food;
			cell_to_reinit >> self;
		}
		if empty(runners) {
				end_sim <- true;
				ask cell_to_reinit {
					is_nest <- init_is_nest;
					food <- init_food;
			
				}
			}
			
	}

}


species runner skills: [moving] {
	rgb color <- one_of(blue, orange, yellow);
	point target;
	geometry my_shape;
	point final_target;
	float depth;
}



	//Grid used to discretize the space to place food
grid cell width: 100 height: 100 neighbors: 8  use_regular_agents: false use_individual_shapes: false {
	bool is_nest <- false;
	float road <- 0.0 max: 240.0 update: (road <= evaporation_per_cycle) ? 0.0 : road - evaporation_per_cycle;
	rgb color <- is_nest ? nest_color : ((food > 0) ? food_color : ((road < 0.001) ? background : yellow)) update: is_nest ? nest_color : ((food > 0) ?
	food_color : ((road < 0.001) ? background :yellow));
	int food <- 0;
	bool init_is_nest;
	int init_food;
	aspect default{
		draw shape color: color;
	}
	
	action manage_cell {
		if (color != background) {
			create base_change_behavior with:(shape:shape, steps_concerned: [3,4,5], color: color);
			is_nest <- false;
			road <- 0.0;
			color <- #black;
			food <- 0;
		}	
	}
	
}

//Species ant that will move and follow a final state machine
species ant skills: [moving] control: fsm parent: base_change_behavior { 
	list<int> steps_concerned <- [3,4,5];
	float speed <- 1.0;
	bool has_food <- false;
	point my_target;
	rgb color <- blue;
	
	//Reflex to place a pheromon stock in the cell
	reflex diffuse_road when: has_food = true and not(step_sim in steps_concerned) {
		cell(location).road <- cell(location).road + 100.0;
	}
	//Action to pick food
	action pick (int amount) {
		has_food <- true;
		cell place <- cell(location);
		place.food <- place.food - amount;
	}
	//Action to drop food
	action drop {
		has_food <- false;
		heading <- heading - 180;
	}
	//Action to find the best place in the neighborhood cells
	point choose_best_place {
		container list_places <- cell(location).neighbors;
		if (list_places count (each.food > 0)) > 0 {
			return point(list_places first_with (each.food > 0));
		} else {
			list_places <- (list_places where ((each.road > 0) and ((each distance_to cell2.location) > (self distance_to  cell2.location)))) sort_by (each.road);
			return point(last(list_places));
		}

	}
	//Reflex to drop food once the ant is in the nest
	reflex drop when: has_food and (cell(location)).is_nest and not(step_sim in steps_concerned){
		do drop();
	}
	//Reflex to pick food when there is one at the same location
	reflex pick when: !has_food and (cell(location)).food > 0 and not(step_sim in steps_concerned){
		do pick(1);
	}
	
	state do_nothing;
	//Initial state to make the ant wander 
	state wandering initial: true {
		if (my_target = nil) {
			my_target <- any_location_in(cell2);
		}
		do goto target: my_target;
		if location = my_target {my_target <- nil;}
		float pr <- (cell(location)).road;
		transition to: do_nothing when: (step_sim in steps_concerned);
		transition to: carryingFood when: has_food;
		transition to: followingRoad when: (pr > 0.05) and (pr < 4);
		
	}
	//State to carry food once it has been found
	state carryingFood {
		do goto(target:  cell2.location);
		transition to: do_nothing when: (step_sim in steps_concerned);
		transition to: wandering when: !has_food;
	}
	//State to follow a pheromon road if once has been found
	state followingRoad {
		point next_place <- choose_best_place();
		float pr <- (cell(next_place != nil ? next_place : location)).road;
		do goto(target: next_place);
		
		transition to: do_nothing when: (step_sim in steps_concerned);
		transition to: carryingFood when: has_food;
		transition to: wandering when: (pr < 0.05) or (next_place = nil);
	}
	
	geometry to_build -> {circle(5.0)};
	
	aspect default {
		draw circle(5.0) color: color;
	} 
}

species base_change_behavior skills: [moving]{
	point init_target;
	point target_step ;
	list<int> steps_concerned <- [2,3,4,5];
	rgb color;
	geometry to_build -> {shape};
	
	reflex step_change_loc when: step_sim in steps_concerned  {
		if target_step = nil {
			target_step <- any_location_in(the_cell);
		}
		if (target_step != nil) {
			do goto target: target_step;
		}
		if location distance_to target_step < 0.5 {
			target_step <- nil;
		}
	}
	aspect default {
		draw shape color: color;
	}
	
}

//Species boids goal which represents the goal that will be followed by boids agents using the skill moving
species boids_goal parent: base_change_behavior {
	float range  <- 20.0;
	point target <- nil;

	rgb color <- blue;
	reflex wander when: step_sim = 1{ 
		if target = nil {
			target <- {rnd(xmin,xmax),rnd(ymin,ymax)};
		} 
		do  goto target: target speed: maximal_speed / 2.0; 
		if location = target {
			target <- nil;
		} 
	}
	geometry to_build -> {circle(10)};
	
	aspect default { 
		draw circle(10) color: color;
	}
} 

//Species boids which represents the boids agents whom follow the boid goal agents, using the skill moving
species boids skills: [moving]  parent: base_change_behavior {
	//Speed of the boids agents
	float speed max: maximal_speed <- maximal_speed;
	//Range used to consider the group of the agent
	float range <- minimal_distance * 2;
	point velocity <- {0,0};
	rgb color <- orange;
	point target_step <- any_location_in(cell2);
	reflex step_change_loc when: step_sim in [2,3,4,5] {
		if (target_step != nil) {
			do goto target: target_step;
		}
		if location = target_step {
			target_step <- any_location_in(the_cell);
		}
	}
	
		
	//Reflex used when the separation is applied to change the velocity of the boid
	reflex separation  when: step_sim = 1{
		point acc <- {0,0};
		ask (boids overlapping (circle(minimal_distance)))  {
			acc <- acc - ((location) - myself.location);
		}  
		velocity <- velocity + acc;
	}
	
	//Reflex to align the boid with the other boids in the range
	reflex alignment when: step_sim = 1 {
		list others  <- ((boids overlapping (circle (range)))  - self);
		point acc <- mean (others collect (each.velocity)) - velocity;
		velocity <- velocity + (acc / alignment_factor);
	}
	 
	//Reflex to apply the cohesion of the boids group in the range of the agent
	reflex cohesion when: step_sim = 1{
		list others <- ((boids overlapping (circle (range)))  - self);
		point mass_center <- (length(others) > 0) ? mean (others collect (each.location)) : location;

		point acc <- mass_center - location;
		acc <- acc / cohesion_factor; 
		velocity <- velocity + acc;   
	}
	
	//action to represent the bounding of the environment considering the velocity of the boid
	action bounding {
		if  (location.x) < xmin {
			velocity <- velocity + {bounds,0};
		} else if (location.x) > xmax {
			velocity <- velocity - {bounds,0};
		}
			
		if (location.y) < ymin {
			velocity <- velocity + {0,bounds};
		} else if (location.y) > ymax {
			velocity <- velocity - {0,bounds};
		}
			
	}
	//Reflex to follow the goal 
	reflex follow_goal when: step_sim = 1{
		velocity <- velocity + ((first(boids_goal).location - location) / cohesion_factor);
	}
	
	//Action to move the agent  
	action do_move {  
		if (((velocity.x) as int) = 0) and (((velocity.y) as int) = 0) {
			velocity <- {(rnd(4)) -2, (rnd(4)) - 2};
		}
		point old_location <- copy(location);
		do goto target: location + velocity;
		velocity <- location - old_location;
	}
	
	//Reflex to apply the movement by calling the do_move action
	reflex movement when: step_sim = 1 {
		do do_move;
		do bounding;
	}
	
	geometry to_build -> {triangle(15)};
	
	
	aspect default { 
		draw  triangle(15) rotate: heading  color: color;
	}
	
} 

//Species people representing the people
species people  parent: base_change_behavior { 
	list<int> steps_concerned <- [5];
	
	//Size of the people agent
	float size const: true <- 2.0;
	//Color of the people agent  
	rgb color const: true <- one_of ([blue, yellow, orange]); 
	//Building in which the agent lives
	cell my_place <- nil;
	//List of all the neighbour people agents
	list<people> my_neighbours update:  step_sim in [1,2,3,4] ? people at_distance neighbours_distance : []; 
	int similar_nearby -> 
		(my_neighbours count (each.color = color))
	;
	//Computation of the total neighbours nearby
	int total_nearby -> 
		length (my_neighbours)
	;
	//Boolean to know if the agent is happy or not
	bool is_happy -> similar_nearby >= (percent_similar_wanted * total_nearby ) ;
	//Reflex to migrate the people agent when it is not happy 
	reflex migrate when:(every(3#cycle) and (flip(0.01) or !is_happy)) and step_sim in [1,2,3,4]{
		//Add the place to the free places as it will move to another place
		free_places << my_place;
		//Change the place of the agent
		my_place <- one_of(free_places);
		location <- my_place.location; 
		//Remove the new place from the free places
		free_places >> my_place;
	}
	
	
	geometry to_build -> {square(7) };

	aspect default {
		draw square(7) color: color;
	}
}



species building parent: base_change_behavior {
	string type; 
	rgb color <- yellow  ;
	list<int> steps_concerned <- [4,5];
	
	
	aspect default {
		draw shape color: color ;
	}
}

species road_ag  parent: base_change_behavior {
	rgb color <- orange;
	list<int> steps_concerned <- [4,5];
	geometry to_build -> {shape + 1.0};

	
	aspect default {
		draw shape + 1.0 color: color ;
	}
}

species people_moving parent: base_change_behavior {
	rgb color <- blue ;
	list<int> steps_concerned <- [4,5];
	point the_target <- nil ;
		
	reflex time_to_move when: step_sim in [1,2,3] and the_target = nil and flip(0.05){
		the_target <- any_location_in (one_of(building));
	}
		
	
	reflex move when: the_target != nil and step_sim in [1,2,3]{
		do goto target: the_target on: the_graph ; 
		if the_target = location {
			the_target <- nil ;
		}
	}
	
	geometry to_build -> {circle(5)};
	
	aspect default {
		draw circle(5) color: color border: #black;
	}
}

experiment "Run me!" type: gui {
	output {
		
		display view background: #black type: opengl axes: false {
			light #ambient intensity: (end_sim or not turn_off_light) ? 150 : 0;
			
			light "the_spot" active: not end_sim and (cycle >= time_turn_off_light)  type: #spot location: {world.shape.width / 2, world.shape.height / 2, world.shape.width / 2} direction: {0, 0, -1} 
			intensity: min(200,3 * (cycle - time_turn_off_light)) show: false angle: 60  dynamic: true;
			
			agents "Grid" value: cell where ((each.food > 0) or (each.road > 0) or (each.is_nest));
			
			species ant   ;
			species runner;
			species boids_goal;
			species boids;
			
			species people ;
			species base_change_behavior;
			
			species building ;
			species road_ag ;
			species people_moving ;
		}
	}
}
