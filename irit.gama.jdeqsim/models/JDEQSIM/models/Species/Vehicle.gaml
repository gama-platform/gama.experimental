/**
* Name: Vehicle
* Based on the internal empty template. 
* Author: jferdelyi
* Tags: 
*/
model Vehicle

import "Person.gaml"

global {
	Car create_car (Person ownerPerson) {
		create Car returns: values {
			do init_vehicle(ownerPerson);
		}

		return values[0];
	}

	Bike create_bike (Person ownerPerson) {
		create Bike returns: values {
			do init_vehicle(ownerPerson);
		}

		return values[0];
	}

	Truck create_truck (Person ownerPerson) {
		create Truck returns: values {
			do init_vehicle(ownerPerson);
		}

		return values[0];
	}

}

species Vehicle virtual: true skills: [jdeqsimvehicle] {

	action init_vehicle (Person ownerPerson);
}

species Car parent: Vehicle {

	action init_vehicle (Person ownerPerson) {
		location <- ownerPerson.location;
		do init(Scheduler[0], 130.0 #km / #h, 4.5 #m, ownerPerson);
	}

	aspect default {
		draw circle(2) color: #cyan border: #black;
	}

}

species Bike parent: Vehicle {

	action init_vehicle (Person ownerPerson) {
		location <- ownerPerson.location;
		do init(Scheduler[0], 20.0 #km / #h, 1.0 #m, ownerPerson);
	}

	aspect default {
		draw circle(2) color: #green border: #black;
	}

}

species Truck parent: Vehicle {

	action init_vehicle (Person ownerPerson) {
		location <- ownerPerson.location;
		do init(Scheduler[0], 110.0 #km / #h, 15.0 #m, ownerPerson);
	}

	aspect default {
		draw circle(2) color: #red border: #black;
	}

}
