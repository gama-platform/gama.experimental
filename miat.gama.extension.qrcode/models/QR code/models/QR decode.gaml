/**
* Name: testwebcam
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model testwebcam

global {
	int webcam <- 0;
	bool show_camera <- true;
	int image_width <- 320;
	int image_height <- 240;
	image_file image_to_decode <- image_file("../includes/26810014.jpg");
	init {
		//Decode a QR code from an image
		write sample(decodeQRFile(image_to_decode.path));
	}
	reflex capture_webcam {
		//capture image from webcam
		string result_qr_decode <- string(decodeQR(image_width, image_height,webcam));
		write sample(result_qr_decode);
		
		
		if show_camera {
			matrix mat <- field(cam_shot("image_test", image_width, image_height, webcam));
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
		
		display image_decoded {
			image image_to_decode ;
		}
	}
}
