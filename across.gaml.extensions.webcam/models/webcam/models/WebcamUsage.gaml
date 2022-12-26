/**
* Name: WebcamUsage
* Shows how to use a webcam to capture image
* Author: Patrick Taillandier & Baptiste Lesquoy
* Tags: Webcam, Image
*/

model WebcamUsage


global {   
	//define a new webcam - give the id of the webcam (0 - default, 1...)
	webcam cam <- webcam(0);  
	
	//image to display
	matrix img; 
	
	bool image_miror_horizontal <- false ;
	bool image_miror_vertical <- false ;
	
	pair<int,int> resolution <- 640::480 among:  [176::144,320::240,640::480].pairs ;
		
	init {
		//capture the image from the webcam and save it into a file - do not apply a miror operator to the image
		img <- cam_shot(cam, resolution,image_miror_horizontal, image_miror_vertical,"generated/webcamImage.jpg");	
	}
	
	reflex refresh {
		//capture the image - do apply a miror operator to the image
		img <- cam_shot(cam, resolution,image_miror_horizontal, image_miror_vertical );	
	}
	
}

experiment display_webcam autorun: true{
	//parameter "Resolution scaling" var: resolution_scaling;
	parameter "Allows to horizontally mirror the image" var: image_miror_horizontal;
	parameter "Allows to vertically mirror the image" var: image_miror_vertical;
	parameter "Possible resolutions" var: resolution ;
	output {
		display "Webcam image"  {
			//display the image from the webcam
			image matrix:img;
		}
	}
}