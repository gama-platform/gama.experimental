/**
* Name: Building
* Based on the internal empty template. 
* Author: jferdelyi
* Tags: 
*/
model Building

species Building {
	string type;

	aspect default {
		if type = "working" {
			draw shape color: #red border: #black;			
		} else {
			draw shape color: #grey border: #black;			
		}
	}

}
