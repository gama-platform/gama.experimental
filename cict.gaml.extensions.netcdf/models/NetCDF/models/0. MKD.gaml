/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	file netcdf_sample <- file("../includes/ENS_mm_rcp85.2015_2050_MKD_tas.nc");
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
//						write ""+m.columns+" "+m.rows;
		ask cell {
			grid_value <- float(m at {grid_x, 12-grid_y-1});
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


//definition of the grid from the asc file: the width and height of the grid are directly read from the asc file. The values of the asc file are stored in the grid_value attribute of the cells.
grid cell 
//width: 16 height: 12
file: netcdf_sample
{
//	init {
//		color<- grid_value = 0.0 ? #black  : (grid_value = 1.0  ? #green :   #yellow);
//	}
}
//grid cell width: 16 height: 12 {
//}

experiment sim type: gui {
	output {
		display "s" {
			grid cell;
		}

	}

}
