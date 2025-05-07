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
	point trans <- {2.0, 2.0};
	geometry displayed_shape;

	init {
		point A <- first(shape.points);
		point B <- last(shape.points);
		if (A = B) {
			trans <- {0, 0};
		} else {
			point u <- {-(B.y - A.y) / (B.x - A.x), 1};
			float angle <- angle_between(A, B, A + u);
			if (angle < 150) {
				trans <- u / norm(u);
			} else {
				trans <- -u / norm(u);
			}

		}

		displayed_shape <- (shape + lanes) translated_by (trans * 2);
		do init(Scheduler[0], maxspeed #km / #h, 3600.0 #s, lanes, shape.perimeter);
	}

	aspect default {
		draw displayed_shape border: #gray color: rgb(255 * (capacity / max_capacity), 0, 0);
	}

}
