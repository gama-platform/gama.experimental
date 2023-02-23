/**
* Name: TestSimple
* Based on the internal empty template. 
* Author: benoitgaudou
* Tags: 
*/


model TestSimple

global {
	// Temporal param
	float step <- 1 #s;
	date starting_date <- date([1970, 1, 1, 0, 0, 0]);

	init {
		create manager;
		create people number: 2;
		
		ask people {
			loop t from: 10 to: 100 step: 10 {
				do later the_action: "info" at: starting_date + t#s refer_to: people(0) ;
			}
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
species people skills: [scheduling] {
	// The event manager used to schedule cars
	agent event_manager <- manager[0];
	
	action info {
		write sample(self) + " - " + sample(current_date) + ' - ' + sample(cycle);
	}
}


experiment name type: gui {
	output {

	}
}