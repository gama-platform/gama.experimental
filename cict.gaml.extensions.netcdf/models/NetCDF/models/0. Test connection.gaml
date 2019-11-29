/**
* Name: Testconnection
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	nc_file netcdf_sample <- nc_file("../includes/tos_O1_2001-2002.nc");
	geometry shape <- envelope(rectangle(180, 170));
	int times <- 0;

	reflex s {
		matrix<int> m <- (matrix<int>(netcdf_sample readDataSlice (0, times, 0, -1, -1)));
		ask cell {
			grid_value <- float(m at {grid_x, grid_y});
		}

		times <- times + 1;
		if (times > 23) {
			times <- 0;
		}

	}

}

grid cell width: 180 height: 170 {
	rgb color update: rgb(grid_value);
}

experiment mike type: gui {
	output {
		display "s"  {
			grid cell; 
		}

	}

}
