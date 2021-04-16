model IDM

// Car species using IDM skill
species car skills: [idm] {
	// Default shape, length is an IDM parameter
	geometry default_shape <- rectangle(lenght, 1.5);
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
	//geometry shape <- line([{0.0, 100.0},{3000.0, 100.0}]); 
	aspect default {
		draw shape color: #grey;
	}

}

// The world
global {
	// Generator frequency in cycle
	int generate_frequency <- rnd(20, 20);
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
		// Create car from the begining of the road to the end
		create car {
			location <- road[0].shape.points[0];
			the_target <- road[0].shape.points[length(road[0].shape.points) - 1];
			next_car <- last_car;
			last_car <- self;
		}
	}

}

// Experiment
experiment "IDM using idm" {
	bool idm <- true;
	output {
		display main_window type: opengl {
			species road;
			species car;
		}

	}
}

