model pattern_model 

global {
	float x_coeff <- 0.06666666666666667;
	float y_coeff <- 0.06666666666666667;
	int max_detection_objects <- 20;

	action initialize {
		csv_file pattern_file <- csv_file("../includes/patterns/patterns.csv",",",string,true);
		create pattern from:pattern_file with:(name:get("name"), color:rgb(get("color")), score_max:int(get("score_max")),pattern_image_file:get("pattern_image_file"));
	}

	action indentify_all_matching_objects(string path_to_global_image) {
		ask pattern {
			do indentify_matching_objects(path_to_global_image);
		}
	}
}
species object{
	pattern my_pattern;
	int score;
}
species pattern{
	rgb color;
	int score_max;
	string pattern_image_file;

	action indentify_matching_objects(string path_to_global_image) {
		map res <- image_matching(path_to_global_image,[pattern_image_file::name], max_detection_objects);
		loop r over: res.keys {
			float score <- float(map(res[r])["SCORE"]);
			if score <= score_max {
				geometry g <- polygon(geometry(r).points collect {(each.x * x_coeff) + x_offset,(each.y * y_coeff) + y_offset}); 
				create object with: (shape:g.contour + 0.1, my_pattern:self, score:int(-1 * score));
			}
		}
	}
}
