/**
* Name: Road
* Based on the internal empty template. 
* Author: jferdelyi
* Tags: 
*/
model Road

import "../Utility/Scheduler.gaml"

species Road skills: [jdeqsimroad] {
	int lanes; // From shapefile 
	float maxspeed; // From shapefile
	init {
		do init(Scheduler[0], maxspeed #km / #h, 3600.0 #s, lanes, shape.perimeter);
	}

	aspect default {
		draw shape + lanes color: rgb(255 * (capacity / max_capacity), 0, 0) border: #black;
	}

}
