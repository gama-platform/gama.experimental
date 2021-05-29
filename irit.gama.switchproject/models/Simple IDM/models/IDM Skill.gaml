model IDM

// Car species using IDM skill
species car skills: [idm] {
	// Default shape, length is an IDM parameter
	geometry default_shape <- rectangle(length, 1.5);
	// The car target
	point the_target;
	// The next car
	car next_car <- nil;

	// Relfex move, using supersedes goto (from moving skill)
	reflex move {
		// Do goto to target, on the graph and follow the next car
		do goto target: the_target on: the_graph follow: next_car;
		
		// If location is reached then kill the car
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
experiment "IDM using IDM skill" {
	output {
		display "IDM skill" type: opengl {
			species road;
			species car;
		}

	}
}
