/**
* Name: QRencode
* Based on the internal skeleton template. 
* Author: admin_ptaillandie
* Tags: 
*/

model QRencode

global {
	init {
		matrix<bool> qr <- matrix<bool>(encodeQR("Hello", 25, 25));
		ask cell_image {
			color <- qr[grid_x,grid_y] ? #black : #white; 
		}
	}
}

grid cell_image width: 25 height: 25;

experiment QRencode type: gui {
	output {
		display qr_code {
			grid cell_image;
		}
	}
}
