/**
* Name: ContourDetection
* Vectorization of elements from a binary image based on image analysis operators
* Author: Patrick Taillandier
* Tags: image processing, binary image, contours
*/

model ImageVectorization

global {
	
	image_file img_file <- image_file("../includes/hydro.png");
	matrix img <- img_file.contents; 
	matrix img1 <- detect_contours(img); 
	list<geometry> objects ;
	float min_perimeter <- 20.0;
	 
	init {
		loop l over: vectorize(img) {
			geometry ll <- geometry(l);
			geometry g <- ll;
			if (l overlaps world.shape.contour) {
				g <- polygon((ll - (world.shape.contour + (world.shape.width / 100.0))).points);
				if g.area > polygon(ll.points).area {
					g <-  polygon(ll.points);
				}	
			} 
			objects <- objects + g.geometries ;
		}
		objects <- objects where (each.perimeter > min_perimeter);
	}

}

experiment ImageVectorization type: gui {
	output {
		display image type: opengl {
			
			image image(img) refresh: false;
			graphics "objects"{
				loop l over: objects {
					draw l color: rnd_color(255) depth: 1;
				}
			}		
		}
		display image1 type: opengl {
			image image(img) refresh: false;	
		}
	}
}
