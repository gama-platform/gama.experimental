/**
* Name: analyseWebcamImage
* Example of use of the image analysis plugin to detect and analysis Lego block from webcam images 
* Author: Patrick Taillandier
* Tags: 
*/


model analyseWebcamImage

import "analyseImage.gaml"

global {
	
	action create_agents {
		create webcam_analyser {
			do run_thread interval: 2#s;
		}
	}
}


species webcam_analyser skills: [thread] {
	webcam webcam1 <- webcam(1);
	bool show_camera <- true;
	int image_width <- 640;
	int image_height <- 480;
	
	//the action run in the thread 
	action thread_action {
		blocks_detected <- [];
		
		//building the geometries (black block, white block, block bounds) from the points defined
		geometry black_subblock <- length(blacksubblock_points) = 2 ? polygon([blacksubblock_points[0],{blacksubblock_points[1].x, blacksubblock_points[0].y},blacksubblock_points[1],{blacksubblock_points[0].x, blacksubblock_points[1].y} ]): nil;
		geometry white_subblock <- length(whitesubblock_points) = 2 ? polygon([whitesubblock_points[0],{whitesubblock_points[1].x, whitesubblock_points[0].y},whitesubblock_points[1],{whitesubblock_points[0].x, whitesubblock_points[1].y} ]): nil;
		geometry bounds_g <- length(bounds_points) = 2 ? polygon([bounds_points[0],{bounds_points[1].x, bounds_points[0].y},bounds_points[1],{bounds_points[0].x, bounds_points[1].y} ]): nil;
		
		//detecting the code
		list<block> blocks <- detect_blocks(
			webcam1, //webcam used for the image analysis
			image_width, image_height, //webcam image resolution
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
	}	
}