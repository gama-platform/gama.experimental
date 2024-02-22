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
	
	float max_value;
	float min_value;
	int x_max;
	int y_max;

	init {
		create hydroManager;
		
		max_value <- mnt max_of (each.grid_value);
		min_value <- (mnt where (each.grid_value > regex_val)) min_of (each.grid_value);
		x_max <- mnt max_of (each.grid_x);
		y_max <- mnt max_of (each.grid_y);

		ask mnt {
			do update_mnt_cells;
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
						rgb(255-(water_depth*30), 255-(water_depth*30), 255);
	}
}

experiment xp type: gui {
	output {
		display hellowrold {
			grid mnt;
		}
		display hellowrold2 {
			species mnt aspect:water;
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