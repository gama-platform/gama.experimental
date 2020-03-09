/***
* Name: BiNetwork
* Author: kevinchapuis
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model BiNetwork

/* Insert your model definition here */

global {
	
	file regular_road <- file("../includes/roads.shp");
	file pedestrian_area <- file("../includes/pedestrianArea.shp");
	file obstacles <- file("../includes/obstacles.shp");
	
	list<geometry> pedestrian_corridors;
	
	geometry shape <- envelope(regular_road);
	
	bool mixed_corridors <- false parameter:true;
	bool reduced_angular_distance <- true parameter:true;
	int nb_people parameter:true init:1;
	
	string pedestrian_model parameter:true init:"SFM" among:["SFM","simple"];
	
	graph pedestrian_network;
	
	init {
		
		create walking_area from:pedestrian_area;
		create obstacle from:obstacles;
		create road from:regular_road;
		create people number:nb_people with:[location::any_location_in(any(obstacle)),rad::reduced_angular_distance,pedestrian_model::pedestrian_model];
		
		if(mixed_corridors){
			// With mixed corridor
			pedestrian_corridors <- generate_pedestrian_network([obstacle],walking_area,road,false,false,1.0,0.1,false,0.1,0.0,0.0);
		} else {
			// Only 2D / continuous space corridor
			pedestrian_corridors <- generate_pedestrian_network([obstacle],walking_area,false,false,10.0,0.001,true,0.1,0.0,0.0);
		}
				
		write "Continuous space corridors : "+pedestrian_corridors count (each["road_status"]=1);
		write "Simple 1D corridors : "+pedestrian_corridors count (each["road_status"]=0);
		//save pedestrian_corridors type:"shp" to:pedestrian_file_roads;
		
		create corridor from: pedestrian_corridors { do initialize distance:10#m obstacles:[obstacle]; }
		
		pedestrian_network <- as_edge_graph(corridor);
		path a_path <- pedestrian_network path_between(geometry(first(pedestrian_network.vertices)), geometry(last (pedestrian_network.vertices)));
		write "edges:" + length(a_path.edges);
		write "segments:" + length(a_path.segments);
		
		if(reduced_angular_distance) {
			ask corridor {
				do build_exit_hub pedestrian_graph:pedestrian_network distance_between_targets: 3.0;
			}
		}
		
	}
	
}

species obstacle {
	
}

species walking_area {
	aspect default {
		draw shape color:#green;
	}
}

species road {
	aspect default {
		draw shape at:{1,0} color:#gray;
	}
}

species people skills:[escape_pedestrian] {
	
	float speed <- 1#m/1#s;
	point dest;
	
	bool rad <- false;
	
	reflex choose_dest when:dest=nil {
		dest <- any_location_in(any(corridor));
		do compute_virtual_path pedestrian_graph:pedestrian_network final_target:dest reduce_angular_distance:rad;
	}
	
	reflex go_nimp when:not(dest=nil) {
		do walk;
		if(final_target=nil){ dest<-nil; }
	}
	
	aspect default {
		draw circle(3) color:#red border:#black;
		if (current_target != nil) {draw triangle(1.5) at: current_target color:#red;}
		if (current_path != nil) {draw current_path.shape color: #red;}
		loop t over: targets {
			draw circle(1) color: #magenta at: point(t);
		}
	}
	
}

species corridor skills:[pedestrian_road]{
	
	rgb color <- rnd_color(255);

	
	aspect default {
		draw shape color: color;
		//draw free_space color:road_status=1?#red:#black;
		loop h over: exit_nodes.values accumulate list<point>(each){
			draw square(2.0) at: point(h) color: color;
		} 
		//draw exit_hub  color:rgb(int(self));
	}
}

experiment pedestrian {
	output {
		display "pedestrian_network" /*type: opengl*/ {
			species walking_area transparency:0.8;
			species obstacle;
			species corridor aspect:default;
			species people;
		}
	}
}