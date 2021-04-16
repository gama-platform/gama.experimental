model IDM

// Car species using moving skill: implement IDM
species car skills: [moving] {
	// Default shape, length is an IDM parameter
	geometry default_shape <- rectangle(lenght, 1.5);
	// The car target
	point the_target;
	// The next car
	car next_car <- nil;

	// IDM params
	float lenght <- 5.0 #m;
	float desired_speed <- 30.0 #m / #s;
	float spacing <- 1.0 #m;
	float reaction_time <- 1.5 #s;
	float max_acceleration <- 4.0 #m / #s ^ 2;
	float desired_deceleration <- 3.0 #m / #s ^ 2;

	// Dynamic params
	float acceleration <- 0.0 min: -desired_deceleration max: max_acceleration #m / #s ^ 2;
	float speed <- 0.0 #m / #s;

	// Move reflex
	reflex move {
		// Check if this is the first car or not
		if next_car = nil or dead(next_car) {
			// Compute acceleration
			acceleration <- max_acceleration * (1 - ((speed / desired_speed) ^ 4.0));
		} else {
			// Compute acceleration with the next car
			float delta_speed <- next_car.speed - speed;
			float actual_gap <- (self distance_to next_car using topology(the_graph)) - lenght;
			float desired_minimum_gap <- spacing + (reaction_time * speed) - ((speed * delta_speed) / (2 * sqrt(max_acceleration * desired_deceleration)));
			acceleration <- max_acceleration * (1 - ((speed / desired_speed) ^ 4.0) - ((desired_minimum_gap / actual_gap) ^ 2));
		}

		// Compute speed and goto
		speed <- speed + (acceleration * step);
		do goto on: the_graph target: the_target speed: speed;
		if location = the_target {
			do die();
		}

	}

	// Default aspect, rotate the default shape in the right direction
	aspect default {
		// Rotate default shape
		shape <- default_shape rotated_by heading at_location location;
		draw shape color: #blue;
	}

}

// Simple road species
species road {
	aspect default {
		draw shape color: #grey;
	}

}

// The world
global {
	// Generator frequency in cycle
	int generate_frequency <- 20;
	// Time step
	float step <- 0.1;
	// Last created car
	car last_car <- nil;
	
	// Road shapefile
	file road_shapefile <- file("../includes/road.shp");
	// The road graph
	graph the_graph;
	// World shape
	geometry shape <- envelope(road_shapefile);


	// Init the world
	init {
		// Create roads and the graph
		create road from: road_shapefile;
		the_graph <- as_edge_graph(road);
	}

	// Car generator
	reflex generate when: (cycle mod generate_frequency) = 0 {
		// Create car start from the begining of the road and the target is the end of the road
		create car {
			location <- first(road[0].shape.points);
			the_target <- last(road[0].shape.points);
			next_car <- last_car;
			last_car <- self;
		}
	}

}

// Experiment
experiment "IDM using moving" {
	output {
		display "IDM moving skill" type: opengl {
			species road;
			species car;
		}

	}
}
