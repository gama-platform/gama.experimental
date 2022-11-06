/**
* Name: analyseImage
* Example of use of the image analysis plugin to detect and analysis Lego block from an image 
* Author: Patrick Taillandier
* Tags: 
*/

model analyseImage

global {
	list<rgb> colors <- [#red, #blue, #magenta, #yellow, #pink, #orange, #green, #cyan, #violet, #gray, #olivedrab, #lightsteelblue, #lightcoral];
	image_file image_file_test <- image_file("../includes/WIN_20221102_11_17_05_Pro.jpg");
	geometry shape <- rectangle(1920, 1080);
	
	
	//tolerance for the comparison of color (white and black)
	float tolerance_BW <- 1.2 min: 1.0 max: 2.0 step: 0.1 parameter: true;
	
	//allow to increase the constrast of the image
	float coeff_constrast <- 1.5 min: 1.0 max:3.0 step: 0.1 parameter: true;
	
	//define the low threshold for the detection of block
	float low_threhold_block_detection <- 0.1 min: 0.0 max:0.5 step: 0.1 parameter: true;
	
	//define the high threshold for the detection of block
	float high_threhold_block_detection <- 0.3 min: 0.1 max:1.0 step: 0.1 parameter: true;
	
	//possibility to save all the images produced for debugging puropose
	bool save_image <- false parameter: true;
	
	//points used for the distorsion computation
	list<point> distorsion_points <- [{332.5892028808594,46.62588882446289,0.0},{1359.1029052734375,33.372005462646484,0.0},{1348.7962646484375,1046.6259765625,0.0},{356.15557861328125,1048.099609375,0.0}];
	
	//points used to define the typical size of a block to analysis
	list<point> bounds_points <- [{879.5982666015625,455.6495361328125,0.0},{945.5914306640625,519.5516967773438,0.0}];
	
	//points used to compute the typical level of intensity of white blocks
	list<point> whitesubblock_points <-[{915.5494995117188,457.8449401855469,0.0},{943.7417602539062,485.8640441894531,0.0}];
	
	//points used to compute the typical level of intensity of black blocks
	list<point> blacksubblock_points <- [{358.23541259765625,757.6268920898438,0.0},{387.1962890625,785.8941040039062,0.0}];
	
	string current_mode <- "";
	list<pattern> patterns; 
	bool define_bounds_points <- false;
	bool define_distorsion_points <- false;
	bool define_whitesubblock_points <- false;
	bool define_blacksubblock_points <- false;
	
	map<string,list<point>> blocks_detected;
	
	init {
		//patterns that we want to detect with their id
		patterns << create_pattern("1", 0,1,0,0);
		patterns << create_pattern("2", 1,0,1,1);
		patterns << create_pattern("3", 0,1,1,1);
		patterns << create_pattern("4", 0,1,1,0);
		patterns << create_pattern("5", 1,0,0,0);
		patterns << create_pattern("6", 1,1,0,1);
		patterns << create_pattern("7", 1,1,1,0);
		patterns << create_pattern("8", 1,1,0,0);
		patterns << create_pattern("9", 0,0,0,1);
		patterns << create_pattern("10", 1,0,0,1);
		patterns << create_pattern("11", 0,1,0,1);
		patterns << create_pattern("12", 1,0,1,0);
		patterns << create_pattern("13", 0,0,1,0);
		
		point pt0 <- distorsion_points[0];
		point pt1 <- distorsion_points[2];
		
		float w <- pt1.x - pt0.x;
		float h <- pt1.y - pt0.y;
		
		//read from the file the expected results for validation purpose
		matrix data <- matrix(csv_file("../includes/data.csv", " ", false));
		loop i from: 0 to: data.columns - 1 {
			loop j from: 0 to: data.rows -1 {
				point loc <- {pt0.x + (i + 0.5)/(data.columns) * w,  pt0.y + (j + 0.5)/(data.rows) * h};
				create cell with: (type: string(data[i,j]), shape: rectangle(0.7 *w/(data.columns -1) ,0.7 * h/(data.rows -1)) at_location loc) {
					int t <- int(float(type)) - 1;
					color <- colors[t];
				}
			}
		}
	}
	
	//create a new pattern from a 2x2 matrix
	pattern create_pattern(string id, int v00,int v01,int v10,int v11) {
		matrix m <- {2,2} matrix_with 0;
		m[0,0] <- v00;m[0,1] <- v01;m[1,0] <- v10;m[1,1] <- v11; 
		pattern p  <- pattern_with(id, {2,2}, 0);
		p <- p with_matrix m;
		return p;
	}
	
	action define_distorsions_points {
		define_distorsion_points <- not define_distorsion_points;
		define_whitesubblock_points <- false;
		define_blacksubblock_points <- false;
		define_bounds_points <- false;
		distorsion_points <- [];
		whitesubblock_points <- [];
		blacksubblock_points <- [];
		define_bounds_points <- [];
		current_mode <- "Definition of distorsion points";
	}
	
	action define_white_subblock {
		define_distorsion_points <- false;
		define_whitesubblock_points <- not define_whitesubblock_points;
		define_blacksubblock_points <- false;
		define_bounds_points <- false;
		distorsion_points <- [];
		whitesubblock_points <- [];
		blacksubblock_points <- [];
		define_bounds_points <- [];
		current_mode <- "Definition of the white block";
	}
	
	action define_black_subblock {
		define_distorsion_points <- false;
		define_whitesubblock_points <- false;
		define_blacksubblock_points <- not define_blacksubblock_points;
		define_bounds_points <- false;
		distorsion_points <- [];
		whitesubblock_points <- [];
		blacksubblock_points <- [];
		define_bounds_points <- [];
		current_mode <- "Definition of the black block";
	}
	
	action define_bounds {
		define_distorsion_points <- false;
		define_whitesubblock_points <- false;
		define_blacksubblock_points <- false;
		define_bounds_points <- not define_bounds_points;
		distorsion_points <- [];
		whitesubblock_points <- [];
		blacksubblock_points <- [];
		bounds_points <- [];
		current_mode <- "Definition of the block bound";
	}
	
	action mouse_click {
			
		if define_distorsion_points {
			if (length(distorsion_points) < 4) {
				distorsion_points << #user_location;
			}
		} else if define_whitesubblock_points {
			if (length(whitesubblock_points) < 2) {
				whitesubblock_points << #user_location;
				if (length(whitesubblock_points) = 2) {
					write sample(whitesubblock_points);
				} 
			}
			
		} else if define_blacksubblock_points {
			if (length(blacksubblock_points) < 2) {
				blacksubblock_points << #user_location;
				if (length(blacksubblock_points) = 2) {
					write sample(blacksubblock_points);
				} 
			} 
			
		} else if define_bounds_points {
			if (length(bounds_points) < 2) {
				bounds_points << #user_location;
				if (length(bounds_points) = 2) {
					write sample(bounds_points);
				} 
			} 
			
		}
	}
	
	action define_code {
		
		current_mode <- "Detection of the codes of the blocks";
		write "Detection of the codes of the blocks";
		blocks_detected <- [];
		
		//building the geometries (black block, white block, block bounds) from the points defined
		geometry black_subblock <- length(blacksubblock_points) = 2 ? polygon([blacksubblock_points[0],{blacksubblock_points[1].x, blacksubblock_points[0].y},blacksubblock_points[1],{blacksubblock_points[0].x, blacksubblock_points[1].y} ]): nil;
		geometry white_subblock <- length(whitesubblock_points) = 2 ? polygon([whitesubblock_points[0],{whitesubblock_points[1].x, whitesubblock_points[0].y},whitesubblock_points[1],{whitesubblock_points[0].x, whitesubblock_points[1].y} ]): nil;
		geometry bounds_g <- length(bounds_points) = 2 ? polygon([bounds_points[0],{bounds_points[1].x, bounds_points[0].y},bounds_points[1],{bounds_points[0].x, bounds_points[1].y} ]): nil;
		
		//detecting the code
		list<block> blocks <- detect_blocks(
			image_file_test.path, //image from which detecting the block code
			patterns, //list of patterns to detect
			distorsion_points, //list of 4 detection points (top-left, top-right, bottom-right, bottom-left)
			  8,8, //size of the grid (columns, rows)
			  black_subblock, //black subblock for the computation of the black intensity 
			  white_subblock,//white subblock for the computation of the white intensity
			  bounds_g, //example of block for computation of the expected size of blocks
			 tolerance_BW, //optional: tolerance for black and white color: default: 1.2
			low_threhold_block_detection,//optional: low threshold for block detection, default: 0.1
			 high_threhold_block_detection, //optional: high threshold for block detection, default: 0.3
			 coeff_constrast, //optional: coefficient to increase the contrast of the imahe, default: 1.5 
			 save_image //optional: save the image produced (just for debugging purpose)
		);
		
		
		//group the blocks per id for visualization purpose
		loop b over: blocks {
			if  b.type != nil {
				if not(b.type.id in blocks_detected.keys) {
					blocks_detected[b.type.id] <- [b.shape.location];
				} else {
					blocks_detected[b.type.id] << b.shape.location;
				}
				
			} 
		}
		
		//validation of the code - if there is an error (pattern not detected, or wrong pattern detected), display a square around the falty blocks
		ask cell {
			error <- true;
			loop bb over: blocks {
				if bb.shape overlaps self {
					error <- bb.type = nil or (int(float(type)) != int(bb.type.id)) ;
					break;
				}
			} 
		}
		write "Number of errors: " + (cell count each.error);
	}
}

species cell 
{
	string type;
	rgb color;
	bool error <- false;
	
	aspect default {
		if error {
			draw shape.contour width: 5 color: color;
		}
	}
}



experiment analyseImage type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		display image_display {
			overlay position: { 5, 5 } size: { 800 #px, 180 #px } background: # black transparency: 0.4 border: #black rounded: true
            {
            	draw "current action: " + current_mode at: { 50#px,  30#px } color: # white font: font("Helvetica", 30, #bold);
            	draw "'d': detecting codes of blocks" at: { 50#px,  60#px } color: # white font: font("Helvetica", 20, #bold);
            	
            	draw "'p': define the distorsion points" at: { 50#px,  80#px } color: # white font: font("Helvetica", 20, #bold);
            	draw "'b': define the black block" at: { 50#px,  100#px } color: # white font: font("Helvetica", 20, #bold);
            	draw "'w': define the white block" at: { 50#px,  120#px } color: # white font: font("Helvetica", 20, #bold);
            	draw "'g': define the bound of block" at: { 50#px,  140#px } color: # white font: font("Helvetica", 20, #bold);
            	
            }
			image image_file_test;
			species cell position: {0,0,0.01};
			event "p" action: define_distorsions_points;
			event "d" action: define_code;
			event "b" action: define_black_subblock;
			event "w" action: define_white_subblock;
			event "g" action: define_bounds;
			
			event #mouse_down action: mouse_click;
			graphics "distorsion" {
				loop pt over: distorsion_points {
					draw circle(10) color: #red at: pt;
				}
			}
			graphics "blackSubBlock" {
				loop pt over: blacksubblock_points {
					draw circle(2) color: #magenta at: pt;
				}
			}
			graphics "whiteSubBlock" {
				loop pt over: whitesubblock_points {
					draw circle(2) color: #cyan at: pt;
				}
			}
			graphics "bounds points" {
				loop pt over: bounds_points {
					draw square(2) color: #gold at: pt;
				}
			}
			graphics "blocks detected" {
				loop id over: blocks_detected.keys {
					rgb col <- colors[int(id) -1];
					loop pt over: blocks_detected[id] {
						draw circle(20) color: col at: pt;
					}
				}
			}
		}
	}
}
