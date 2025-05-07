/**
* Name: NewModel
* Based on the internal empty template. 
* Author: jferdelyi
* Tags: 
*/
model UT

species Tester skills: [jdeqsimunittest] {

	init {
		do test();
	}

}

global {

	init {
		create Tester;
	}

}

experiment "JDEQSIM Unit Test" {
}

