/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	nc_file netcdf_sample <- nc_file("../includes/tos_O1_2001-2002.nc");
	int times <- 0;
	int grid_num <- 0;
	int gridsSize <- 0;
	int timesAxisSize <- 0;

	init {
		write openDataSet(netcdf_sample);
		gridsSize <- getGridsSize(netcdf_sample);
		timesAxisSize <- netcdf_sample getTimeAxisSize grid_num;
	}

	reflex s {
		matrix<int> m <- (matrix<int>(netcdf_sample readDataSlice (grid_num, times, 0, -1, -1)));
		//				write ""+m.columns+" "+m.rows;
		ask cell {
			grid_value <- float(m at {grid_x, grid_y});
			color <- rgb(grid_value);
		}

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

grid cell width: 180 height: 170 {
}

experiment mike type: gui {
	output {
		display "s" {
			grid cell;
		}

	}

}