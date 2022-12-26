/**
* Name: analyseWebcamImage
* Example of use of the image analysis plugin to detect and analysis Lego block from webcam images 
* Author: Patrick Taillandier
* Tags: 
*/


model analyseWebcamImage

import "analyseImage.gaml"

global {
	int image_width <- 640;
	int image_height <- 480;
	bool image_miror_horizontal <- true ;
	bool image_miror_vertical <- false ;
	
	bool save_image <- true;
	list<point> distorsion_points <- [{384.54160789844855,64.96240601503759,0.0},{1402.7644569816644,32.48120300751879,0.0},{1408.1805359661496,1039.3984962406014,0.0},{430.5782792665726,1055.639097744361,0.0}];
	list<point> bounds_points <- [{668.4972170686457,466.4175824175824,0.0},{736.1781076066791,525.7582417582418,0.0}];
	list<point> blacksubblock_points <-[{537.8849721706865,469.978021978022,0.0},{569.9443413729128,497.2747252747253,0.0}];
	list<point> whitesubblock_points <-[{699.3692022263451,331.1208791208791,0.0},{731.4285714285714,360.7912087912088,0.0}];
	matrix img;
	webcam webcam1 <- webcam(0);
	
	action create_agents {
		img <- cam_shot(webcam1, image_width::image_height );
		create webcam_analyser {
			do run_thread interval: 2#s;
		}
	} 
	
	reflex capture_webcam {
		img <- cam_shot(webcam1,image_width::image_height);
		ask webcam_analyser {
			do analyse_image;
		}
	}
}


species webcam_analyser skills: [thread] {
	
	action analyse_image {
		blocks_detected <- [];
		
		//building the geometries (black block, white block, block bounds) from the points defined
		geometry black_subblock <- length(blacksubblock_points) = 2 ? polygon([blacksubblock_points[0],{blacksubblock_points[1].x, blacksubblock_points[0].y},blacksubblock_points[1],{blacksubblock_points[0].x, blacksubblock_points[1].y} ]): nil;
		geometry white_subblock <- length(whitesubblock_points) = 2 ? polygon([whitesubblock_points[0],{whitesubblock_points[1].x, whitesubblock_points[0].y},whitesubblock_points[1],{whitesubblock_points[0].x, whitesubblock_points[1].y} ]): nil;
		geometry bounds_g <- length(bounds_points) = 2 ? polygon([bounds_points[0],{bounds_points[1].x, bounds_points[0].y},bounds_points[1],{bounds_points[0].x, bounds_points[1].y} ]): nil;
		
		//detecting the code
	 list<block> blocks <- detect_blocks(
			webcam1, //webcam used for the image analysis
			image_width::image_height, //webcam image resolution
			image_miror_horizontal, image_miror_vertical,
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
			 save_image, //optional: save the image produced (just for debugging purpose)
			improve_image //optional: apply filter on the image, default: false
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
		write "blocks: " + blocks collect each.type;	
	} 
	//the action run in the thread 
	action thread_action {
		//do analyse_image;
	}	
}


experiment test {
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
         	 image matrix:img;
			species cell position: {0,0,0.01};
			event "p" action: define_distorsions_points;
			event "d" action: define_code;
			event "b" action: define_black_subblock;
			event "w" action: define_white_subblock;
			event "g" action: define_bounds;
			event #mouse_move action: define_mouse_loc;
			event #mouse_down action: mouse_click;
			graphics "mouse_loc" {
				draw circle(5) at: mouse_location;
			}
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