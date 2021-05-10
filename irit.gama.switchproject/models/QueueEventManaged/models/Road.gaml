/**
* Name: Queue
* Queue and event manager demo. 
* Author: Jean-François Erdelyi
* Tags: 
*/
model Road

/**
 * Initialisation of the model
 */
global {
// Road and cars params
	float road_width <- 3.5 #m const: true;
	float road_height <- 30 #m const: true;
	float car_width <- 1.5 #m const: true;
	float car_height <- 4.0 #m const: true;

	// Shape of the world
	geometry shape <- rectangle(road_width, road_height);

	// Temporal param
	float step <- 1 #s;
	date starting_date <- date([1970, 1, 1, 0, 0, 0]);

	// Cars generator rate (+1 => arithmetic error if value is 0)
	int generate_frequency <- 2 update: rnd(1, 10) + 1;

	// Create manager and road
	init {
		create manager;
		create queue_road;
	}

	// Generate cars 
	reflex generate when: (cycle mod generate_frequency) = 0 {
		ask (queue_road) {
			do add_car();
		}

	}

}

/** 
 * Event manager species
 */
species manager control: event_manager {

	// Reflex to write the size of event queues
	reflex write_size {
		write "[" + name + "]::[write_size] manager size = " + size + " at " + (starting_date + time);
	}

}

/**
 * The car species with scheduling skill in order to use the action 'later'
 */
species car skills: [scheduling] {
	// General aspect
	geometry shape <- rectangle(car_width, car_height) at_location point(road_width / 2.0, road_height);
	rgb color <- rnd_color(255);

	// The event manager used to schedule cars
	agent event_manager <- manager[0];

	// Last word and die 
	action die_in_peace (string last_word) {
		write "Peace " + last_word;
		ask queue_road[0] {
			do change_locations;
		}

		do later the_action: die at: (event_date + 1);
	}

	// Default aspect
	aspect default {
		draw shape color: color;
	}

}

/**
 * The queue road species with scheduling skill in order to use the action 'later'
 */
species queue_road skills: [scheduling] {
	// Capacity
	int nb_car_max <- 5;

	// Representation of cars inside the road
	queue<car> cars_queue;

	// General aspect
	geometry shape <- rectangle(road_width, road_height) at_location point(road_width / 2.0, road_height / 2.0);

	// Free flow travel time (fake)
	int time_to_travel <- 10;

	// The event manager used to schedule road
	agent event_manager <- manager[0];

	// Pop car
	action pop_car {
		write "Pop car at: " + event_date;

		// Pop car
		if (length(cars_queue) > 0) {
			car c <- pop(cars_queue);

			// Die
			ask c {
				do later the_action: die_in_peace with_arguments: map("last_word"::"OK"); // at is null -> no
			}

		}

	}

	// Change location of each cars
	action change_locations {
		int i <- 0;
		loop c over: car {
			c.location <- {road_width / 2.0, c.location.y - (car_height + (car_height / 2.0))};
			i <- i + 1;
		}

	}

	// Add new car
	action add_car {
		if (length(cars_queue) < nb_car_max) {
			// Create and get new car
			create car returns: new_cars;
			car new_car <- new_cars[0];

			// Set location and time
			new_car.location <- {road_width / 2.0, length(cars_queue) * (car_height + (car_height / 2.0)) + 0.5 + (car_height / 2.0)};
			date t <- starting_date + time + (time_to_travel);

			// Push car to queue
			push item: new_car to: cars_queue;

			// Schedule pop
			do later the_action: pop_car at: t;
		}

	}

	// Default aspect
	aspect default {
		draw shape color: rgb(255 * (1 - (nb_car_max - length(cars_queue)) / nb_car_max), 0, 0);
	}

}

/**
 * Experiments
 */
experiment Road type: gui {
	output {
		display main_window type: opengl {
			species queue_road;
			species car;
		}

	}

}
