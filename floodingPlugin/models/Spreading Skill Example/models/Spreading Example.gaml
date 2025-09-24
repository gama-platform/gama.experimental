model SpreadingSkillObstacleExample

global {
   // === DATA FILES ===
   string selected_dem_path <- "../includes/SRTM_DEM_NHATLE_30M_RESIZED_BY_FOUR.tif";
   // Alternative DEM files (uncomment as needed):
   // string selected_dem_path <- "../includes/terrain_large.tif";
   // string selected_dem_path <- "../includes/hanoi.tif";
   file dem_file <- file(selected_dem_path);
   field elevation_map <- field(dem_file);
   geometry shape <- envelope(dem_file);
   string selected_water_path <- "../includes/water_multipolygon.shp";
   // Alternative water files (uncomment as needed):
   // string selected_water_path <- "../includes/river_clean.shp";
   file water_shapefile <- file(selected_water_path);
   list<geometry> water_geometries <- [];

   // === GLOBAL DISPLAY FIELDS ===
   field display_water_field;
   field display_obstacle_field;

   // === OBSTACLE BUILDING STATE ===
   bool obstacle_building_active <- false;
   point obstacle_point1 <- nil;
   point obstacle_point2 <- nil;
   bool waiting_for_first_point <- false;
   bool waiting_for_second_point <- false;

   // === OBSTACLE REMOVAL STATE ===
   bool obstacle_removal_active <- false;
   bool waiting_for_obstacle_removal <- false;

   // === SIMULATION STATISTICS ===
   int total_cycles <- 0;
   float simulation_start_time <- 0.0;

   init {
   // Load water geometries
   	water_geometries <- water_shapefile.contents;
   	write "=== INITIALIZATION ===";
   	write "Loaded " + length(water_geometries) + " water geometries";
   	write "DEM dimensions: " + elevation_map.columns + "x" + elevation_map.rows;
   	write "World bounds: " + shape;

   	// Initialize display fields with same dimensions as DEM  
   	display_water_field <- field(elevation_map.columns, elevation_map.rows);
   	display_obstacle_field <- field(elevation_map.columns, elevation_map.rows);

   	// Initialize fields to zero
   	loop i from: 0 to: elevation_map.columns - 1 {
   		loop j from: 0 to: elevation_map.rows - 1 {
   			display_water_field[i, j] <- 0.0;
   			display_obstacle_field[i, j] <- 0.0;
   		}

   	}

   	// Create simulation manager agent
   	create simulation_manager number: 1;
   	write "SpreadingSkill with Rain, Obstacles, and Removal system initialized";
   	write "Use buttons and controls to manage simulation, rain, obstacle building, and obstacle removal";
   	write "========================";
   }
   
      // === OBSTACLE REMOVAL ACTION ===
   action toggle_obstacle_removal {
   	ask simulation_manager {
   		do toggle_obstacle_removal_mode();
   		obstacle_removal_active <- is_obstacle_removal_mode();
   		if (obstacle_removal_active) {
   			write "🗑️ OBSTACLE REMOVAL MODE ACTIVATED";
   			write "   → Click on any obstacle cell to remove connected obstacle area";
   			write "   → All connected obstacle cells will be removed at once";
   			waiting_for_obstacle_removal <- true;
   			
   			// Disable obstacle building mode if it was active
   			if (obstacle_building_active) {
   				ask world {
   				do toggle_obstacle_building();
   				
   				}
   			}
   		} else {
   			write "🗑️ OBSTACLE REMOVAL MODE DEACTIVATED";
   			waiting_for_obstacle_removal <- false;
   		}
   	}
   }

   // === ENHANCED OBSTACLE BUILDING ACTIONS ===
   action toggle_obstacle_building {
   	ask simulation_manager {
   		do toggle_obstacle_building_mode();
   		obstacle_building_active <- is_obstacle_building_mode();
   		if (obstacle_building_active) {
   			write "🔨 OBSTACLE BUILDING MODE ACTIVATED";
   			write "   → Click first point in the 3D view";
   			write "   → World bounds: " + world.shape;
   			waiting_for_first_point <- true;
   			waiting_for_second_point <- false;
   			obstacle_point1 <- nil;
   			obstacle_point2 <- nil;
   			
   			// Disable obstacle removal mode if it was active
   			if (obstacle_removal_active) {
   				ask world {
   				do toggle_obstacle_removal();
   				
   				}
   			}
   		} else {
   			write "🔨 OBSTACLE BUILDING MODE DEACTIVATED";
   			waiting_for_first_point <- false;
   			waiting_for_second_point <- false;
   			obstacle_point1 <- nil;
   			obstacle_point2 <- nil;
   		}
   	}
   }



   // === ENHANCED MOUSE CLICK HANDLER ===
   action handle_obstacle_click (point click_location) {
   	if (obstacle_building_active) {
   		// OBSTACLE BUILDING LOGIC
   		if (waiting_for_first_point) {
   			obstacle_point1 <- click_location;
   			waiting_for_first_point <- false;
   			waiting_for_second_point <- true;
   			write "✓ First point selected: " + obstacle_point1;
   			write "   → Click second point to complete obstacle";
   		} else if (waiting_for_second_point) {
   			obstacle_point2 <- click_location;
   			waiting_for_second_point <- false;
   			write "✓ Second point selected: " + obstacle_point2;
   			write "   → Building obstacle...";

   			// Build the obstacle
   			ask simulation_manager {
   				bool success <- build_obstacle(obstacle_point1, obstacle_point2);
   				if (success) {
   					write "🏗️ Obstacle built successfully!";
   					write "   Active obstacles: " + get_active_obstacle_count();

   					// Update display field
   					if (obstacle_field != nil) {
   						display_obstacle_field <- obstacle_field;
   					}
   				} else {
   					write "❌ Failed to build obstacle";
   					write "   Possible reasons: not in building mode, area has water, or coordinates invalid";
   				}
   			}

   			// Reset for next obstacle
   			waiting_for_first_point <- true;
   			obstacle_point1 <- nil;
   			obstacle_point2 <- nil;
   			write "   → Ready for next obstacle (click first point)";
   		}
   	} else if (obstacle_removal_active) {
   		// OBSTACLE REMOVAL LOGIC
   		write "🗑️ Attempting to remove obstacle area at: " + click_location;
   		
   		// First, check if there's an obstacle at this location (for user feedback)
   		ask simulation_manager {
   			int area_size <- get_obstacle_area_size(click_location);
   			if (area_size > 0) {
   				write "   → Found connected obstacle area of " + area_size + " cells";
   				write "   → Removing obstacle area...";
   				
   				bool success <- remove_obstacle_area(click_location);
   				if (success) {
   					write "✅ Obstacle area removed successfully!";
   					write "   Remaining active obstacles: " + get_active_obstacle_count();
   					
   					// Update display fields
   					if (obstacle_field != nil) {
   						display_obstacle_field <- obstacle_field;
   					}
   					if (water_field != nil) {
   						display_water_field <- water_field;
   					}
   				} else {
   					write "❌ Failed to remove obstacle area";
   				}
   			} else {
   				write "❌ No obstacle found at clicked location";
   				write "   → Click on an obstacle cell to remove connected obstacle area";
   			}
   		}
   	} else {
   		// ENHANCED INFORMATION DISPLAY LOGIC
   		if (display_water_field != nil) {
   			float water_level <- display_water_field[click_location];
   			if (water_level > 0.01) {
   				write "💧 Water level: " + (water_level with_precision 2) + "m";
   			} else {
   				write "🏞️ No water at this location";
   			}
   		}

   		// Show terrain elevation
   		if (elevation_map != nil) {
   			float terrain_height <- elevation_map[click_location];
   			write "🏔️ Terrain elevation: " + (terrain_height with_precision 2) + "m";
   		}

   		// Show obstacle elevation if present
   		if (display_obstacle_field != nil) {
   			float obstacle_height <- display_obstacle_field[click_location];
   			if (obstacle_height > 0.01) {
   				write "🏗️ Obstacle total elevation: " + (obstacle_height with_precision 2) + "m";
   				
   				// Show obstacle area size for information
   				ask simulation_manager {
   					int area_size <- get_obstacle_area_size(click_location);
   					if (area_size > 0) {
   						write "🔗 Connected obstacle area size: " + area_size + " cells";
   					}
   					// Show obstacle type and properties
   					string obstacle_info <- get_obstacle_info_at_point(click_location);
   					write "ℹ️ " + obstacle_info;
   				}
   			}
   		}

   		write "📍 Click coordinates: " + click_location;
   	}
   }

   // === ENHANCED GLOBAL STATUS REPORTING ===
   action report_global_status {
   	write "=== GLOBAL SIMULATION STATUS ===";
   	write "Total simulation cycles: " + total_cycles;
   	write "Obstacle building mode: " + (obstacle_building_active ? "ACTIVE" : "INACTIVE");
   	write "Obstacle removal mode: " + (obstacle_removal_active ? "ACTIVE" : "INACTIVE");
   	
   	if (obstacle_building_active) {
   		string status <- waiting_for_first_point ? "Waiting for first point" : (waiting_for_second_point ? "Waiting for second point" : "Ready");
   		write "Obstacle building status: " + status;
   		if (obstacle_point1 != nil) {
   			write "First point: " + obstacle_point1;
   		}
   	}
   	
   	if (obstacle_removal_active) {
   		write "Obstacle removal status: " + (waiting_for_obstacle_removal ? "Click on obstacle to remove" : "Ready");
   	}

   	write "World envelope: " + world.shape;
   	write "DEM grid size: " + elevation_map.columns + "x" + elevation_map.rows;
   	ask simulation_manager {
   		do report_status();
   	}
   }

   // Track simulation cycles
   reflex count_cycles {
   	total_cycles <- total_cycles + 1;
   }
   
	bool eval_finish (map<string, map> input_map) {
		write input_map["page1"]["file"];
		return input_map["page1"]["file"] != nil;
	}
   
		// === FILE BROWSING ACTIONS ===
	action browse_dem_file {
		map<string, map> results <- wizard("My wizard", eval_finish, [wizard_page("page1", "Browse DEM file (.tif)", [enter("file", file)], font("Helvetica", 14, #bold))]);
		string new_dem_path <- nil;
		if (results != nil) {
			if (results["page1"] != nil) {
				if (results["page1"]["file"] != nil) {
					new_dem_path <- results["page1"]["file"];
				}

			}
		}

		if (new_dem_path != nil and new_dem_path != "") {
			selected_dem_path <- new_dem_path;
			write "📁 DEM file selected: " + selected_dem_path;
			write "   → Press 'Update Files' to apply changes";
		} else {
			write "❌ DEM file selection cancelled";
		}
	} 
	
	action browse_water_file {
		map<string, map> results <- wizard("My wizard", eval_finish, [wizard_page("page1", "Browse Water file (.shp)", [enter("file", file)], font("Helvetica", 14, #bold))]);
		string new_water_path <- nil;
		if (results != nil) {
			if (results["page1"] != nil) {
				if (results["page1"]["file"] != nil) {
					new_water_path <- results["page1"]["file"];
				}

			}
		}

		if (new_water_path != nil and new_water_path != "") {
			selected_water_path <- new_water_path;
			write "📁 Water file selected: " + selected_water_path;
			write "   → Press 'Update Files' to apply changes";
		} else {
			write "❌ Water file selection cancelled";
		}
	}
	
	// === NEW: ADD OBSTACLES FROM SHAPEFILE ACTION ===
	action add_obstacles_from_shapefile(file shapefile_to_load, string obstacle_type, float height, bool uniform_height, bool destroyable, float destruction_time) {
		list<geometry> obstacle_geometries <- shapefile_to_load.contents;
		if (length(obstacle_geometries) = 0) {
			write "❌ No geometries found in shapefile";
			return;
		}
		
		ask simulation_manager {
			bool success <- add_obstacles_from_shapefile(
				obstacle_shapefile: obstacle_geometries,
				obstacle_type: obstacle_type,
				height: height,
				uniform_height: uniform_height,
				destroyable: destroyable,
				destruction_time: destruction_time
			);
			
			if (success) {
				write "✅ Obstacles added from shapefile successfully!";
				write "   Type: " + obstacle_type;
				write "   Height: " + height + "m";
				write "   Uniform height: " + uniform_height;
				write "   Destroyable: " + destroyable;
				if (destroyable) {
					write "   Destruction time: " + destruction_time + " cycles";
				}
				
				// Update display field
				if (obstacle_field != nil) {
					display_obstacle_field <- obstacle_field;
				}
			} else {
				write "❌ Failed to add obstacles from shapefile";
			}
		}
	}
}

   species simulation_manager skills: [spreading] {

   // === PERFORMANCE TRACKING ===
   int steps_since_spread <- 0;
   int last_water_count <- 0;
   int last_field_update <- 0;

   init {
   	write "=== SIMULATION MANAGER INITIALIZATION ===";
   	write "Creating simulation manager with full SpreadingSkill capabilities:";
   	write "  • Water spreading simulation with realistic physics";
   	write "  • Rain system with intensity control";
   	write "  • Obstacle building with continuity and destruction mechanics";
   	write "  • Obstacle removal with connected component detection";
   	write "  • Support for multiple obstacle types (dyke, building, wall, earthwork, barrier)";

   	// Create obstacle field with same dimensions as DEM
   	field obstacle_field_for_java <- field(elevation_map.columns, elevation_map.rows);

   	// Initialize obstacle field with proper bounds
   	loop i from: 0 to: elevation_map.columns - 1 {
   		loop j from: 0 to: elevation_map.rows - 1 {
   			obstacle_field_for_java[i, j] <- 0.0;
   		}

   	}

   	// Use coordinate-aligned initialization
   	do
   	initialize_spreading_grid_with_obstacle_field(dem_field: elevation_map, obstacle_field: obstacle_field_for_java, water_geometries: water_geometries, initial_water_depth: 1.5, flow_threshold: 0.01, rising_rate: 0.3, min_flow_diff: 0.001, equalization_threshold: 0.1);

   	// PERFORMANCE FIX: Simple initial field sync, let Java handle the details
   	if (water_field != nil) {
   		display_water_field <- water_field;
   	}

   	if (obstacle_field != nil) {
   		display_obstacle_field <- obstacle_field;
   	}

   	last_water_count <- get_active_water_count();
   	write "✓ Spreading grid initialized successfully";
   	write "✓ Initial water cells: " + get_active_water_count();
   	write "✓ Initial edge cells: " + get_edge_cell_count();
   	write "✓ Grid dimensions: " + grid_width + "x" + grid_height;
   	write "✓ Performance-optimized field synchronization enabled";
   	write "✓ Obstacle removal system ready";
   	write "✓ Simulation parameters:";
   	write "    Flow threshold: " + (flow_threshold with_precision 3) + "m";
   	write "    Rising rate: " + (rising_rate with_precision 3) + "m/step";
   	write "    Default obstacle height: " + (default_obstacle_height with_precision 1) + "m";
   	write "    Obstacle destruction time: " + (obstacle_destruction_time with_precision 1) + " cycles";
   	write "============================================";
   }

   // === PERFORMANCE-OPTIMIZED SIMULATION STEP ===
   reflex simulation_step when: is_simulation_active() {
   	do simulate_spreading_step();

   	// PERFORMANCE FIX: Lightweight field synchronization
   	// Only update display fields when necessary, let Java handle the heavy lifting
   	if (water_field != nil) {
   		display_water_field <- water_field;
   	}

   	// Update obstacle field less frequently (obstacles change rarely)
   	if (obstacle_field != nil and (cycle mod 10 = 0 or last_field_update = 0)) {
   		display_obstacle_field <- obstacle_field;
   		last_field_update <- cycle;
   	}

   	// Track progress efficiently
   	int current_water_count <- get_active_water_count();
   	if (current_water_count != last_water_count) {
   		steps_since_spread <- 0;
   		last_water_count <- current_water_count;

   		// Only print significant changes to avoid spam
   		if (current_water_count mod 100 = 0 or current_water_count < 100) {
   			write "Water spreading: " + current_water_count + " cells";
   		}

   	} else {
   		steps_since_spread <- steps_since_spread + 1;
   	}

   	// Alert if obstacles are in danger (but not too frequently)
   	if (cycle mod 20 = 0) {
   		int attacked_obstacles <- get_obstacles_under_attack();
   		if (attacked_obstacles > 0) {
   			write "⚔️ WARNING: " + attacked_obstacles + " obstacle cell(s) under water attack!";
   		}

   	}

   }

   // === SIMULATION CONTROL ACTIONS ===
   action start_spreading {
   	do start_spreading_simulation();
   	simulation_start_time <- gama.machine_time;
   	write "🚀 SIMULATION STARTED at step " + get_current_step();
   	write "   Water cells: " + get_active_water_count() + " | Edge cells: " + get_edge_cell_count();
   	if (get_active_obstacle_count() > 0) {
   		write "   Active obstacles: " + get_active_obstacle_count();
   	}

   }

   action stop_spreading {
   	do stop_spreading_simulation();
   	write "⏸️ SPREADING STOPPED at step " + get_current_step();
   	write "   Total runtime: " + ((gama.machine_time - simulation_start_time) / 1000.0 with_precision 1) + " seconds";
   	write "   Final water cells: " + get_active_water_count();
   }

   action reset_spreading {
   	do reset_spreading_simulation(water_geometries, 1.5);

   	// Recreate obstacle field with same dimensions after reset
   	field obstacle_field_for_reset <- field(elevation_map.columns, elevation_map.rows);
   	loop i from: 0 to: elevation_map.columns - 1 {
   		loop j from: 0 to: elevation_map.rows - 1 {
   			obstacle_field_for_reset[i, j] <- 0.0;
   		}

   	}

   	// Re-initialize with proper coordinate alignment
   	do
   	initialize_spreading_grid_with_obstacle_field(dem_field: elevation_map, obstacle_field: obstacle_field_for_reset, water_geometries: water_geometries, initial_water_depth: 1.5, flow_threshold: 0.01, rising_rate: 0.3, min_flow_diff: 0.001, equalization_threshold: 0.1);

   	// PERFORMANCE FIX: Simple field sync after reset
   	if (water_field != nil) {
   		display_water_field <- water_field;
   	}

   	if (obstacle_field != nil) {
   		display_obstacle_field <- obstacle_field;
   	}

   	// Reset tracking variables
   	steps_since_spread <- 0;
   	last_water_count <- get_active_water_count();
   	last_field_update <- 0;
   	write "🔄 SIMULATION RESET";
   	write "   Active water cells: " + get_active_water_count();
   	write "   Edge cells: " + get_edge_cell_count();
   	write "   All obstacles cleared, rain stopped, building/removal modes disabled";
   	write "   Performance-optimized field synchronization restored";
   }

   // === RAIN CONTROL ACTIONS ===
   action start_light_rain {
   	do start_rain(0.1, 1.0);
   	write "🌧️ Light rain started (0.1m/step, normal intensity)";
   }

   action start_heavy_rain {
   	do start_rain(0.3, 1.5);
   	write "⛈️ Heavy rain started (0.3m/step, 1.5x intensity)";
   }

   action start_extreme_rain {
   	do start_rain(0.5, 2.0);
   	write "🌪️ EXTREME rain started (0.5m/step, 2x intensity)";
   }

   action start_custom_rain (float rate, float intensity) {
   	do start_rain(rate, intensity);
   	write "🌦️ Custom rain started (rate: " + rate + "m/step, intensity: " + intensity + "x)";
   }

   action increase_rain {
   	if (is_rain_active()) {
   		float current_rate <- get_rain_rate();
   		float new_rate <- current_rate + 0.05;
   		do set_rain_rate(new_rate);
   		write "📈 Rain increased to " + (get_rain_rate() with_precision 2) + "m/step";
   	} else {
   		write "❌ No rain active to increase";
   	}

   }

   action decrease_rain {
   	if (is_rain_active()) {
   		float current_rate <- get_rain_rate();
   		float new_rate <- max(0.0, current_rate - 0.05);
   		if (new_rate <= 0.0) {
   			do stop_rain();
   			write "🌤️ Rain stopped (rate reached 0)";
   		} else {
   			do set_rain_rate(new_rate);
   			write "📉 Rain decreased to " + (get_rain_rate() with_precision 2) + "m/step";
   		}

   	} else {
   		write "❌ No rain active to decrease";
   	}

   }

   action stop_rain_completely {
   	do stop_rain();
   	write "☀️ Rain stopped completely";
   }

   // === OBSTACLE CONTROL ACTIONS ===
   action clear_obstacles {
   	int obstacle_count <- get_active_obstacle_count();
   	do clear_all_obstacles();

   	// Simple field update after clearing obstacles
   	if (obstacle_field != nil) {
   		display_obstacle_field <- obstacle_field;
   	}

   	write "💥 All obstacles cleared (" + obstacle_count + " obstacle cells removed)";
   }

   action emergency_obstacle_clear {
   	ask world {
   		if (obstacle_building_active) {
   			do toggle_obstacle_building(); // Turn off building mode
   		}
   		if (obstacle_removal_active) {
   			do toggle_obstacle_removal(); // Turn off removal mode
   		}
   	}

   	do clear_obstacles();
   	write "🚨 EMERGENCY: All obstacles cleared and all obstacle modes disabled";
   }

   // === OBSTACLE REMOVAL CONTROL ACTIONS ===
   action toggle_removal_mode {
   	ask world {
   		do toggle_obstacle_removal();
   	}
   }

   action remove_obstacle_at_point (point location) {
   	int area_size <- get_obstacle_area_size(location);
   	if (area_size > 0) {
   		write "🗑️ Removing obstacle area of " + area_size + " cells...";
   		bool success <- remove_obstacle_area(location);
   		if (success) {
   			write "✅ Obstacle area removed successfully!";
   			
   			// Update display fields
   			if (obstacle_field != nil) {
   				display_obstacle_field <- obstacle_field;
   			}
   			if (water_field != nil) {
   				display_water_field <- water_field;
   			}
   		}
   	} else {
   		write "❌ No obstacle area found at specified location";
   	}
   }

   // === ENHANCED STATUS REPORTING ===
   action report_status {
   	write "=== DETAILED SIMULATION STATUS ===";
   	write "SIMULATION:";
   	write "  • Active: " + is_simulation_active();
   	write "  • Step: " + get_current_step();
   	write "  • Steps since last spread: " + steps_since_spread;
   	write "WATER:";
   	write "  • Active water cells: " + get_active_water_count();
   	write "  • Edge cells: " + get_edge_cell_count();
   	write "  • Flow threshold: " + (flow_threshold with_precision 3) + "m";
   	write "  • Rising rate: " + (rising_rate with_precision 3) + "m/step";
   	write "RAIN:";
   	write "  • Rain active: " + is_rain_active();
   	if (is_rain_active()) {
   		write "  • Rain rate: " + (get_rain_rate() with_precision 3) + "m/step";
   		write "  • Rain intensity: " + (get_rain_intensity() with_precision 1) + "x";
   		float rain_volume <- get_rain_rate() * get_active_water_count();
   		write "  • Total rain volume/step: " + (rain_volume with_precision 1) + "m³";
   	}

   	write "OBSTACLES:";
   	write "  • Building mode: " + is_obstacle_building_mode();
   	write "  • Removal mode: " + is_obstacle_removal_mode();
   	write "  • Active obstacle cells: " + get_active_obstacle_count();
   	write "  • Obstacle cells under attack: " + get_obstacles_under_attack();
   	write "  • Default obstacle height: " + (default_obstacle_height with_precision 1) + "m";
   	write "  • Destruction time: " + (obstacle_destruction_time with_precision 1) + " cycles";
   	if (get_obstacles_under_attack() > 0) {
   		write "  ⚔️ ALERT: Obstacle cells under water attack!";
   	}

   	write "GRID:";
   	write "  • Grid size: " + grid_width + "x" + grid_height;
   	write "  • Total cells: " + (grid_width * grid_height);
   	write "  • Water coverage: " + ((get_active_water_count() / (grid_width * grid_height)) * 100.0 with_precision 1) + "%";
   	write "===================================";
   }

   action quick_status {
   	string sim_status <- is_simulation_active() ? "RUNNING" : "STOPPED";
   	string rain_status <- is_rain_active() ? ("RAIN " + (get_rain_rate() with_precision 1) + "m/s") : "NO RAIN";
   	string obstacle_status <- " " + get_active_obstacle_count() + " obstacle cells";
   	if (get_obstacles_under_attack() > 0) {
   		obstacle_status <- obstacle_status + " (" + get_obstacles_under_attack() + " under attack)";
   	}
   	string mode_status <- "";
   	if (is_obstacle_building_mode()) {
   		mode_status <- " | BUILDING";
   	} else if (is_obstacle_removal_mode()) {
   		mode_status <- " | REMOVAL";
   	}

   	write "📊 QUICK STATUS: " + sim_status + " | " + rain_status + " | " + obstacle_status + mode_status + " | Water: " + get_active_water_count() + " cells";
   } 
   }

   experiment FloodSimulationWithObstaclesComplete type: gui {
   int auto_report_interval <- 50;

   // === EXPERIMENT REFLEXES ===
   reflex auto_report when: auto_report_interval > 0 and (cycle mod auto_report_interval = 0) and cycle > 0 {
   	ask simulation_manager {
   		do quick_status();
   	}

   }

   output {
   // === MAIN 3D VISUALIZATION WITH ENHANCED INTERACTIONS ===
   	display "Flood Simulation with Obstacles and Removal" type: opengl refresh: true {
   	// Terrain elevation (grayscale with proper scaling)
   		mesh elevation_map scale: 20 triangulation: true grayscale: true transparency: 0.1 refresh: false;
   		light #ambient intensity: 100;

   		// Water field with proper scale and triangulation
   		mesh display_water_field scale: 20 triangulation: true color: rgb(0, 100, 255, 180) refresh: true;

   		// Obstacle field with proper scale and triangulation  
   		mesh display_obstacle_field scale: 20 triangulation: true color: rgb(139, 69, 19, 255) refresh: true;

   		// Simulation manager (optional visualization)
   		species simulation_manager transparency: 0.8;

   		// === EVENT HANDLERS ===
   		event #mouse_down {
   			ask world {
   				do handle_obstacle_click(#user_location);
   			}

   		}

   		event #mouse_move {
   		// Show information on mouse move
   			if (!obstacle_building_active and !obstacle_removal_active) {
   				if (display_water_field != nil) {
   					float water_level <- display_water_field[#user_location];
   					if (water_level > 0.01) {
   						draw string("💧 " + (water_level with_precision 1) + "m") at: #user_location + {0, 0, 10} color: #cyan font: font("Arial", 12, #bold);
   					}

   				}

   				if (display_obstacle_field != nil) {
   					float obstacle_height <- display_obstacle_field[#user_location];
   					if (obstacle_height > 0.01) {
   						draw string("🏗️ " + (obstacle_height with_precision 1) + "m") at: #user_location + {0, 0, 15} color: #orange font: font("Arial", 12, #bold);
   					}
   				}
   			} else if (obstacle_removal_active) {
   				// Show obstacle area size preview during removal mode
   				if (display_obstacle_field != nil) {
   					float obstacle_height <- display_obstacle_field[#user_location];
   					if (obstacle_height > 0.01) {
   						ask first(simulation_manager) {
   							int area_size <- get_obstacle_area_size(#user_location);
   							if (area_size > 0) {
   								draw string("🗑️ Remove " + area_size + " cells") at: #user_location + {0, 0, 20} color: #red font: font("Arial", 12, #bold);
   							}
   						}
   					}
   				}
   			}
   		}

   		// === ENHANCED VISUAL OVERLAYS ===
   		overlay position: {5, 5} size: {400, 200} background: #black transparency: 0.3 border: #white {
   			string title <- "🌊 FLOOD SIMULATION CONTROL";
   			string sim_text <- "Simulation: " + (first(simulation_manager).is_simulation_active() ? "RUNNING" : "STOPPED");
   			string water_text <- "Water Cells: " + first(simulation_manager).get_active_water_count();
   			string rain_text <- "Rain: " + (first(simulation_manager).is_rain_active() ? ("Active (" + (first(simulation_manager).get_rain_rate() with_precision 1) + "m/s)") : "None");
   			string obstacle_text <- "Obstacle Cells: " + first(simulation_manager).get_active_obstacle_count() + " active";
   			if (first(simulation_manager).get_obstacles_under_attack() > 0) {
   				obstacle_text <- obstacle_text + " (" + first(simulation_manager).get_obstacles_under_attack() + " under attack)";
   			}

   			string mode_text <- "Mode: ";
   			if (obstacle_building_active) {
   				mode_text <- mode_text + (waiting_for_first_point ? "🔨 Click 1st point" : (waiting_for_second_point ? "🔨 Click 2nd point" : "🔨 Building"));
   			} else if (obstacle_removal_active) {
   				mode_text <- mode_text + "🗑️ Click obstacle to remove";
   			} else {
   				mode_text <- mode_text + "Normal";
   			}
   			
   			draw title at: {10, 20} color: #yellow font: font("Arial", 14, #bold);
   			draw sim_text at: {10, 40} color: (first(simulation_manager).is_simulation_active() ? #green : #red);
   			draw water_text at: {10, 55} color: #cyan;
   			draw rain_text at: {10, 70} color: (first(simulation_manager).is_rain_active() ? #blue : #gray);
   			draw obstacle_text at: {10, 85} color: #brown;
   			draw mode_text at: {10, 100} color: (obstacle_building_active ? #orange : (obstacle_removal_active ? #red : #white));
   			
   			if (obstacle_building_active and obstacle_point1 != nil) {
   				draw ("First: " + obstacle_point1) at: {10, 115} color: #orange;
   			}
   			if (obstacle_removal_active) {
   				draw "Click obstacle cell to remove area" at: {10, 130} color: #red font: font("Arial", 10, #italic);
   			}
   			if (obstacle_building_active or obstacle_removal_active) {
   				draw "Modes are mutually exclusive" at: {10, 145} color: #gray font: font("Arial", 9, #italic);
   			}
   			
   			// NEW: Show supported obstacle types
   			draw "Obstacle types: dyke, building, wall, earthwork, barrier" at: {10, 160} color: #lightgray font: font("Arial", 9, #italic);
   		}

   	}

   	// === CONTROL CHARTS ===
   	display "Simulation Progress" type: java2D {
   		chart "Water Spread and System Status" type: series x_serie_labels: "Step" {
   			data "Active Water Cells" value: first(simulation_manager).get_active_water_count() color: #blue;
   			data "Edge Cells" value: first(simulation_manager).get_edge_cell_count() color: #red;
   			data "Simulation Step" value: first(simulation_manager).get_current_step() color: #green;
   			data "Rain Rate x100" value: first(simulation_manager).get_rain_rate() * 100 color: #purple;
   			data "Active Obstacle Cells" value: first(simulation_manager).get_active_obstacle_count() color: #brown;
   			data "Attacked Obstacle Cells x10" value: first(simulation_manager).get_obstacles_under_attack() * 10 color: #orange;
   		}

   	}

   	// === ENHANCED STATUS MONITORS ===
   	// Simulation monitors
   	monitor "🚀 Simulation Active" value: first(simulation_manager).is_simulation_active() color: first(simulation_manager).is_simulation_active() ? #green : #red;
   	monitor "📊 Current Step" value: first(simulation_manager).get_current_step();
   	monitor "💧 Active Water Cells" value: first(simulation_manager).get_active_water_count();
   	monitor "🔥 Edge Cells" value: first(simulation_manager).get_edge_cell_count();

   	// Rain monitors 
   	monitor "🌧️ Rain Active" value: first(simulation_manager).is_rain_active() color: first(simulation_manager).is_rain_active() ? #blue : #gray;
   	monitor "📈 Rain Rate (m/step)" value: first(simulation_manager).get_rain_rate() with_precision 3;
   	monitor "⚡ Rain Intensity" value: first(simulation_manager).get_rain_intensity() with_precision 1;

   	// Enhanced obstacle monitors
   	monitor "🔨 Obstacle Building Mode" value: obstacle_building_active color: obstacle_building_active ? #orange : #gray;
   	monitor "🗑️ Obstacle Removal Mode" value: obstacle_removal_active color: obstacle_removal_active ? #red : #gray;
   	monitor "🏗️ Active Obstacle Cells" value: first(simulation_manager).get_active_obstacle_count();
   	monitor "⚔️ Obstacle Cells Under Attack" value: first(simulation_manager).get_obstacles_under_attack() color: first(simulation_manager).get_obstacles_under_attack() > 0 ?
   	#red : #green;
   	monitor "🎯 Building Status" value: obstacle_building_active ? (waiting_for_first_point ? "First Point" : (waiting_for_second_point ? "Second Point" : "Ready")) : "Inactive";
   	monitor "🎯 Removal Status" value: obstacle_removal_active ? (waiting_for_obstacle_removal ? "Click Obstacle" : "Ready") : "Inactive";

   	// Performance monitors
   	monitor "⏱️ Total Cycles" value: total_cycles;
   	monitor "📏 Grid Size" value: string(first(simulation_manager).grid_width) + "x" + string(first(simulation_manager).grid_height);
   	monitor "💧 Water Coverage %" value:
   	((first(simulation_manager).get_active_water_count() / (first(simulation_manager).grid_width * first(simulation_manager).grid_height)) * 100.0) with_precision 1;
   }

   // === ACTION DEFINITIONS ===
   // Simulation control actions
   action ask_start_spreading {
   	ask simulation_manager {
   		do start_spreading();
   	}

   }

   action ask_stop_spreading {
   	ask simulation_manager {
   		do stop_spreading();
   	}

   }

   action ask_reset_spreading {
   	ask simulation_manager {
   		do reset_spreading();
   	}

   }

   action ask_report_status {
   	ask simulation_manager {
   		do report_status();
   	}

   }

   action ask_quick_status {
   	ask simulation_manager {
   		do quick_status();
   	}

   }

   action ask_global_status {
   	ask world {
   		do report_global_status();
   	}

   }

   // Rain control actions
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

   action ask_start_extreme_rain {
   	ask simulation_manager {
   		do start_extreme_rain();
   	}

   }

   action ask_stop_rain {
   	ask simulation_manager {
   		do stop_rain_completely();
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

   // Enhanced obstacle control actions
   action ask_toggle_obstacle_building {
   	ask world {
   		do toggle_obstacle_building();
   	}

   }

   action ask_toggle_obstacle_removal {
   	ask world {
   		do toggle_obstacle_removal();
   	}

   }

   action ask_clear_obstacles {
   	ask simulation_manager {
   		do clear_obstacles();
   	}

   }

   action ask_emergency_clear {
   	ask simulation_manager {
   		do emergency_obstacle_clear();
   	}

   }
   
   	action ask_browse_dem {
		ask world {
			do browse_dem_file();
		}

	}
	
	action ask_browse_water {
		ask world {
			do browse_water_file();
		}
	}
	
	action ask_update_files {
		string new_selected_dem_path <- selected_dem_path;
		string new_selected_water_path <- selected_water_path;
		ask simulation {
			do die;
		}

		create simulation with: [selected_dem_path::new_selected_dem_path, selected_water_path::new_selected_water_path];
	}

   // === ENHANCED USER COMMAND INTERFACE ===
   // Primary simulation controls
   user_command "🚀 Start Spreading" action: ask_start_spreading category: "Simulation";
   user_command "⏸️ Stop Spreading" action: ask_stop_spreading category: "Simulation";
   user_command "🔄 Reset Spreading" action: ask_reset_spreading category: "Simulation";

   // Rain controls
   user_command "🌧️ Light Rain" action: ask_start_light_rain category: "Rain";
   user_command "⛈️ Heavy Rain" action: ask_start_heavy_rain category: "Rain";
   user_command "🌪️ Extreme Rain" action: ask_start_extreme_rain category: "Rain";
   user_command "☀️ Stop Rain" action: ask_stop_rain category: "Rain";
   user_command "📈 Increase Rain" action: ask_increase_rain category: "Rain";
   user_command "📉 Decrease Rain" action: ask_decrease_rain category: "Rain";

   // Enhanced obstacle controls 
   user_command "🔨 Build Obstacles (Toggle)" action: ask_toggle_obstacle_building category: "Obstacles";
   user_command "🗑️ Remove Obstacles (Toggle)" action: ask_toggle_obstacle_removal category: "Obstacles";
   user_command "💥 Clear All Obstacles" action: ask_clear_obstacles category: "Obstacles";
   user_command "🚨 Emergency Clear" action: ask_emergency_clear category: "Obstacles";

   // Status and monitoring
   user_command "📊 Quick Status" action: ask_quick_status category: "Status";
   user_command "📋 Full Report" action: ask_report_status category: "Status";
   user_command "🌍 Global Status" action: ask_global_status category: "Status";
   
   // Browse file
   user_command "📁 Browse DEM File" action: ask_browse_dem category: "Files";
   user_command "📁 Browse Water File" action: ask_browse_water category: "Files";
   user_command "📁 Update Files"  action: ask_update_files category: "Files";
}