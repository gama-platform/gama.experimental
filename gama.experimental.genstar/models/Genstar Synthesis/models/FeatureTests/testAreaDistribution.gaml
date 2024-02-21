//Allows to test the impact of the area distribution on the choice of location
model Localization

global {
	bool area_distribution <- true parameter: true;
	init {
		create building with: (shape: square(10) at_location {10,50});
		create building with: (shape: square(40) at_location {60,50});
		create people number: 100;
		if area_distribution {
			localize species: people distribution:"area" nests: building nest_attribute: "nest" ;	
		} else {
			localize species: people nests: building nest_attribute: "nest" ;	
		
		}
	}
} 

species building {
	aspect default {
		draw shape color: #gray;
	}
}

species people {
	int Age;
	geometry nest;
	string Sexe;
	string iris;
	rgb color <- rnd_color(255);
	
	aspect default {
		draw circle(0.2) color: color;
	}
}

experiment Localization type: gui {
	output {
		display map {
			species building;
			species people;
		}
	}
}
