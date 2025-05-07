/**
* Name: World
* Based on the internal empty template. 
* Author: jferdelyi
* Tags: 
*/
model World

import "Species/Vehicle.gaml"
import "Species/Node.gaml"
import "Species/Road.gaml"
import "Species/Person.gaml"
import "Species/Building.gaml"
import "Utility/Scheduler.gaml"

global {
	date starting_date <- date([1970, 1, 1, 0, 0, 0]);
	float step <- 10 #seconds;
	string dataset_path <- "../includes/CASTANET-TOLOSAN-CROPPED/";
	shape_file shape_roads <- shape_file(dataset_path + "roads.shp");
	shape_file shape_nodes <- shape_file(dataset_path + "nodes.shp");
	shape_file shape_boundary <- shape_file(dataset_path + "boundary.shp");
	shape_file shape_buildings <- shape_file(dataset_path + "buildings.shp");
	shape_file shape_individuals <- shape_file(dataset_path + "individuals.shp");
	geometry shape <- envelope(shape_boundary);
	graph full_network;

	init {
		date init_date <- (starting_date + (machine_time / 1000));
		date now <- init_date;
		write "Scheduler...";
		create Scheduler;
		init_date <- now;
		now <- (starting_date + (machine_time / 1000));
		write "done in " + milliseconds_between(init_date, now) + "ms";
		write "Building...";
		create Building from: shape_buildings;
		init_date <- now;
		now <- (starting_date + (machine_time / 1000));
		write "done in " + milliseconds_between(init_date, now) + "ms";
		write "Road...";
		create Road from: shape_roads;
		init_date <- now;
		now <- (starting_date + (machine_time / 1000));
		write "done in " + milliseconds_between(init_date, now) + "ms";
		write "Node...";
		create Node from: shape_nodes;
		init_date <- now;
		now <- (starting_date + (machine_time / 1000));
		write "done in " + milliseconds_between(init_date, now) + "ms";
		write "Full network...";
		full_network <- as_driving_graph(Road, Node);
		init_date <- now;
		now <- (starting_date + (machine_time / 1000));
		write "done in " + milliseconds_between(init_date, now) + "ms";
		write "Get buildings...";
		list<Building> working_buildings <- Building where (each.type = "working");
		list<Building> home_buildings <- Building where (each.type != "working");
		init_date <- now;
		now <- (starting_date + (machine_time / 1000));
		write "done in " + milliseconds_between(init_date, now) + "ms";
		write "Person...";
		create Person from: shape_individuals {
			do set_data(one_of(working_buildings), one_of(home_buildings), full_network);
		}
		init_date <- now;
		now <- (starting_date + (machine_time / 1000));
		write "done in " + milliseconds_between(init_date, now) + "ms";
	}

}

experiment "JDEQSIM demo" type: gui {
	output {
		display main_window type: opengl {
			species Road;
			species Node;
			species Building;
			species Person;
			species Bike;
			species Car;
			species Truck;
		}

	}

}
