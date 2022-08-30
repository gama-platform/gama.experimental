/**
* Name: Water Management Bac Hung Hai
* Author:  Arnaud Grignard, Tri Nguyen-Huu, Benoit Gaudou
* Description: Wter Management Bac Hung Hai - MIT CityScope - IRD UMMISCO - WARM
* Tags: grid, load_file, asc
*/

model watermanagement

global {

	file gates_shape_file <- shape_file("../includes/BachHungHaiData/gates.shp");
	file rivers_shape_file <- shape_file("../includes/BachHungHaiData/rivers.shp");
	file main_rivers_shape_file <- shape_file("../includes/BachHungHaiData/main_rivers_simple.shp");
	file river_flows_shape_file <- shape_file("../includes/BachHungHaiData/river_flows.shp");
	file landuse_shape_file <- shape_file("../includes/BachHungHaiData/VNM_adm4.shp");
	
	graph the_river;
	geometry shape <- envelope(main_rivers_shape_file);	
	
	list<string> cells_types <- ["Aquaculture", "Rice","Vegetables", "Industrial", "Null"];
	
	map<string, rgb> cells_colors <- [cells_types[0]::#orange, cells_types[1]::#darkgreen,cells_types[2]::#lightgreen, cells_types[3]::#red, cells_types[4]::#black];
	map<string, float> cells_withdrawal <- [cells_types[0]::0.5, cells_types[1]::3.0,cells_types[2]::0.25, cells_types[3]::4.0];
	map<string, int> cells_pollution <- [cells_types[0]::55, cells_types[1]::0,cells_types[2]::20, cells_types[3]::90];
	map<string,matrix<int>> lego_code <-["Aquaculture"::matrix([[1,1],[1,0]]),"Rice"::matrix([[1,0],[0,0]]),"Vegetables"::matrix([[1,0],[0,1]]),"Industrial"::matrix([[1,0],[1,0]])];

	bool showGrid parameter: 'Show grid' category: "Parameters" <-true;
	bool showWater parameter: 'Show Water' category: "Parameters" <-true;
	bool showLanduse parameter: 'Show LandUse' category: "Parameters" <-true; 
	bool showDryness parameter: 'Show Dryness' category: "Parameters" <-false; 
	
	bool showLegend parameter: 'Show Legend' category: "Legend" <-true;
    bool showOutput parameter: 'Show Output' category: "Legend" <-true;
	
	bool keystoning parameter: 'Show keystone grid' category: "Keystone" <-false;
	bool table_interaction <- true;
	
	list<gate> source;
	list<gate> dest;
	
	map<river,float> probaEdges;
	
	float evaporationAvgTime parameter: 'Evaporation time' category: "Parameters" step: 10.0 min: 2.0 max:10000.0 <- 2500.0 ;
	float StaticPollutionEvaporationAvgTime parameter: 'Pollution Evaporation time' category: "Parameters" step: 10.0 min: 2.0 max:10000.0 <- 500.0 ;
	int grid_height <- 8;
	int grid_width <- 8;
	
	// dryness parameters
	int dryness_removal_amount parameter: 'Water Evaporation time' category: "Parameters" step: 10 min: 10 max:1000 <- 100 ; 
	
	// hdtrung ------------------------------------------------------------------------------------------------------------------
	int cameraID <- 2;
	list<point> points_for_distorsion_removing;
	image_file first_image_file <- image_file("../includes/t_capturetest.png");
	image_file remove_distord_image_file <- image_file("../includes/t_real_map_distorsion_removed.jpg");
	float x_coeff ;
	float y_coeff ;
	geometry remove_distorsion_world_geom;
	list<list<int>> legoList;
	int current_cols <- 8 min: 0 max: 100 parameter: true on_change: update_cols;
	int current_rows <- 8 min: 0 max: 100 parameter: true on_change: update_rows;
	
	string image_distorsion_removed_name <- "t_real_map_distorsion_removed.jpg";
	string path_to_first_img <- first_image_file.path;
	bool map_def <- false;
	
	point first_pt <- nil;
	point last_pt <- nil;
	float point_size <- 0.5;
	rgb color_pt <- #cyan;
	point mouse_loc <- nil;
	
	action update_cols {
		current_cols <- current_cols;
		write("Current cols: " + current_cols);
		ask experiment {
			do update_outputs(true);
		}
	}
	
	action update_rows {
		current_rows <- current_rows;
		write("Current rows: " + current_rows);
		ask experiment {
			do update_outputs(true);
		}
	}
	
	// hdtrung ------------------------------------------------------------------------------------------------------------------
	
	init{
		string first_img <- capture_image("../includes/t_capturetest.png", cameraID);
//		cityIOUrl <- launchpad ? "https://cityio.media.mit.edu/api/table/launchpad": "https://cityio.media.mit.edu/api/table/urbam";
		create main_river from:main_rivers_shape_file{
			shape<-(simplification(shape,100));
		}
		create river from: rivers_shape_file;
		create gate from: gates_shape_file with: [type:: string(read('Type'))];
		create landuse from: landuse_shape_file with:[type::string(get("SIMPLE"))]{
			shape<-(simplification(shape,100));
		}
		create eye_candy from:river_flows_shape_file with: [type:: int(read('TYPE'))];
		
		ask cell {
			do init_cell;
		}
		
		ask river {
			overlapping_cell <- first(cell overlapping self);
		}
		
		ask landuse {
			if !empty(cell overlapping self) {
				cell c <- (cell overlapping self) with_max_of(inter(each.shape,self.shape).area);
				c.landuse_on_cell <+ self;
			}
		}
		
		source <- gate where (each.type = "source");
		dest <- gate where (each.type = "sink");
		
		ask gate {
			controledRivers <- river overlapping (0.4#km around self.location);
		}
		
		the_river <- as_edge_graph(river);
		probaEdges <- create_map(river as list,list_with(length(river),100.0));
	}
	
	reflex manage_water  {
		ask river {
			waterLevel <- 0;
		}
		ask water {
			river(self.current_edge).waterLevel <- river(self.current_edge).waterLevel+1;
		}
		ask polluted_water {
			river(self.current_edge).waterLevel <- river(self.current_edge).waterLevel+1;
		}
		probaEdges <- create_map(river as list, river collect(100/(1+each.waterLevel)));
		ask river where each.is_closed{
			put 0.001 at: self in: probaEdges;
		}
		ask source where(!each.is_closed){
			create water {
				location <- myself.location;
				color<-#blue;
			}
		}
		ask dest {
			do take_water;
		}
	}
	
	
	reflex water_consumption_and_pollution{
		ask water where(each.current_edge != nil) {
			cell c <- river(self.current_edge).overlapping_cell;
			if flip(cells_withdrawal[ c.type] * 0.01){
				ask c.landuse_on_cell {
					 self.dryness <- max(self.dryness - dryness_removal_amount,0);	
				}
				if(flip(cells_pollution[ c.type] * 0.01)) {
					create polluted_water {
						location <- myself.location;
						heading <- myself.heading;
						type<-c.type;
					}		
				}	
			do die;
			}
		}	
		
		ask polluted_water where(each.current_edge != nil) {
			if flip(cells_withdrawal[ river(self.current_edge).overlapping_cell.type] * 0.01){
				create static_pollution number: 8{
					dissolution_expectancy<-StaticPollutionEvaporationAvgTime * (0.8 + rnd(0.4));
					color <- myself.color;
					location <- any_location_in(3#km around(myself.location));
				}
				if(flip(cells_pollution[ river(self.current_edge).overlapping_cell.type] * 0.01)) {
					create polluted_water {
						location <- myself.location;
						heading <- myself.heading;
						color <- cells_colors[river(myself.current_edge).overlapping_cell.type] ;
					}		
				}	
			do die;
			}
		}	
	}
	
	// if the user clicks on a gate, it will close or open it. If the user clicks on a land plot, it will change the land use. If when clicking when the mouse is over
	// a gate and a land plot, it will only perform the action on the gate.
	action mouse_click {
		gate selected_station <- first(gate overlapping (circle(1) at_location #user_location));
		if selected_station != nil{
			selected_station.is_closed <- !selected_station.is_closed;
			ask selected_station.controledRivers {
				self.is_closed <- !(self.is_closed);
			}
		} else {
			cell selected_cell <- first(cell overlapping (circle(1) at_location #user_location));
			if selected_cell != nil{
				int old_type <- index_of(cells_types, selected_cell.type);
				selected_cell.type <- cells_types[mod(index_of(cells_types, selected_cell.type)+1,length(cells_types))];
			}
			ask selected_cell.landuse_on_cell{
			  self.color<-cells_colors[selected_cell.type];
			}
		}
		 
	}
	
	// hdtrung ----------------------------------------------------------------------------------------------------------------------------------------------
	action map_corners {
		map_def <- not map_def;
		points_for_distorsion_removing <- [];
	}
	
	action mouse_down_action {
		if map_def{
			points_for_distorsion_removing << #user_location;
			if length(points_for_distorsion_removing) = 4 {
				write(points_for_distorsion_removing);
				string new_corners <- map_define(points_for_distorsion_removing);
				string new_image_path <- remove_perspective(first_image_file.path replace (first_image_file.name,image_distorsion_removed_name),first_image_file.path, 1500,1500);
//				string new_image_path <- remove_perspective(remove_distord_image_file.path,first_image_file.path, 1500,1500);
				write(new_image_path);
					if new_image_path != nil {
						matrix mat <- matrix(image_file(new_image_path));
						first_image_file <-image_file(copy(new_image_path));
						x_coeff <- world.shape.width / mat.columns;
						y_coeff <- world.shape.height / mat.rows;
//						points_for_distorsion_removing <- [];
						remove_distorsion_world_geom <- nil;
					}
				
				legoList <- code_detect(remove_distord_image_file.path, current_cols, current_rows);
				map_def <- false;

				loop i over: legoList{
					write(i);
				}
			}
		}
	}
	// hdtrung ----------------------------------------------------------------------------------------------------------------------------------------------
	reflex user_interact when: table_interaction and every(500#cycle){
		string capture_map_img <- capture_image("../includes/t2_capturetest.png", cameraID);
		string image_distorsion_removed_name_t2 <- capture_image("../includes/t2_real_map_distorsion_removed.jpg", cameraID);
		image_file capture_map_image_file <- image_file("../includes/t2_capturetest.png");
		image_file remove_distord_image_file_t2 <- image_file("../includes/t2_real_map_distorsion_removed.jpg");
		string new_corners <- map_define(points_for_distorsion_removing);
		string new_image_path <- remove_perspective(capture_map_image_file.path replace (capture_map_image_file.name,image_distorsion_removed_name_t2),capture_map_image_file.path, 1500,1500);
		if new_image_path != nil {
			matrix mat <- matrix(image_file(new_image_path));
			first_image_file <-image_file(copy(new_image_path));
			x_coeff <- world.shape.width / mat.columns;
			y_coeff <- world.shape.height / mat.rows;
			remove_distorsion_world_geom <- nil;			
		}		
		legoList <- code_detect(remove_distord_image_file_t2.path, current_cols, current_rows);
//		write("----------------------------------------------------------------------------------------------------------------");
//		loop i over: legoList{
//			write(i);
//		}
//		write("----------------------------------------------------------------------------------------------------------------");
//		write(" ");
//		write("start----------------------------------------------------------------------------------------------------------------");


//list<string> cells_types <- ["Aquaculture", "Rice","Vegetables", "Industrial", "Null"];
//	
//	map<string, rgb> cells_colors <- [cells_types[0]::#orange, cells_types[1]::#darkgreen,cells_types[2]::#lightgreen, cells_types[3]::#red, cells_types[4]::#black];
		
		loop i from: 0 to: 7 {
			loop j from: 0 to: 7{
				switch legoList[i][j]{
					match_one [10, 11]{ // Rice
						cell[i,j].type <- cells_types[1];
						if (legoList[i][j] = 10){
							ask gate overlapping cell[i,j]{
								is_closed<-false;
							}
						}
						if (legoList[i][j] = 11){
							ask gate overlapping cell[i,j]{
								is_closed<-true;
							}
						}
					}
					match_one [20, 21]{ // Vegetable
						cell[i,j].type <- cells_types[2];
						if (legoList[i][j] = 20){
							ask gate overlapping cell[i,j]{
								is_closed<-false;
							}
						}
						if (legoList[i][j] = 21){
							ask gate overlapping cell[i,j]{
								is_closed<-true;
							}
						}
					}	
					match_one [30, 31]{ // Industrial
						cell[i,j].type <- cells_types[3];
						if (legoList[i][j] = 30){
							ask gate overlapping cell[i,j]{
								is_closed<-false;
							}
						}
						if (legoList[i][j] = 31){
							ask gate overlapping cell[i,j]{
								is_closed<-true;
							}
						}
					}	
					match_one [40, 41]{ // Aquaculture
						cell[i,j].type <- cells_types[0];
						if (legoList[i][j] = 40){
							ask gate overlapping cell[i,j]{
								is_closed<-false;
							}
						}
						if (legoList[i][j] = 41){
							ask gate overlapping cell[i,j]{
								is_closed<-true;
							}
						}
					}
					default {
						cell[i,j].type <- cells_types[4];
					} 
				}
			}
        } 	
//		write("end----------------------------------------------------------------------------------------------------------------");
	}
		

}

grid cell width: 8 height: 8 {
	string type;
	rgb color;
	list<river> rivers_on_cell;
	list<landuse> landuse_on_cell <- [];
	
	init {
		type<-one_of (cells_types);
	}
	
	action init_cell {
		rivers_on_cell <- river overlapping self;
	}

	aspect base{
		if(showGrid){
			if(type="Water"){
				draw shape color:color border: #white;	
			}else{
			  	draw shape color:cells_colors[type];	
			}	
		}
		if keystoning {
				draw 100.0 around(shape * 0.75) color: #black;
		}
	}
}

species water skills: [moving] {
	rgb color <- #blue;
	int amount<-250;
	river edge;
	float tmp;

	reflex move {	
		if edge != nil{
			tmp <- probaEdges[edge];
			put 1.0 at: edge in: probaEdges;	
		}
		do wander on: the_river speed: 450.0 proba_edges: probaEdges;
		if edge != nil{
			put tmp at: edge in: probaEdges;	
		}
		edge <- river(current_edge);
	}
	
	reflex evaporate when: (flip(1/evaporationAvgTime)){
		do die;
	}
	
	aspect default {
		if(showWater){
		  draw square(0.25#km)  color: color;		
		}
	}
}

species polluted_water parent: water {
	rgb color <- #red;
	string type;
	
	aspect default {
		draw square(0.25#km)  color: cells_colors[type];	
	}
}

species static_pollution{
	rgb color;
	float dissolution_expectancy;
	
	reflex remove_pollution{
		dissolution_expectancy <- dissolution_expectancy - 10;
		if dissolution_expectancy < 0 {
			do die;
		}
		
	}
	
	aspect{
		draw square(0.2#km) color: color;
	}
}

species main_river{
	aspect base{
		draw shape color:#blue width:2;
	}
}

species river{
	int waterLevel <- 0;
	bool is_closed <- false;
	cell overlapping_cell;
	
	aspect base{
	  draw shape color: is_closed? #red:rgb(235-235*sqrt(min([waterLevel,8])/8),235-235*sqrt(min([waterLevel,8])/8),255) width:3;		
	}
}


species gate {
	rgb color <- rnd_color(255);
	string Name;
	string type; // amongst "source", "sink" or "null".
	geometry shape <- circle(0.75#km);	
	bool is_closed<-false;
	list<river> controledRivers <- [];

	action take_water {
		ask (agents of_generic_species water) overlapping self{do die;}
	}
	
	aspect default {
		if is_closed{
			draw circle(0.75#km)-circle(0.4#km) color:  #red  border: #black;
		}else{
			if self.type = "source" {
				draw circle(0.75#km) - circle(0.40#km) color:  rgb(0,162,232)  border: #black;
			}else if self.type = "sink" {
			//	draw circle(0.75#km) - circle(0.40#km) color:  #white;//  border: #black;
			}else{
				draw circle(0.75#km)-circle(0.4#km) color:  #green  border: #black;
			}
		}
	}
}


species landuse{
	string type;
	rgb color;
	int dryness <- 500;
	
	reflex dry when: (dryness < 1000) {
		dryness <- dryness + int(dryness_removal_amount/100);
	}
	
	aspect base{
	  if(showLanduse){
	  	
	  	if(showDryness){
	  		draw shape color:(dryness>500) ? #red :#green  border:#black;
	  	    //draw string(dryness) color:#white size:50;	
	  	}else{
	  		draw shape color:color border:#black;
	  	}
	  }	
	}
}


species eye_candy{
	int type;
	
	aspect base{
		if mod(cycle,3) = mod(type,3){
			draw shape color:#blue;
		}
		if mod(cycle-1,3) = mod(type,3){
			draw shape color:rgb(50,50,255);
		}
		if mod(cycle-2,3) = mod(type,3){
			draw shape color:rgb(100,100,255);
		}
	}
}

experiment dev type: gui {
	output {
		display "Bac" type: opengl draw_env:false background:#black synchronized:true refresh: every(1#cycle)
		{
			species landuse aspect:base transparency:0.65;
			species cell aspect:base transparency: 0.6;	
			species main_river aspect:base;			
			species river aspect:base transparency: 0.2;
			species polluted_water transparency: 0.2;
			species static_pollution transparency: 0.5;
			species water transparency: 0.2;
			
			species eye_candy aspect: base;
			
			species gate;
			
			event mouse_down action:mouse_click;
			event["g"] action: {showGrid<-!showGrid;};
			event["l"] action: {showLegend<-!showLegend;};
			event["w"] action: {showWater<-!showWater;};
			
			graphics 'background'{
				draw shape color:#white at:{location.x,location.y,-10};
			}
			
			overlay position: { 180#px, 250#px } size: { 0 #px, 0 #px } background:#white transparency: 0.0 border: #black rounded: true //position: { 180#px, 250#px } size: { 180 #px, 100 #px }
            {   
            	if(showLegend){            
					float x <- -70#px;
					float y <- -150#px;
		            draw "CityScope" at: { x, y } color: #white font: font("Helvetica", 32,#bold);
		            draw "\nHanoi" at: { x, y } color: #white font: font("Helvetica", 32,#bold);
	            	draw "\n\nWater Management" at: { x, y + 35#px } color: #white font: font("Helvetica", 17,#bold);
	            	
	            	y <- 190#px;
	            	draw "INTERACTION" at: { x,  y } color: #white font: font("Helvetica", 20,#bold);
	            	y<-y+25#px;
	            	draw "Landuse" at: { x,  y } color: #white font: font("Helvetica", 20,#bold);
	            	y<-y+25#px;
	            	
	                loop type over: cells_types where (each != "Null")
	                {
	                    draw square(20#px) at: { x + 10#px, y } color: #white;
						loop i from: 0 to: lego_code[type].rows - 1{
							loop j from: 0 to: lego_code[type].columns - 1{
								draw square(8#px) at: {x + (5+i*10)#px, y + (-5+j*10)#px} color: lego_code[type][i,j]=1?#black:#white;
							}
						}
	                    draw square(20#px) at: { x + 40#px, y } color: cells_colors[type] border: cells_colors[type]+1;
	                    draw string(type) at: { x + 60#px, y + 7#px } color: #white font: font("Helvetica", 20,#bold);
	                    y <- y + 25#px;
	                }
	                
	                y <- y + 40#px;
	                draw "Gate" at: { x + 0#px,  y+7#px } color: #white font: font("Helvetica", 20,#bold);
	            	y <- y + 25#px;
	                draw circle(10#px)-circle(5#px) at: { x + 20#px, y } color: #green border: #black;
	                draw 'Open' at: { x + 40#px, y + 7#px } color: #white font: font("Helvetica", 20,#bold);
	                y <- y + 25#px;
	                draw circle(10#px)-circle(5#px) at: { x + 20#px, y } color: #red border: #black;
	                draw 'Closed' at: { x + 40#px, y + 7#px } color: #white font: font("Helvetica", 20,#bold);
	                y <- y + 25#px;
	                draw circle(10#px)-circle(5#px) at: { x + 20#px, y } color: rgb(0,162,232) border: #black;
	                draw 'Source' at: { x + 40#px, y + 7#px } color: #white font: font("Helvetica", 20,#bold);
	                y <- y + 25#px;
	                draw "Turn lego to open" at: { x + 0#px,  y+4#px } color: #white font: font("Helvetica", 20,#bold);
	            	draw "\nand close" at: { x + 0#px,  y+4#px } color: #white font: font("Helvetica", 20,#bold);
	            
            	} 
            	if(showOutput){
            		float xOutput<-1550#px;
	            	
	            	float y <- 300#px;
	            	y<-y+75#px;
	                draw "OUTPUT" at: { xOutput+0#px,  y+4#px } color: #white font: font("Helvetica", 20,#bold);
	                y<-y+25#px;
	                draw "Pollutante" at: { xOutput+0#px,  y+4#px } color: #white font: font("Helvetica", 20,#bold);
	            	y<-y+25#px;
	                loop type over: cells_types
	                {
	                    draw circle(4#px) at: { xOutput+20#px, y } color: cells_colors[type] border: cells_colors[type]+1;
	                    draw string(type) + ": " +length(polluted_water where (each.type= type)) at: { xOutput+40#px, y + 4#px } color: #white font: font("Helvetica", 20,#bold);
	                    y <- y + 15#px;
	                }
	                 y <- y + 25#px;
               	 	draw string("Evaporation rate") at: { xOutput+0#px, y + 4#px } color: #white font: font("Helvetica", 20,#bold);
                	y <- y + 25#px;
                	draw rectangle(200#px,2#px) at: { xOutput+100#px, y } color: #white;
                	draw rectangle(2#px,10#px) at: { xOutput+(evaporationAvgTime/10000.0)*200#px, y } color: #white;
                	
                	y <- y + 50#px;
                	draw string("135 000ha - 2000km of canal") at: { xOutput+0#px, y + 4#px } color: #white font: font("Helvetica", 20,#bold);
            	}
            }
            

		}
		
		display image_view type: opengl axes: false{
			overlay position: { 5, 5 } size: { 480 #px, 100 #px } background: # black transparency: 0.3 border: #black rounded: true
            {
            	if  map_def{
            		draw "Choose 4 map corners" font: font(30) at: { 40#px,  50#px } color: #white;
            	}
          	}
          	graphics "image" {
          		draw image_file(path_to_first_img);
          	}
          	
			event "m" action: map_corners;
			
			event mouse_down action: mouse_down_action;
			
			graphics "remove deformation" {
				loop pt over: points_for_distorsion_removing {
					draw circle(point_size) at: pt color: color_pt;
				}
			}
			
			graphics "remove deformation geom" transparency: 0.5 {
				if remove_distorsion_world_geom != nil {
					draw  remove_distorsion_world_geom color: color_pt; 
				}
			}
			
			graphics "mouse loc points" {
				if map_def and mouse_loc != nil {
					draw circle(point_size) at: mouse_loc + {0,0,0.1} color: color_pt;
				}
			}
			
			graphics "pattern points" {
				if first_pt != nil {
					draw circle(point_size) at: first_pt  + {0,0,0.1} color: color_pt;
				}
				if last_pt != nil {
					draw circle(point_size) at: last_pt + {0,0,0.1} color: color_pt;
				}
			}
        }
	} 
}
