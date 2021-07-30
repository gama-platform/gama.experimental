/**
* Name: Person
* Based on the internal empty template. 
* Author: jferdelyi
* Tags: 
*/
model Person

import "../Utility/Scheduler.gaml"
import "Road.gaml"
import "Vehicle.gaml"
import "Building.gaml"
species Person skills: [jdeqsimperson] {
	Building home;
	Building work;
	Vehicle currentVehicle;

	init {
		do init(Scheduler[0]);
	}

	action set_plan (date endDate, list<Road> roads, Building end, int random <- 0) {
		do add_leg(roads);
		do add_activity(endDate, 0.0, last(roads), end);
	}

	action set_data (Building workBuilding, graph full_network) {
		int random <- rnd(0, 0);
		home <- one_of(Building);
		location <- home.location;
		work <- workBuilding;
		Road homeRoad <- Road closest_to home;
		do add_activity(date([1970, 1, 1, 0, 0, 0]) + random, 0.0, homeRoad, home);
		path the_path <- path_between(full_network, home, work);
		if the_path = nil {
			do die;
		}

		list<Road> path_to_target <- list<Road>(the_path.edges);
		do set_plan(date([1970, 1, 1, 16, 30, 0]), path_to_target, work, random);
		the_path <- path_between(full_network, work, home);
		if the_path = nil {
			do die;
		}

		path_to_target <- list<Road>(the_path.edges);
		do set_plan(date([1970, 1, 2, 6, 0, 0]), list<Road>(the_path.edges), home);
		Vehicle v <- world.create_car(self);
	}

	aspect default {
		draw circle(1) color: #red border: #black;
	}

}
