/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	netcdf_file netcdf_sample <- netcdf_file("../includes/tos_O1_2001-2002.nc");
	int times <- 1;
	int grid_num <- 0;
	int gridsSize <- 0;
	int timesAxisSize <- 0;
	field field_from_matrix;
	geometry shape <- envelope(netcdf_sample);

	init {
		if(openDataSet(netcdf_sample)) {
			write "The dataset has been opened correctly.";						
			gridsSize <- getGridsSize(netcdf_sample);
			timesAxisSize <- netcdf_sample getTimeAxisSize grid_num;
			matrix<int> m <- (matrix<int>(netcdf_sample readDataSlice (grid_num, times, 0, -1, -1)));
			field_from_matrix <- field(m);
		} else {
			write "The dataset cannot be opened.";			
		}
	}

	reflex s {
		matrix<int> m <- (matrix<int>(netcdf_sample readDataSlice (grid_num, times, 0, -1, -1)));
		field_from_matrix <- field(m);

		//		ask cell {
		//			grid_value <- float(m at {grid_x, grid_y});
		//			color <- rgb(grid_value);
		//		}
		times <- times + 1;
		if (times > timesAxisSize - 1) {
			times <- 0;
		}

		grid_num <- grid_num + 1;
		if (grid_num > gridsSize - 1) {
			grid_num <- 0;
			timesAxisSize <- netcdf_sample getTimeAxisSize grid_num;
		}

	}

}

//grid cell file: netcdf_sample {
//
//	init {
//		color <- rgb(grid_value);
//	}
//
//}
experiment mike type: gui {
	list<rgb> palette <- brewer_colors(any(brewer_palettes(0)));
	output {
		display Field type: opengl {
		//			mesh field_from_matrix color: palette triangulation: true smooth: 4;
			mesh field_from_matrix color: palette scale: 0.1 triangulation: true smooth: 4;
		}

	}

}
