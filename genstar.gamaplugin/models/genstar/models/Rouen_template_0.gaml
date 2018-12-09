/**
* Name: Rouentemplate
* Author: Patrick Taillandier
* Description: Describe here the model and its experiments
* Tags: Tag1, Tag2, TagN
*/

model Rouentemplate

global {
	init {
		create people number: 10000;
	}
}
species people {
	int Age;
	string CSP;
	string Sexe;
	string iris;
	string Couple;
	
	aspect default { 
		draw circle(0.5) color: #red border: #black;
	}
}


experiment Rouentemplate type: gui {
	output {
		display map {
			species people;
		}
	}
}
