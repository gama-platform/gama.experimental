/***
* Name: CubeCollector
* Author: youcef sklab

* Tags: Tag1, Tag2, TagN
***/
model CubeCollector

global skills: [network] {
	bool isNotifiyed <- false; // if true, a notification is received
	bool isCenter <- false; //  if true, go back to the scene center
	bool isNewPosition <- false; // if true, move the ball to a new position
	bool isNewColor <- false; // if true, set a new color for the ball
	int counter <- 0;
	int x <- 0;
	int y <- 0;
	int z <- 0;
	int colorIndex <- 0;
	int formIndex <- 0;
	int speed <- 10; // How speedy the ball's movements will be.
	int obstacleCounter <- 0;
	list colorlist <- ["black", "red", "blue", "white", "yellow"];
	list formList <- ["Capsule", "Cube", "Cylinder", "Sphere"];

	init {
		create GamaAgent number: 1 {
			color <- rnd_color(255); 
			shape <- sphere(4);
			
			
			// connect to the Mqtt server
			do connectMqttClient(); 
			write "connected";
			
			// subscribe to get notifiyed by the object: Player, when its field: count of type: field,  is eaqual (operator ==) to 4 
			do unityNotificationSubscribe notificationId: "Notification_01" objectName: "Player" fieldType: "field" fieldName: "count" fieldValue: "4" fieldOperator: "==";
			
			// subscribe to the topic: notification, in order to receive notifications 
			do subscribe_To_Topic topic: "notification";
			
		}

	}

}

species GamaAgent skills: [unity] {

	aspect base {
		draw shape color: color;
	}

	reflex updatePosition when: cycle mod 4 = 0
	{
		isNewPosition <- true; 
		isNewColor <- true;
		isCenter <- false;
		x <- rnd(-8, 8);
		z <- rnd(-8, 8);
		colorIndex <- rnd(0, 4);
		formIndex <- rnd(0, 3);
		speed <- rnd(10, 150);
	}
	
	reflex updateCenter when: cycle mod 9 = 0
	{
		isCenter <- true;
		isNewPosition <- false;
		counter <- cycle;

	}

	reflex resetPositionToCenter when: isCenter 
	{
		
		// Reset the Ball to the center scene center (not a move)
		map<string, string> pos <- map<string, string>(["x"::0, "y"::0, "z"::0]);
		//do setUnityPosition objectName: "TestObject" position: pos;
		do setUnityPosition objectName: "Player" position: {0,0,0};
		isCenter <- false;
		write "Comme back to center! x=" + 0 + " y=" + 0 + " z=" + 0;
	}

	reflex moveToNewPosition when: isNewPosition 
	{
		// Move the ball to the new position (not a position reset). The movement speed is specified too.
		map<string, string> pos <- map<string, string>(["x"::x, "y"::y, "z"::z]);
		//do unityMove objectName: "TestObject" position: pos speed:speed;
		do unityMove objectName: "Player" position: pos speed: speed;
		isNewPosition <- false;
		write "Move to new position!  x=" + x + " y=" + y + " z=" + z;

		// Create new GameObject:
		if (obstacleCounter < 4 and cycle > 10) {
			obstacleCounter <- obstacleCounter + 1;
			map<string, string> posi <- map<string, string>(["x"::x, "y"::1, "z"::z]);
			do newUnityObject objectName: "Test_" + obstacleCounter type: (formList[formIndex]) color: (colorlist[colorIndex]) position: posi;
			write " Create New Object";
		}
	}

	reflex colorTopic when: isNewColor 
	{
		isNewColor <- false;
		string colorEl <- colorlist[colorIndex];
		//Change the Ball's color to red
		do setUnityColor objectName: "Player" color: colorEl;
		write "message color topic sent!";
	}


	// Check if a notification is received
	reflex checkNotification when: !isNotifiyed {
		isNotifiyed <- isNotificationTrue("Notification_01");
	}


	reflex endGame when: isNotifiyed {
		do destroyUnityObject objectName: "Player";
		write "Game Over  --------------- The end";
		do die;
	}

}

experiment RunGame type: gui {
	map<string, point>
	anchors <- ["center"::#center, "top_left"::#top_left, "left_center"::#left_center, "bottom_left"::#bottom_left, "bottom_center"::#bottom_center, "bottom_right"::#bottom_right, "right_center"::#right_center, "top_right"::#top_right, "top_center"::#top_center];
	output {
		display view type: opengl {
			species GamaAgent aspect: base;
			graphics Send_Receive {
				draw world.shape empty: true color: #black;
				draw circle(0.5) at: {50, 5} color: #red;
				draw "test" at: {52, 5} anchor: {0.8, 0.8} color: #black font: font("Helvetica", 13, #bold);
				draw circle(0.5) at: {50, 10} color: #red;
				draw "test-text" at: {52, 10} anchor: {0.8, 0.8} color: #blue font: font("Helvetica", 13, #bold);
			}

		}

	}

}








