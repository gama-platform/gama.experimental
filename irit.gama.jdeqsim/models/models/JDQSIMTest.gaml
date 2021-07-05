/**
* Name: JDQSIMTest
* JDQSIM test. 
* Author: Jean-François Erdelyi
* Tags: 
*/
model JDQSIMTest

species Road skills: [jdeqsimroad] {
	string z <- "jdeqsimroad";

	reflex road_test {
		write test(name, string(time));
	}

}

species Vehicle skills: [jdeqsimvehicle] {
	string c <- "jdeqsimvehicle";

	reflex vehicle_test {
		write test(name, string(time));
	}

}

global {

	init {
		create Road;
		create Vehicle;
	}

}

experiment "JDEQSIM demo" {
}