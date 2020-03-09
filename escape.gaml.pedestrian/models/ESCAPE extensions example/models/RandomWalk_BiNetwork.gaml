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
	
	bool mixed_corridors <- true parameter:true;
	bool reduced_angular_distance <- true parameter:true;
	
	graph pedestrian_network;
	
	init {
		
		create walking_area from:pedestrian_area;
		create obstacle from:obstacles;
		create road from:regular_road;
		create people number:500 with:[location::any_location_in(any(obstacle)),rad::reduced_angular_distance];
		
		if(mixed_corridors){
			// With mixed corridor
			pedestrian_corridors <- generate_pedestrian_network([obstacle],walking_area,road,false,false,1.0,0.1,false,0.1,0.0,0.0);
		} else {
			// Only 2D / continuous space corridor
			pedestrian_corridors <- generate_pedestrian_network([obstacle],walking_area,false,false,10.0,0.1,true,0.1,0.0,0.0);
		}
				
		write "Continuous space corridors : "+pedestrian_corridors count (each["road_status"]=1);
		write "Simple 1D corridors : "+pedestrian_corridors count (each["road_status"]=0);
		//save pedestrian_corridors type:"shp" to:pedestrian_file_roads;
		
		create corridor from: pedestrian_corridors { do initialize distance:10#m obstacles:[obstacle]; }
		
		pedestrian_network <- as_edge_graph(corridor);
		
		if(reduced_angular_distance) {
			ask corridor {
				do build_exit_hub pedestrian_graph:pedestrian_network distance_between_targets: 3.0;
				do update_exit_hub;
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
		draw circle(1) color:#gray border:#black;
		if current_target != nil {draw line(location,current_target) color:rgb(int(self));}
	}
	
}

species corridor skills:[pedestrian_road]{
	
	geometry exit_hub <- shape;
	
	action update_exit_hub {
		 
		/* 
		loop e over:exit_nodes.keys {
			point e1 <- exit_nodes.keys first_with (each != e);
			loop e2 over:exit_nodes[e] {
				exit_hub <- exit_hub + line([e1,e2]);
			}
		} 
		* 
		*/
		
		list<geometry> geoms <- exit_nodes accumulate (each);
		
		exit_hub <- exit_hub + union(geoms collect (each+0.5));
	}
	
	aspect default {
		if road_status=2 {draw shape color:#black;}
	}
}

experiment pedestrian {
	output {
		display "pedestrian_network" {
			species walking_area transparency:0.8;
			species obstacle;
			species corridor;
			species people;
		}
	}
}