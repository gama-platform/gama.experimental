/**
* Name: NewModel
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model NewModel

global {
	image_file real_map_image_file <- image_file("../includes/real_map.jpg");
	image_file real_map_distortion_removed_image_file <- image_file("../includes/real_map_distorsion_removed.jpg");
	image_file real_map_distortion_removed_image_file1 <- image_file("../includes/real_map_distorsion_removed.png");

	string image_distorsion_removed_name <- "real_map_distorsion_removed.jpg";
	string image_distorsion_removed_name1 <- "real_map_distorsion_removed.png";
	string image_shapren_name <- "real_map_sharpened.jpg";
	string includes_folder <- "../includes";
	string pattern_folder <- "../includes/patterns";
	string result_folder <- "../includes/results";
	string prcimg_folder <- "../includes/prcImg";
	
	string path_to_global_image <- real_map_image_file.path;
	string path_to_map_image <- real_map_distortion_removed_image_file.path;
	string path_to_map_image1 <- real_map_distortion_removed_image_file1.path;
	
	
	
	int max_detection_objects <- 20;
	rgb color_pt_removedistorsion <- #red;
	float point_size_removedistorsion <- 0.25;
	rgb color_mouse_removedistorsion <- #cyan;
	float mouse_size_removedistorsion <- 0.5;
	
	rgb color_pt_pattern <- #green;
	float point_size_pattern <- 0.25;
	rgb color_mouse_pattern <- #yellow;
	float mouse_size_pattern <- 0.5;
	
	
	
	bool pattern_definition <- false;
	bool remove_distorsion <- false;
	point first_pt <- nil;
	point last_pt <- nil;
	geometry geom_pt <- nil;
	
	geometry temp_geometry <- nil;
	
	list<point> points_for_distorsion_removing;
	
	bool follow_mouse <- false;
	point mouse_loc <- nil;
	
	
	int init_score_max <- 50 min: 0 max: 400 ;
	int current_score_max <- 50 min: 0 max: 400 parameter: true on_change: update_score;
	
	float x_coeff ;
	float y_coeff ;
	
	geometry remove_distorsion_world_geom;
	
	int x_res;
	int y_res;
	pattern concerned_pattern <- nil;
	init {
		matrix mat <- matrix(real_map_image_file);
		x_res <- mat.columns;
		y_res <- mat.rows;
		x_coeff <- shape.width / x_res;
		y_coeff <- shape.height / y_res;
		
		
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
		concerned_pattern <- nil;
		remove_distorsion <- false;
		temp_geometry <- nil;
		points_for_distorsion_removing <- [];
		remove_distorsion_world_geom <- nil;
		
	}
	
	action mode_remove_distorsion {
		remove_distorsion <- not remove_distorsion;
		pattern_definition <- false;
		first_pt <- nil;
		last_pt <- nil;
		geom_pt <- nil;
		temp_geometry <- nil;
		points_for_distorsion_removing <- [];
		concerned_pattern <- nil;
		remove_distorsion_world_geom <- nil;
		if remove_distorsion {
			path_to_global_image <- real_map_image_file.path;
			x_coeff <- shape.width / x_res;
			y_coeff <- shape.height / y_res;
		}
		
	}
	
	action mouse_move_action {
		if pattern_definition {
			mouse_loc <- #user_location;
			if first_pt != nil and last_pt = nil {
				temp_geometry <- polygon([first_pt, {first_pt.x,mouse_loc.y },mouse_loc, {mouse_loc.x,first_pt.y }]).contour;
				
			}
			
		} else if remove_distorsion {
			mouse_loc <- #user_location;
			if length(points_for_distorsion_removing) = 1 {
				remove_distorsion_world_geom <- line(points_for_distorsion_removing + #user_location );
			} else {
				remove_distorsion_world_geom <- polygon(points_for_distorsion_removing + #user_location);
			}
		}
	}
	
	
	action mouse_down_action {
//		if remove_distorsion {
//			points_for_distorsion_removing << #user_location;
//			if length(points_for_distorsion_removing) = 4 {
//				string new_image_path <- remove_perspective(real_map_image_file.path replace (real_map_image_file.name,image_distorsion_removed_name),points_for_distorsion_removing,real_map_image_file.path, 1500,1500);
//					if new_image_path != nil {
//						ask object {
//							do die;
//						}
//						ask pattern {
//							do indentify_matching_objects;
//						}
//						matrix mat <- matrix(image_file(new_image_path));
//						path_to_global_image <-copy(new_image_path);
//						x_coeff <- world.shape.width / mat.columns;
//						y_coeff <- world.shape.height / mat.rows;
//						points_for_distorsion_removing <- [];
//						remove_distorsion_world_geom <- nil;
//					}
//			}else if pattern_definition {
//			if geom_pt != nil {
//				geom_pt <- nil;
//				first_pt <- nil;
//				last_pt <- nil;
//			}
//			if first_pt = nil and (#user_location overlaps world ){
//				first_pt <- #user_location;
//			}
//			else if  (#user_location overlaps world ){
//				last_pt <- #user_location;
//				geom_pt <- polygon([first_pt, {first_pt.x,last_pt.y },last_pt, {last_pt.x,first_pt.y }]);
//				map result <- user_input_dialog("New Pattern", [enter("Pattern name", string, ""), enter("Pattern color", rgb, rnd_color(255))]);
//				string name_pattern <- result["Pattern name"];
//				rgb color_pattern <- result["Pattern color"];
//				if name_pattern != "" {
//					string new_pattern_path <- crop_image(pattern_folder + "/" + name_pattern + ".png",geom_pt,path_to_global_image, world );
//					create pattern with: (pattern_image_file:new_pattern_path, score_max:init_score_max, name: name_pattern, color:color_pattern ) {
//						do indentify_matching_objects;
//					}
//				}
//				
//			}
//		} else {
//				if length(points_for_distorsion_removing) = 2 {
//					remove_distorsion_world_geom <- line(points_for_distorsion_removing);
//				} else {
//					remove_distorsion_world_geom <- polygon(points_for_distorsion_removing);
//				}
//			}
//	}
		
		
//		----------------------------------------------------------------------------------------------------
		if pattern_definition {
			if geom_pt != nil {
				geom_pt <- nil;
				first_pt <- nil;
				last_pt <- nil;
			}
			if first_pt = nil and (#user_location overlaps world ){
				first_pt <- #user_location;
			}
			else if  (#user_location overlaps world ){
				last_pt <- #user_location;
				geom_pt <- polygon([first_pt, {first_pt.x,last_pt.y },last_pt, {last_pt.x,first_pt.y }]);
				map result <- user_input_dialog("New Pattern", [enter("Pattern name", string, ""), enter("Pattern color", rgb, rnd_color(255))]);
				string name_pattern <- result["Pattern name"];
				rgb color_pattern <- result["Pattern color"];
				if name_pattern != "" {
					string new_pattern_path <- crop_image(pattern_folder + "/" + name_pattern + ".png",geom_pt,path_to_global_image, world );
					create pattern with: (pattern_image_file:new_pattern_path, score_max:init_score_max, name: name_pattern, color:color_pattern ) {
						do indentify_matching_objects;
					}
				}
				
			}
			
		} else if remove_distorsion {
			points_for_distorsion_removing << #user_location;
			if length(points_for_distorsion_removing) = 4 {
				string new_image_path <- remove_perspective(real_map_image_file.path replace (real_map_image_file.name,image_distorsion_removed_name),points_for_distorsion_removing,real_map_image_file.path, 1500,1500);
					if new_image_path != nil {
						ask object {
							do die;
						}
						ask pattern {
							do indentify_matching_objects;
						}
						matrix mat <- matrix(image_file(new_image_path));
						path_to_global_image <-copy(new_image_path);
						x_coeff <- world.shape.width / mat.columns;
						y_coeff <- world.shape.height / mat.rows;
						points_for_distorsion_removing <- [];
						remove_distorsion_world_geom <- nil;
					}
			} else {
				if length(points_for_distorsion_removing) = 2 {
					remove_distorsion_world_geom <- line(points_for_distorsion_removing);
				} else {
					remove_distorsion_world_geom <- polygon(points_for_distorsion_removing);
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
//		map res <- image_matching(path_to_map_image, 8, 8);
		list<Lego> legoList <- code_detect(path_to_map_image1, 8, 8);
		write(legoList);
		write(length(legoList));
//		string test <- code_detect(path_to_map_image1, 8, 8);
		loop r over: res.keys {
			float score <- float(map(res[r])["SCORE"]);
			geometry g <- polygon(geometry(r).points collect {(each.x * x_coeff),(each.y * y_coeff)}); 
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
            	} else if remove_distorsion {
            		draw "Crop the environment" font: font(30) at: { 40#px,  50#px } color: #white;
            	} else if concerned_pattern != nil {
            		draw "Modification of the max score of: " + concerned_pattern.name font: font(20) at: { 40#px,  50#px } color: #white;
            	}
	 
          	}
          	graphics "image" {
          		draw image_file(path_to_global_image);
          	}
          	
			species object ;
			event "p" action: mode_pattern;
			event "c" action: mode_remove_distorsion;
			event "s" action: save_patterns;
//			event "r" action: sharpen_image;
			
			event mouse_down action: mouse_down_action;
			event mouse_move action: mouse_move_action;
			
			graphics "remove deformation" {
				loop pt over: points_for_distorsion_removing {
					draw circle(point_size_removedistorsion) at: pt color: color_pt_removedistorsion;
				}
			}
			
			graphics "remove deformation geom" transparency: 0.5 {
				if remove_distorsion_world_geom != nil {
					draw  remove_distorsion_world_geom color: color_pt_removedistorsion; 
				}
			}
			graphics "mouse loc points" {
				if (pattern_definition or remove_distorsion)and mouse_loc != nil {
					draw circle(pattern_definition ? mouse_size_pattern : mouse_size_removedistorsion) at: mouse_loc + {0,0,0.1} color: pattern_definition ? color_mouse_pattern: color_mouse_removedistorsion;
				}
				
			}
			graphics "pattern points" {
				if first_pt != nil {
					draw circle(point_size_pattern) at: first_pt  + {0,0,0.1} color:   color_pt_pattern;
				}
				if last_pt != nil {
					draw circle(point_size_pattern) at: last_pt + {0,0,0.1} color:  color_pt_pattern;
				}
			}
			graphics "pattern points geom" transparency: 0.5  {
				if geom_pt != nil {
					draw geom_pt color: color_pt_pattern;
				}
				
				
			} 
		}
	}
}
