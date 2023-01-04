/**
* Name: testwebcam
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model testwebcam

global {
	webcam webcam1 <- webcam(0);
	
	//image to display
	matrix img; 
	
	int image_width <- 640;
	int image_height <- 480;
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
		string result_qr_decode <- string(decodeQR(webcam1,image_width::image_height, false));
		write sample(result_qr_decode);
		img <- cam_shot(webcam1, image_width::image_height, false);
		
	}
} 


experiment qrcode_usage type: gui {
	output {
		display image_from_webcam {
			image matrix:img;
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
