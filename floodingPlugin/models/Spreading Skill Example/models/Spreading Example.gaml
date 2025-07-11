model SpreadingSkillExample

global {
// Load DEM and water data (same as your original model)
	file dem_file <- file("../includes/SRTM_DEM_NHATLE_30M_RESIZED_BY_FOUR.tif");
	// file dem_file <- file("../includes/terrain_large.tif");
	// file dem_file <- file("../includes/hanoi.tif");
	field elevation_map <- field(dem_file);
	geometry shape <- envelope(dem_file);
	file water_shapefile <- file("../includes/water_multipolygon.shp");
	// file water_shapefile <- file("../includes/river_clean.shp");
	// file water_shapefile <- file("../includes/water_polygon_hanoi.shp");
	list<geometry> water_geometries <- [];

	// Global water field for display (updated by simulation manager)
	field display_water_field;

	init {
	// Load water geometries
		water_geometries <- water_shapefile.contents;
		write "Loaded " + length(water_geometries) + " water geometries";

		// Initialize display water field with same dimensions as DEM
		display_water_field <- field(elevation_map.columns, elevation_map.rows);

		// Create simulation manager agent
		create simulation_manager number: 1;
		write "SpreadingSkill with Rain example initialized";
		write "Use simulation manager actions to control the simulation and rain";
	}

}

species simulation_manager skills: [spreading] {

	init {
		write "Simulation manager created with SpreadingSkill and Rain system";

		// Initialize the spreading grid with your data and parameters
		do initialize_spreading_grid(elevation_map, // DEM field
		water_geometries, // Water polygons
		1.5 #m, // Initial water depth (1.5m)
		0.01 #m, // Flow threshold
		0.3 #m, // Rising rate 
		0.001 #m, // Min flow difference
		0.1 #m // Equalization threshold
);

		// Update global display field
		if (water_field != nil) {
			display_water_field <- water_field;
		}

		write "Spreading grid initialized successfully";
		write "Active water cells: " + get_active_water_count();
		write "Edge cells: " + get_edge_cell_count();
	}

	// Automatic simulation step when active
	reflex simulation_step when: is_simulation_active() {
		do simulate_spreading_step();

		// Update global display field for visualization
		if (water_field != nil) {
			display_water_field <- water_field;
		}

	}

	// Manual control actions for simulation
	action start_simulation {
		do start_spreading_simulation();
		write "Simulation started at step " + get_current_step();
	}

	action stop_simulation {
		do stop_spreading_simulation();
		write "Simulation stopped at step " + get_current_step();
	}

	action reset_simulation {
		do reset_spreading_simulation(water_geometries, 1.5);

		// Update global display field
		if (water_field != nil) {
			display_water_field <- water_field;
		}

		write "Simulation reset. Active cells: " + get_active_water_count();
	}

	// NEW: Rain control actions
	action start_light_rain {
		do start_rain(0.1, 1.0); // 0.1m/step rain rate, normal intensity
		write "Light rain started (0.1m/step)";
	}

	action start_heavy_rain {
		do start_rain(0.3, 1.5); // 0.3m/step rain rate, 1.5x intensity
		write "Heavy rain started (0.3m/step, 1.5x intensity)";
	}

	action start_custom_rain (float rate, float intensity) {
		do start_rain(rate, intensity);
		write "Custom rain started (rate: " + rate + "m/step, intensity: " + intensity + "x)";
	}

	action increase_rain {
		if (is_rain_active()) {
			float current_rate <- get_rain_rate();
			do set_rain_rate(current_rate + 0.05);
			write "Rain increased to " + get_rain_rate() + "m/step";
		} else {
			write "No rain active to increase";
		}

	}

	action decrease_rain {
		if (is_rain_active()) {
			float current_rate <- get_rain_rate();
			float new_rate <- max(0.0, current_rate - 0.05);
			if (new_rate <= 0.0) {
				do stop_rain();
				write "Rain stopped (rate reached 0)";
			} else {
				do set_rain_rate(new_rate);
				write "Rain decreased to " + get_rain_rate() + "m/step";
			}

		} else {
			write "No rain active to decrease";
		}

	}

	// Status reporting
	action report_status {
		write "=== SIMULATION STATUS ===";
		write "Active: " + is_simulation_active();
		write "Step: " + get_current_step();
		write "Active water cells: " + get_active_water_count();
		write "Edge cells: " + get_edge_cell_count();
		write "Flow threshold: " + flow_threshold;
		write "Rising rate: " + rising_rate;
		write "=== RAIN STATUS ===";
		write "Rain active: " + is_rain_active();
		write "Rain rate: " + get_rain_rate() + "m/step";
		write "Rain intensity: " + get_rain_intensity() + "x";
		write "========================";
	}

}

experiment spreading_with_rain_experiment type: gui {
	output {
		display "Water Spreading Simulation with Rain" type: opengl {
		// Display terrain elevation
			mesh elevation_map scale: 20 triangulation: true grayscale: true;

			// Display water field from the skill
			mesh display_water_field scale: 20 triangulation: true color: rgb(0, 100, 255, 150);

			// Display simulation manager (optional)
			species simulation_manager;
			event #mouse_move {
				write display_water_field[#user_location];
			}

		}

		display "Simulation Controls" type: java2D {
			chart "Water Spread Progress" type: series {
				data "Active Water Cells" value: first(simulation_manager).get_active_water_count() color: #blue;
				data "Edge Cells" value: first(simulation_manager).get_edge_cell_count() color: #red;
				data "Simulation Step" value: first(simulation_manager).get_current_step() color: #green;
				data "Rain Rate x100" value: first(simulation_manager).get_rain_rate() * 100 color: #purple;
			}

		}

		// Basic monitors
		monitor "Simulation Active" value: first(simulation_manager).is_simulation_active();
		monitor "Current Step" value: first(simulation_manager).get_current_step();
		monitor "Active Water Cells" value: first(simulation_manager).get_active_water_count();
		monitor "Edge Cells" value: first(simulation_manager).get_edge_cell_count();

		// Rain monitors
		monitor "Rain Active" value: first(simulation_manager).is_rain_active() color: first(simulation_manager).is_rain_active() ? #red : #green;
		monitor "Rain Rate (m/step)" value: first(simulation_manager).get_rain_rate();
		monitor "Rain Intensity" value: first(simulation_manager).get_rain_intensity();
	}

	// Simulation control actions
	action ask_start_simulation {
		ask simulation_manager {
			do start_simulation();
		}

	}

	action ask_stop_simulation {
		ask simulation_manager {
			do stop_simulation();
		}

	}

	action ask_reset_simulation {
		ask simulation_manager {
			do reset_simulation();
		}

	}

	action ask_report_status {
		ask simulation_manager {
			do report_status();
		}

	}

	// NEW: Rain control actions
	action ask_start_light_rain {
		ask simulation_manager {
			do start_light_rain();
		}

	}

	action ask_start_heavy_rain {
		ask simulation_manager {
			do start_heavy_rain();
		}

	}

	action ask_stop_rain {
		ask simulation_manager {
			do stop_rain();
		}

	}

	action ask_increase_rain {
		ask simulation_manager {
			do increase_rain();
		}

	}

	action ask_decrease_rain {
		ask simulation_manager {
			do decrease_rain();
		}

	}

	// User commands for manual control
	user_command "Start Simulation" action: ask_start_simulation;
	user_command "Stop Simulation" action: ask_stop_simulation;
	user_command "Reset Simulation" action: ask_reset_simulation;
	user_command "Report Status" action: ask_report_status;

	// NEW: Rain user commands
	user_command "Start Light Rain" action: ask_start_light_rain;
	user_command "Start Heavy Rain" action: ask_start_heavy_rain;
	user_command "Stop Rain" action: ask_stop_rain;
	user_command "Increase Rain" action: ask_increase_rain;
	user_command "Decrease Rain" action: ask_decrease_rain;
}

