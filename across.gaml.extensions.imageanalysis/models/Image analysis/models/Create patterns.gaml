/**
* Name: NewModel
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model NewModel

global {
	image_file real_map_image_file <- image_file("../includes/real_map.jpg");

	string image_crop_name <- "real_map_crop.jpg";
	string pattern_folder <- "../includes/patterns";
	
	int max_detection_objects <- 20;
	rgb color_pt_crop <- #red;
	float point_size_crop <- 0.25;
	rgb color_mouse_crop <- #cyan;
	float mouse_size_crop <- 0.5;
	
	rgb color_pt_pattern <- #green;
	float point_size_pattern <- 0.25;
	rgb color_mouse_pattern <- #yellow;
	float mouse_size_pattern <- 0.5;
	
	
	string path_to_global_image <- real_map_image_file.path;
	bool pattern_definition <- false;
	bool crop_world <- false;
	point first_pt <- nil;
	point last_pt <- nil;
	geometry geom_pt <- nil;
	
	bool follow_mouse <- false;
	point mouse_loc <- nil;
	
	
	int init_score_max <- 50 min: 0 max: 400 ;
	int current_score_max <- 50 min: 0 max: 400 parameter: true on_change: update_score;
	
	float x_coeff ;
	float y_coeff ;
	float x_offset;
	float y_offset;
	
	geometry mask_world_geom;
	geometry crop_world_geom;
	geometry temp_geometry;
	
	int x_res;
	int y_res;
	pattern concerned_pattern <- nil;
	init {
		matrix mat <- matrix(real_map_image_file);
		x_res <- mat.columns;
		y_res <- mat.rows;
		x_coeff <- shape.width / x_res;
		y_coeff <- shape.height / y_res;
		x_offset <- 0.0;
		y_offset <- 0.0;
		
		if ! folder_exists(pattern_folder) {
			file folder <- new_folder(pattern_folder);
		}
		
		
	}
	
	action save_patterns {
		map result <- user_input_dialog("Save patterns", [enter("Model file name", string, "Init patterns.gaml"), enter("Pattern file name", string, "patterns.csv")]);
		string p1 <- result["Model file name"];
		string p2 <- result["Pattern file name"];
		string path_pa <-  pattern_folder +"/" + p2;
		save "name,color,score_max,pattern_image_file" type:text to: path_pa;
		ask pattern {
			save [name,color,score_max,pattern_image_file] type: csv to: path_pa rewrite: false;
		}
		
		string model_str <- "model pattern_model \n\n";
		model_str <- model_str + "global {\n";
		model_str <- model_str + "\tfloat x_coeff <- " + x_coeff + ";\n";
		model_str <- model_str + "\tfloat y_coeff <- " + y_coeff + ";\n";
		model_str <- model_str + "\tfloat x_offset <- " + x_offset + ";\n";
		model_str <- model_str + "\tfloat y_offset <- " + y_offset + ";\n";
		model_str <- model_str + "\tint max_detection_objects <- " + max_detection_objects + ";\n";
	
		model_str <- model_str + "\n\taction initialize {\n\t\tcsv_file pattern_file <- csv_file(\"" + path_pa +"\",\",\",string,true);\n\t\tcreate pattern from:pattern_file with:(name:get(\"name\"), color:rgb(get(\"color\")), score_max:int(get(\"score_max\")),pattern_image_file:get(\"pattern_image_file\"));"  ;
		
		model_str <- model_str + "\n\t}";
		
		model_str <- model_str + "\n\n\taction indentify_all_matching_objects(string path_to_global_image) {\n\t\task pattern {\n\t\t\tdo indentify_matching_objects(path_to_global_image);\n\t\t}"  ;
		
		model_str <- model_str + "\n\t}";
		
		
		
		model_str <- model_str + "\n}";
		model_str <- model_str + "\nspecies object{\n\tpattern my_pattern;\n\tint score;\n}";

		model_str <- model_str + "\nspecies pattern{\n\trgb color;\n\tint score_max;\n\tstring pattern_image_file;\n\n\taction indentify_matching_objects(string path_to_global_image) {\n\t\tmap res <- image_matching(path_to_global_image,[pattern_image_file::name], max_detection_objects);\n\t\tloop r over: res.keys {\n\t\t\tfloat score <- float(map(res[r])[\"SCORE\"]);\n\t\t\tif score <= score_max {\n\t\t\t\tgeometry g <- polygon(geometry(r).points collect {(each.x * x_coeff) + x_offset,(each.y * y_coeff) + y_offset}); \n\t\t\t\tcreate object with: (shape:g.contour + 0.1, my_pattern:self, score:int(-1 * score));\n\t\t\t}\n\t\t}\n\t}\n}";
		
		save model_str to: p1 type:text;
		
				
	}
	
	action update_score {
		if concerned_pattern != nil {
			concerned_pattern.score_max <- current_score_max;
			ask experiment {
				do update_outputs(true);
			}
		}
		
		
	}
	
	action mode_pattern {
		pattern_definition <- not pattern_definition;
		first_pt <- nil;
		last_pt <- nil;
		geom_pt <- nil;
		temp_geometry <- nil;
		concerned_pattern <- nil;
		crop_world <- false;
	}
	
	action mode_crop_world {
		crop_world <- not crop_world;
		pattern_definition <- false;
		first_pt <- nil;
		last_pt <- nil;
		geom_pt <- nil;
		concerned_pattern <- nil;
		mask_world_geom <- nil;
		temp_geometry <- nil;
		crop_world_geom <- nil;
		x_coeff <- shape.width / x_res;
		y_coeff <- shape.height / y_res;
		x_offset <- 0.0;
		y_offset <- 0.0;
	}
	
	action mouse_move_action {
		if pattern_definition or crop_world{
			mouse_loc <- #user_location;
			if first_pt != nil and last_pt = nil {
				temp_geometry <- polygon([first_pt, {first_pt.x,mouse_loc.y },mouse_loc, {mouse_loc.x,first_pt.y }]).contour;
				
			}
			
		}
	}
	
	
	action mouse_down_action {
		if pattern_definition or crop_world {
			if geom_pt != nil {
				geom_pt <- nil;
				first_pt <- nil;
				last_pt <- nil;
				temp_geometry <- nil;
			}
			if first_pt = nil and (#user_location overlaps (crop_world_geom = nil ? world : crop_world_geom)){
				first_pt <- #user_location;
			}
			else if  (#user_location overlaps (crop_world_geom = nil ? world : crop_world_geom)){
				last_pt <- #user_location;
				temp_geometry <- nil;
				geom_pt <- polygon([first_pt, {first_pt.x,last_pt.y },last_pt, {last_pt.x,first_pt.y }]);
				if pattern_definition {
					map result <- user_input_dialog("New Pattern", [enter("Pattern name", string, ""), enter("Pattern color", rgb, rnd_color(255))]);
					string name_pattern <- result["Pattern name"];
					rgb color_pattern <- result["Pattern color"];
					if name_pattern != "" {
						string new_pattern_path <- crop_image(pattern_folder + "/" + name_pattern + ".png",geom_pt,path_to_global_image, crop_world_geom );
						create pattern with: (pattern_image_file:new_pattern_path, score_max:init_score_max, name: name_pattern, color:color_pattern ) {
							do indentify_matching_objects;
						}
					}
				} else if crop_world {
					string new_image_path <- crop_image(real_map_image_file.path replace (real_map_image_file.name,image_crop_name),geom_pt,real_map_image_file.path, world.shape);
					if new_image_path != nil {
						ask object {
							do die;
						}
						ask pattern {
							do indentify_matching_objects;
						}
						path_to_global_image <-copy(new_image_path);
						crop_world_geom <- copy(geom_pt);
						mask_world_geom <- world.shape - crop_world_geom;
						matrix mat <- matrix(image_file(path_to_global_image));
						x_coeff <- crop_world_geom.width / mat.columns;
						y_coeff <- crop_world_geom.height / mat.rows;
						x_offset <- crop_world_geom.points min_of each.x;
						y_offset <- crop_world_geom.points min_of each.y;
					}
					
						
				}
			}
			
		}
	}
	
}


species pattern {
	rgb color;
	int score_max;
	string pattern_image_file;
	
	action indentify_matching_objects {
		map res <- image_matching(path_to_global_image,[pattern_image_file::name], max_detection_objects);
		loop r over: res.keys {
			float score <- float(map(res[r])["SCORE"]);
			geometry g <- polygon(geometry(r).points collect {(each.x * x_coeff) + x_offset,(each.y * y_coeff) + y_offset}); 
			create object with: (shape:g.contour + 0.1, my_pattern:self, score:int(-1 * score));
		}
	}
}

species object {
	pattern my_pattern;
	int score;
	aspect default {
		if score <= my_pattern.score_max {
			if (my_pattern = concerned_pattern) {
				draw (shape + 0.2) color: my_pattern.color;
			
			} else {
				draw shape color: my_pattern.color;
			
			}
			draw my_pattern.name + ": " + round(score) anchor: #center at: {location.x, location.y + shape.height / 1.3, 0.1} color: my_pattern.color font: font(30);
		}
		
	}
	
	user_command modify_score {
		concerned_pattern <- my_pattern;
		current_score_max <- my_pattern.score_max;
	}
	
	action del_pattern {
		concerned_pattern <- nil;
	 	ask object where (each.my_pattern = my_pattern) - self{
	 		do die;
	 	}
	 	
	 	bool is_ok <- delete_file(my_pattern.pattern_image_file);
	 	if not dead(my_pattern){ask my_pattern  {do die;}}
	 	do die;
	}
	user_command delete_pattern {
		do del_pattern;
	}
}

experiment CreatePatterns type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		display image_view type: opengl axes: false{
			  overlay position: { 5, 5 } size: { 480 #px, 100 #px } background: # black transparency: 0.3 border: #black rounded: true
            {
            	if  pattern_definition {
            		draw "Pattern definition" font: font(30) at: { 40#px,50#px } color: #white;
            	} else if crop_world {
            		draw "Crop the environment" font: font(30) at: { 40#px,  50#px } color: #white;
            	} else if concerned_pattern != nil {
            		draw "Modification of the max score of: " + concerned_pattern.name font: font(20) at: { 40#px,  50#px } color: #white;
            	}
	 
          	}
			image "../includes/real_map.jpg" refresh: false;
			
			species object ;
			event "p" action: mode_pattern;
			event "c" action: mode_crop_world;
			event "s" action: save_patterns;
			
			event mouse_down action: mouse_down_action;
			event mouse_move action: mouse_move_action;
			
			graphics "pattern points" {
				if mask_world_geom != nil {
					draw mask_world_geom  color: #black;
				}
				if (pattern_definition or crop_world )and mouse_loc != nil {
					draw circle(pattern_definition ? mouse_size_pattern : mouse_size_crop) at: mouse_loc + {0,0,0.1} color: pattern_definition ? color_mouse_pattern: color_mouse_crop;
					draw temp_geometry + 0.1 color: pattern_definition ? color_mouse_pattern: color_mouse_crop;
				}
				if first_pt != nil {
					draw circle(pattern_definition ? point_size_pattern : point_size_crop) at: first_pt  + {0,0,0.1} color:  pattern_definition ? color_pt_pattern: color_pt_crop;
				}
				if last_pt != nil {
					draw circle(pattern_definition ? point_size_pattern : point_size_crop) at: last_pt + {0,0,0.1} color:  pattern_definition ? color_pt_pattern: color_pt_crop;
				}
				/*if geom_pt != nil {
					draw geom_pt.contour + 0.1 color: pattern_definition ? color_pt_pattern: color_pt_crop;
				}*/
				
				
			} 
		}
	}
}
