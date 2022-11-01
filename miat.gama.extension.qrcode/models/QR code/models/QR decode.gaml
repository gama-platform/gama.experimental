/**
* Name: testwebcam
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model testwebcam

global {
	webcam webcam1 <- webcam(1);
	bool show_camera <- true;
	int image_width <- 320;
	int image_height <- 240;
	list<geometry> geoms;

	image_file image_to_decode_multi <- image_file("../includes/multiQRcode.png");
	image_file image_to_decode_simple <- image_file("../includes/26810014.jpg");
	init {
		matrix mat <- matrix(image_to_decode_multi);
		//Decode a QR code from an image
		write sample(decodeQRFile(image_to_decode_simple.path));
		map result <- (decodeMultiQRFile(image_to_decode_multi.path));
		write sample(result.keys);
		float x_coeff <- shape.width / mat.columns;
		float y_coeff <- shape.height / mat.rows;
		loop r over: result {
			geoms << polygon(geometry(r).points collect {each.x * x_coeff,each.y * y_coeff});
		}
	}
	reflex capture_webcam {
		//capture image from webcam
		string result_qr_decode <- string(decodeQR(image_width, image_height,webcam1));
		write sample(result_qr_decode);
		
		
		if show_camera {
			matrix mat <- field(cam_shot("image_test", image_width, image_height, webcam1));
			ask cell_image {
				color <- rgb(mat[grid_x,grid_y]);		
			}
		}
	}
}

grid cell_image width: image_width height: image_height;

experiment qrcode_usage type: gui {
	output {
		display image_from_webcam {
			grid cell_image ;
		}
		
		display image_decoded_simple {
			image image_to_decode_simple ;
		}
		
		display image_decoded_multi {
			image image_to_decode_multi ;
			graphics "code "{
				loop g over: geoms {
					draw g.contour + 0.5 color: #magenta;
				}
			}
		}
	}
}
