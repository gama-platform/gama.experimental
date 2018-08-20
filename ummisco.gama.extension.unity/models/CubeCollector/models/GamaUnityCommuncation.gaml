/*** 
* Name: GamaUnityTest
* Author: sklab
* Description: 
* Tags: Tag1, Tag2, TagN
***/
model GamaUnityCommuncation
 

global skills: [network]
{
	string sentMsg <- "";
	string receivedMsg <- "";
	init
	{
		create GamaAgent number: 1
		{
			color <- rnd_color(255);
			shape <- sphere(4);
			do connect(to: "localhost", with_name: "Gama", port: 1883);
		}

	}

}
 
species GamaAgent skills: [moving, network]
{
	rgb color;
	reflex update
	{
		do wander;
	}

	aspect base
	{
		draw shape color: color;
	}

	reflex sendMsg when: cycle mod 15 = 3
	{
		write "send a message to Unity ";
		do send to: "Unity" contents: " This message is sent from Gama to Unity ";
		sentMsg <- "Sent Message is: " + " This message is sent from Gama to Unity ";
	}

	reflex getReceivedMsg when: has_more_message()
	{
		write "get received message from Unity";
		message mess <- fetch_message();
		write name + " fecth this message: " + mess.contents;
		receivedMsg <- "Received Message is: " + mess.contents;
	}

}

experiment Send_Receive type: gui
{
	font my_font <- font("Helvetica", 14, # bold);
	map<string, point> anchors <- ["center"::# center, "top_left"::# top_left, "left_center"::# left_center, "bottom_left"::# bottom_left, "bottom_center"::#
	bottom_center, "bottom_right"::# bottom_right, "right_center"::# right_center, "top_right"::# top_right, "top_center"::# top_center];
	output
	{
		display view type: opengl
		{
			species GamaAgent aspect: base;
			graphics Send_Receive
			{
				draw world.shape empty: true color: # black;
				draw circle(0.5) at: { 50, 5 } color: # red;
				draw sentMsg at: { 52, 5 } anchor: { 0.8, 0.8 } color: # black font: font("Helvetica", 13, # bold);
				draw circle(0.5) at: { 50, 10 } color: # red;
				draw receivedMsg at: { 52, 10 } anchor: { 0.8, 0.8 } color: # blue font: font("Helvetica", 13, # bold);
			}

		}

	}

}


