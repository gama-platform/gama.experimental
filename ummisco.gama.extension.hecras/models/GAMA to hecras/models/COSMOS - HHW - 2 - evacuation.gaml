/***
* Name: HelloHydroWorld
* Author: kevinchapuis, benoit gaudou, Huynh Quang Nghi
* Description: 
* Tags: Tag1, Tag2, TagN
***/
model HelloHydroWorld

global {
	//definiton of the file to import
	file project_file  <- file("../HWC/HWC2.prj");
	file grid_data <- file("../includes/Hello DEM 200x100.MergedInputs.tif");
	float regex_val <- -9999.0;
	
	//computation of the environment size from the geotiff file
	geometry shape <- envelope(grid_data);
	date  starting_date <- date("2019-07-25 00:01:00"); 
	float step <- 1#mn;	
	
	float cell_size;
	
	int nb_houses <- 50;
	float THRESHOLD_FLOODED <- 2.0#m; //1.0 #m;
	
	float max_value;
	float min_value;
	int x_max;
	int y_max;
	
	// Indicators
	int evacuees <- 0;
	int casualties <- 0;
	
	// Parameters
	int evac_minutes <- 60;
	date alert_time <- date("06 00 00","HH mm ss") add_minutes evac_minutes;

	init {
		cell_size <- any(mnt).shape.width;
		
		create hydroManager;
		
		max_value <- mnt max_of (each.grid_value);
		min_value <- (mnt where (each.grid_value > regex_val)) min_of (each.grid_value);
		x_max <- mnt max_of (each.grid_x);
		y_max <- mnt max_of (each.grid_y);

		ask mnt {
			do update_mnt_cells;
		}
		
		create evacuation_point {
			location <- (any(mnt where(each.grid_x = 0))).location;
		}

		do build_houses;
		
		ask house {
			create people with:[location::self.location,target::one_of(evacuation_point)];
		}		
		
	}
	
	action build_houses {
		list<mnt> available <- mnt where ((each.grid_x > x_max/3) and (each.grid_x < 2 * x_max/3) and (each.grid_value > 9.5) and (each.grid_y>0) and (each.grid_y<y_max));
		loop times:nb_houses {
			mnt the_place <- any(available);
			create house with:[location::the_place.location];
			remove the_place from: available ;
		}
	}	
	
	list<mnt> available_move <- [] update: mnt where(empty(people  inside each));

	reflex alert when: (current_date.hour = alert_time.hour) and (current_date.minute = alert_time.minute) {
		write "Time to evacuate " + current_date;
		ask people {
			alerted <- true;
		}
	}
}

species hydroManager /*skills: [hecrasSkill]*/ {
	init{ 
//		do load_hecras();
//		do Project_Open(project_file);
//		do Compute_CurrentPlan();
//		do QuitRas();	
	}	
	
	reflex update_data when: (current_date.day < 26){

		string hrs <- current_date.hour < 10 ? "0" + current_date.hour : "" + current_date.hour;
		// TODO : BEN : to  remove when bug  fixed: no 59  in  the rasmap  ! 
		string min <- (current_date.minute=59)? 58:( current_date.minute < 10 ? "0" + current_date.minute : "" + current_date.minute);

		file grid_dataa <- grid_file("../HWC/Plan 03/Depth (25JUL2019 " + hrs + " " + min + " 00).MergedInputs.tif");
		
		list<float> water_depth_list <- grid_dataa accumulate(float(each["grid_value"])); 
		
		loop i from: 0 to: length(water_depth_list) -1 {
			ask mnt[i] {
				water_depth <- water_depth_list[i];
			}			
		}
	}	
	
	reflex stop_simu when: (current_date.day = 26)  {
		ask world { do pause; }
	}
}

// Definition of the grid from the geotiff file: the width and height of the grid are directly read from the asc file. 
// The values of the asc file are stored in the grid_value attribute of the cells.
grid mnt file: grid_data neighbors: 4 {
	rgb color;
	float water_depth;
	
	reflex update {
		do update_mnt_cells;
	}
	
	action update_mnt_cells {
		if (grid_value = regex_val) {
			color <- #white;
		} else if (grid_value = max_value) {
			color <- #black;		
		}else {
			int val <- int(255 * (grid_value - min_value) / (max_value - min_value));
			color <- rgb(val, val, 255);
		}
	}
	
	aspect water{
		draw shape color: (water_depth<0) ? 
						#white : 
						rgb(255-(water_depth*30), 255-(water_depth*30), water_depth*30);
	}
}

species house {
	aspect default {
		draw square(cell_size) color:#grey;
	}	
}

species people skills: [moving] schedules: shuffle(people) {
	evacuation_point target ; 
	bool alerted <- false;
	
	reflex evacuate when: alerted {
		do goto target: target on: available_move speed: 5#m/#mn;
	}
	
	reflex evacuated when: location = target.location {
		evacuees <- evacuees + 1;
		do die;
	}
	
	reflex flooded when: first(mnt overlapping self).water_depth >= THRESHOLD_FLOODED {
		casualties <- casualties + 1;
		do die;
	}	
	
	aspect default {
		draw circle(1) color:#black;
	}	
}

species evacuation_point {
	aspect default {
		draw circle(3) color: #green;
	}
}

experiment xp type: gui {
	output {
		display hellowrold {
			grid mnt lines: #white;
			species evacuation_point;
			species house;
			species people;			
		}
		display hellowrold2 {
			species mnt aspect:water;
			species evacuation_point;
			species house;
			species people;				
		}

		display evac  {
			chart "evacuees" x_serie_labels: ""+current_date.hour+":"+current_date.minute {
				data "#evacuees"  value: evacuees color: #blue;
				data  "#casualties" value: casualties color: #red;
			}
		}

		display  c  {
			chart "water" x_serie_labels: ""+current_date.hour+":"+current_date.minute {
				data "water elevation mean"  value: (mnt where(each.water_depth > regex_val)) mean_of(each.water_depth) color: #blue;
				data "water elevation max"  value: (mnt where(each.water_depth > regex_val)) max_of(each.water_depth) color: #red;
				data "water elevation min"  value: (mnt where(each.water_depth > regex_val)) min_of(each.water_depth) color: #green;		
			}
		}
	}
}

experiment evalAlertTime type: batch repeat: 1 until: (length(people) = 0) {
//	parameter "Alert time" var: alert_time <- date("06 00 00","HH mm ss") min: date("06 00 00","HH mm ss") max: date("06 30 00","HH mm ss") step: 5#mn;
	parameter  "Alert minutes" var: evac_minutes min: 20 max: 60  step: 5;
	
	permanent {
		display evac  {
			chart "evacuees" x_serie_labels: ""+alert_time.hour+":"+alert_time.minute x_label:  "Alert time"{
				data "#evacuees"  value: simulations mean_of(each.evacuees) y_err_values: [simulations min_of(each.evacuees),simulations max_of(each.evacuees)] color: #blue;
				data "#casualties" value: simulations mean_of(each.casualties) y_err_values: [simulations min_of(each.casualties),simulations max_of(each.casualties)]color: #red;
			}
		}		
	}
}