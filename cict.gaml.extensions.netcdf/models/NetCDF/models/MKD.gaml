/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global {
	file shp_file<-shape_file("../includes/commune_myxuyen.shp");
//	file netcdf_sample <- file("../includes/ENS_mm_rcp45.2015_2050_MKD_pr.nc");
//	file netcdf_sample <- file("../includes/ENS_mm_rcp45.2015_2050_MKD_tas.nc");
//	file netcdf_sample <- file("../includes/ENS_mm_rcp85.2015_2050_MKD_pr.nc");
	file netcdf_sample <- file("../includes/ENS_mm_rcp85.2015_2050_MKD_tas.nc");
//	file netcdf_sample <- file("../includes/tos_O1_2001-2002.nc");
	geometry shape<-to_GAMA_CRS(envelope(netcdf_sample),"4326");
	int times <- 1;
	int grid_num <- 0;
	int gridsSize <- 0;
	int timesAxisSize <- 0;

	init {
//		write openDataSet(netcdf_sample);
		gridsSize <- getGridsSize(netcdf_sample);
		timesAxisSize <- netcdf_sample getTimeAxisSize grid_num;
		create shp from:shp_file;
	}

	reflex s {
		matrix<int> m <- (matrix<int>(netcdf_sample readDataSlice (grid_num, times, 0, -1, -1)));
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
species shp{}
grid cell file: netcdf_sample {

	init {
		color <- rgb(grid_value);
	}

}

experiment sim type: gui {
	output {
		display "s" type:java2D synchronized:true{ 
			grid cell;
			species shp;
		}

	}

}
