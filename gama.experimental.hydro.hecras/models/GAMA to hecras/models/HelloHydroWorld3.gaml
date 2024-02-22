/***
* Name: HelloHydroWorld
* Author: kevinchapuis
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model HelloHydroWorld

global {
	//definiton of the file to import
	file grid_data <- file("../includes/Hello DEM 200x100.MergedInputs.tif") ;
	
	float regex_val <- -9999.0;
	
	//computation of the environment size from the geotiff file
	geometry shape <- envelope(grid_data);
	
	list<mnt> water_body;	
	
	float max_value;
	float min_value;
	int x_max;
	int y_max;
	
	int nb_houses <- 50;
	
	init {
		max_value <- mnt max_of (each.grid_value);
		min_value <- (mnt where (each.grid_value > regex_val)) min_of (each.grid_value);
		x_max <- (mnt with_max_of (each.grid_x)).grid_x;
		y_max <- max(mnt collect (each.grid_y));
		
		/*
		 * 
		 *
		write x_max;
		write "Max value = "+max_value;
		write "Min value = "+min_value;
		* 
		*/
		
		ask mnt {
			if(grid_value = regex_val){
				color <- #black;
			} else if(grid_value = max_value){
				color <- #white;
			} else {
				land_use <- "water"; 
				int val <- int(255 * (grid_value - min_value) / (max_value - min_value));
				color <- rgb(val,val,255);
				water_body <+ self;
			}
		}
		
		do build_house;
		
		ask house {
			create people number:1 with:[location::any_location_in(self),my_house::self];
		}
	}
	
	action build_house {
		list<mnt> available <- mnt where (each.grid_x < x_max/4);
		loop times:nb_houses {
			mnt the_place <- any(available);
			create house with:[location::the_place.location,my_place::the_place,color::#gray]{
				the_place.land_use <- self;
			}
			available >- the_place;
		}
		
	}
	
	action build_village {
				
		ask 4 among (mnt where (each.grid_value = max_value and each.grid_x < x_max/2)) {
			bool stop <- false;
			rgb neighbor_color <- rnd_color(255);
			create house with:[location::self.location,my_place::self, color::neighbor_color] returns:the_houses;
			self.land_use <- the_houses[0];
			mnt current_mnt <- self;
			loop while:not(stop) {
				
				list nghbs <- current_mnt neighbors_at 2 where (not(each.land_use is house)
					and each.grid_value = max_value and each.grid_x < x_max/2);
				if(empty(nghbs)){stop <- true; break;}
				
				current_mnt <- any(nghbs);
				
				create house with:[location::current_mnt.location,my_place::current_mnt, color::neighbor_color] returns:h;
				current_mnt.land_use <- h[0];
				
				if flip(0.05) {stop <- true;}
			}
		}
		
	}
	
	
	int hh <- 0;
	int mm <- 0;

	reflex call_sim_hecras_and_update_data {
	//				write load_hecras();
	//				file f <- file("../HWC/HWC2.prj");
	//				write Project_Open(f);
	//				write Compute_HideComputationWindow();
	//				write Compute_CurrentPlan();
	//				//		write Update_Data(550);
	//				write Project_Close();
	//				write QuitRas();
		mm<-mm+1;
		if(mm>59){
			mm<-0;
			hh<-hh+1;
		}
		if(hh>12){
			do pause;
		}
		string hrs <- hh < 10 ? "0" + hh : "" + hh;
		string min <- mm < 10 ? "0" + mm : "" + mm;
		file grid_dataa <- grid_file("../HWC/Plan 04/Depth (25JUL2019 " + hrs + " " + min + " 00).MergedInputs.tif");
		create aa from: grid_dataa;
		ask aa {
			float v <- float(self["grid_value"]);
			if (mnt[(int(self) mod 1600)] != nil) {
				ask mnt[(int(self) mod 1600)] {
					water_depth <- v;
				}

			}
			//			else{write (int(self) mod 1600);}
			do die;
		}
		
		
		 
//		max_value <- mnt max_of (each.grid_value);
//		min_value <- (mnt where (each.grid_value > regex_val)) min_of (each.grid_value);
//		x_max <- (mnt with_max_of (each.grid_x)).grid_x;
//		y_max <- max(mnt collect (each.grid_y));
		//		ask mnt{
		//			do _init_;
		//		}
		//		matrix data <- matrix(grid_data2.contents collect each.grid_value);
		//		write data;
		//		file updated_data <- csv_file("../includes/Depth.csv",",");
		//		matrix data <- matrix(updated_data);  
		//		loop i from: 0 to: data.rows -1{ 
		//			loop j from: 0 to: data.columns -1{
		//				ask mnt at {i,j}{
		//					grid_value<-float(data[j,i]);
		//				} 
		//			}	
		//		}	
	}
	
	
}

//definition of the grid from the geotiff file: the width and height of the grid are directly read from the asc file. The values of the asc file are stored in the grid_value attribute of the cells.
grid mnt file: grid_data neighbors:4 {
	rgb color;
	agent land_use;
	float water_depth;
	aspect water{
		draw shape color:rgb(255-(water_depth*30), 255-(water_depth*30), water_depth*30);
	}
}

species aa {
}
species water {
	
}

species house {
	mnt my_place;
	rgb color;
	aspect ThreeDhouse {
		draw cube(my_place.shape.width) color:color;
	}
}

species people skills:[moving]{
	house my_house;
	mnt the_site;
	float speed <- 1#m/#s;
	
	path tptf;
	
	bool working;
	
	init {
		tptf <- path_between(topology(mnt), my_house.my_place, water_body closest_to self);
		if (tptf=nil or empty(tptf)) { write sample(self); }
		geometry p <- line(tptf.segments);
		the_site <- any(mnt where (each overlaps p and each.land_use=nil));
	}
	
	reflex go_build_a_dyke when:the_site!=nil and not(working){
		
		do goto target:the_site on:mnt where (each.land_use!="water" and not(each.land_use is house));
		
		if location overlaps the_site {
			working <- true;
			location <- any_location_in(the_site);
		}
	}
	
	reflex build_dyke when:working {
		 if(the_site.land_use=nil){the_site.land_use <- "dyke";}
		 the_site.grid_value <- the_site.grid_value + 0.1;
		 the_site.color <- rgb(the_site.grid_value,the_site.grid_value/2,0);
	}
	
	aspect default {
		draw circle(1) color:#black;
	}
}

experiment xp type:gui {
	output {
		display hellowrold {
			grid mnt;
			species house aspect:ThreeDhouse;
			species people;
		}
		display hellowrold2 {
			species mnt aspect:water;
			species house aspect: ThreeDhouse;
			species people;
		}
	}
}