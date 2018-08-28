/***
* Name: TwoPlayersWithSceneBuilding
* Author: youcef sklab
* Description: This model is like the CubeCollector but with two players. 
*  The aim here is to show that it is possible to set up the unity scene.
* Tags: Tag1, Tag2, TagN
***/
model TwoPlayersWithSceneBuilding

global skills: [unity] {
	bool isGameOver <- false; 	
	list colorlist <- ["black", "red", "blue", "white", "yellow"];
	list formList <- ["Capsule", "Cube", "Cylinder", "Sphere"];
	list playerList <- ["Player", "Player01"];
	

	init {
		create GamaAgent number: 2 {	}
		
		do connectMqttClient(); 
		write "Agent "+self.name+ " connected";

		do callUnityMonoAction objectName: "Player"   actionName:"setWinText"  attribute:" --- Game ON --- ";  
		do callUnityMonoAction objectName: "Player01" actionName:"setWinText"  attribute:" --- Game ON --- ";  
		
		//do setUnityFields objectName: "Player" attributes: attributesList;	
		
		// Create the North wall
		map<string, string> posi <- map<string, string>(["x"::0, "y"::1, "z"::12]);
		do newUnityObject objectName: "NorthWall" type:"Cube" color:"black" position: posi;
		// Set localScale of the NorthWall
		do setUnityProperty objectName: "NorthWall" propertyName:"localScale" propertyValue:"(25.0,2.0,0.3)";
		//do setUnityProperty objectName: "NorthWall" propertyName:"isTrigger" propertyValue:"true";
		//Disable collisions with the wall
		do setUnityProperty objectName: "NorthWall" propertyName:"detectCollisions" propertyValue:"false";
	 	
	 	
	 	
		// Create the South Wall
		posi <- map<string, string>(["x"::0, "y"::1, "z"::-12]);
		do newUnityObject objectName: "SouthWall" type:"Cube" color:"blue" position: posi;
		// Set localScale of the SouthWall
		do setUnityProperty objectName: "SouthWall" propertyName:"localScale" propertyValue:"(25.0,2.0,0.3)";
		do setUnityProperty objectName: "SouthWall" propertyName:"detectCollisions" propertyValue:"false";
	
	 	
		// Create the East Wall
		posi <- map<string, string>(["x"::12, "y"::1, "z"::0]);
		do newUnityObject objectName: "EastWall" type:"Cube" color:"red" position: posi;
		// Set localScale of the EastWall
		do setUnityProperty objectName: "EastWall" propertyName:"localScale" propertyValue:"(24.0,2.0,0.3)";
		// Set localEulerAngles of the EastWall
		do setUnityProperty objectName: "EastWall" propertyName:"localEulerAngles" propertyValue:"(0,90,0)";
		do setUnityProperty objectName: "EastWall" propertyName:"detectCollisions" propertyValue:"false";
		
		
		// Create the West Wall
		posi <- map<string, string>(["x"::-12, "y"::1, "z"::0]);
		do newUnityObject objectName: "WestWall" type:"Cube" color:"yellow" position: posi;
		// Set localScale of the WestWall
		do setUnityProperty objectName: "WestWall" propertyName:"localScale" propertyValue:"(24.0,2.0,0.3)";
		// Set localEulerAngles of the WestWall
		do setUnityProperty objectName: "WestWall" propertyName:"localEulerAngles" propertyValue:"(0,90,0)";
		do setUnityProperty objectName: "WestWall" propertyName:"detectCollisions" propertyValue:"false";
		
		
		

	}
	
	reflex chekGameOver when: isGameOver {
			do disconnectMqttClient(); 
			do die;
		}
	
}

species GamaAgent skills: [unity] {
	
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
	
	
	
	string playerName <- (self.name = "GamaAgent0" ? "Player" : "Player01") ;
	int totalCount <-  4;
	int updateCycle <- (self.name = "GamaAgent0" ? 4 : 6) ;
	int updateCenter <- (self.name = "GamaAgent0" ? 6 : 9) ;
	init{
		color <- rnd_color(255); 
		shape <- sphere(4);
		
		// connect to the Mqtt server
		do connectMqttClient(); 
		write "Agent "+self.name+" connected to server";
			
		// subscribe (to unity engine) to get notifiyed by the object: Player, when its field: count of type: field,  is eaqual (operator ==) to 4 
		do unityNotificationSubscribe notificationId: self.name+"_Notification_01" objectName: playerName fieldType: "field" fieldName: "count" fieldValue: string(totalCount) fieldOperator: "==";
			
		// subscribe to the topic: notification, (Mqtt server) in order to receive notifications 
		do subscribe_To_Topic topic: "notification";
			
		write "Agent "+self.name+ " initialized with player "+playerName;
		
	}

	aspect base {
		draw shape color: color;
	}

	reflex updatePosition when: cycle mod updateCycle = 0
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
	
	reflex updateCenter when: cycle mod updateCenter = 0
	{
		isCenter <- true;
		isNewPosition <- false;
		counter <- cycle;

	}

	reflex resetPositionToCenter when: isCenter 
	{
		
		int newX <- rnd(0,2);
		int newZ <- rnd(0,2);
		
		// Reset the Ball to the center scene center (not a move)
		map<string, string> pos <- map<string, string>(["x"::newX, "y"::0, "z"::newZ]);
		//do setUnityPosition objectName: "TestObject" position: pos;
		do setUnityPosition objectName: playerName position: pos;
		isCenter <- false;
		write "Comme back to center! x=" + 0 + " y=" + 0 + " z=" + 0;
	}

	reflex moveToNewPosition when: isNewPosition 
	{
		
		// set text
		do callUnityMonoAction objectName: playerName actionName:"setWinText"  attribute:"My turn"+self.name;  
		
				
		// Move the ball to the new position (not a position reset). The movement speed is specified too.
		map<string, string> pos <- map<string, string>(["x"::x, "y"::y, "z"::z]);
		//do unityMove objectName: "TestObject" position: pos speed:speed;
		do unityMove objectName: playerName position: pos speed: speed;
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
		do setUnityColor objectName: playerName color: colorEl;
		write "message color topic sent!";
	}


	// Check if a notification is received
	reflex checkNotification when: !isNotifiyed {
		isNotifiyed <- isNotificationTrue(self.name+"_Notification_01");
	}


	reflex endGame when: isNotifiyed {
		do callUnityMonoAction objectName: playerName actionName:"setWinText"  attribute:"Game Over!";  
		//do destroyUnityObject objectName: playerName;
		write "Game Over  --------------- The end";
		isGameOver <- true;
	}
	
	
	reflex chekGameOver when: isGameOver {
			write "Game Over  --------------- The end";
			do disconnectMqttClient();
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








