
model Localization

global {
	float min_dist <- 1.0 min: 0.0 max: 50.0 parameter: true;
	float max_dist <- 5.0 min: 0.0 max: 100.0 parameter: true;
	
	list<geometry> nests;
	init {
		create road with: (shape: line([{10, 50},{90, 50}]));
		create road with: (shape: line([{50, 10},{50, 90}]));
		create people number: 100;
		localize species: people nests: road distribution:"area" min_dist:min_dist max_dist:max_dist  nest_attribute: "nest" ;	
		nests <- remove_duplicates(people collect each.nest);
	}
} 

species road {
	aspect default {
		draw shape color: #blue;
	}
}

species people {
	geometry nest;
	rgb color <- rnd_color(255);
	
	aspect default {
		draw circle(0.2) color: color;
	}
}

experiment Localization type: gui {
	output {
		display map {
			graphics "nests" {
				loop n over: nests {
					draw n color: #lightgray;
				}
			}
			species road;
			species people;
		}
	}
}
