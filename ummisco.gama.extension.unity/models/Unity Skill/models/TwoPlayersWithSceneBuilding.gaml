/***
* Name: TwoPlayersWithSceneBuilding
* Author: youcef sklab
* Description: This model is a game that shows how to make a Gama agent manage a Unity scene by a mean of MQTT protocol. 
*  It consists of two agents which control a ball in the scene by repeatedly sending messages to indicate their new position (randomly)
*  to move towards and colors. At the beginning, the main agent initializes the scene by creating walls, and setting up their position, 
* scale, orientation and color. Then, agents subscribe to the notification system (unity side) and indicate for how many collected cubes 
* they wish to be notified from unity engine. During the first simulation cycles, for each new position sent, an obstacle (object) 
* is created on the scene (at most 5 obstacles). So, the agents move their respective balls by specifying their new positions and trie to collect 
* the cubes. After having received a notification that the total collected cubes have reached the needed threshold, the simulation stops. 
* Tags: Tag1, Tag2, TagN
***/
model TwoPlayersWithSceneBuilding

global skills: [unity] {
	bool isGameOver <- false; 	
	list formList <- ["Capsule", "Cube", "Cylinder", "Sphere"];
	list playerList <- ["Player0", "Player1"];
	

	init {
		
		
		do connectMqttClient(); 
		write "Agent "+self.name+ " connected";

		do callUnityMonoAction objectName: "Player0"   actionName:"setWinText"  attribute:" --- Game ON --- ";  
		map<string,string> attributesList <- map<string, string>(["speed"::35]);
		do setUnityFields objectName: "Player0" attributes: attributesList;	
		
		// Create the North wall
		do newUnityObject objectName: "NorthWall" type:"Cube" color:rgb(193,0,0) position: {0,1,12};
		// Set localScale of the NorthWall
		do setUnityProperty objectName: "NorthWall" propertyName:"localScale" propertyValue:{25.0,2.0,0.3};
		//do setUnityProperty objectName: "NorthWall" propertyName:"isTrigger" propertyValue:"true";
		//Disable collisions with the wall
		do setUnityProperty objectName: "NorthWall" propertyName:"detectCollisions" propertyValue:false;
	 	
	 	
		// Create the South Wall
		do newUnityObject objectName: "SouthWall" type:"Cube" color:rgb(255,139,0)position: {0,1,-12};
		// Set localScale of the SouthWall
		do setUnityProperty objectName: "SouthWall" propertyName:"localScale" propertyValue:{25.0,2.0,0.3};
		do setUnityProperty objectName: "SouthWall" propertyName:"detectCollisions" propertyValue:false;
	
	 	
		// Create the East Wall
		do newUnityObject objectName: "EastWall" type:"Cube" color:rgb(255,1,144) position: {12,1,0};
		// Set localScale of the EastWall
		do setUnityProperty objectName: "EastWall" propertyName:"localScale" propertyValue:{25.0,2.0,0.3};
		// Set localEulerAngles of the EastWall
		do setUnityProperty objectName: "EastWall" propertyName:"localEulerAngles" propertyValue:{0,90,0};
		do setUnityProperty objectName: "EastWall" propertyName:"detectCollisions" propertyValue:false;
		
		
		// Create the West Wall
		do newUnityObject objectName: "WestWall" type:"Cube" color:rgb(109,0,242) position: {-12,1,0};
		// Set localScale of the WestWall
		do setUnityProperty objectName: "WestWall" propertyName:"localScale" propertyValue:{24.0,2.0,0.3};
		// Set localEulerAngles of the WestWall
		do setUnityProperty objectName: "WestWall" propertyName:"localEulerAngles" propertyValue:{0,90,0};
		do setUnityProperty objectName: "WestWall" propertyName:"detectCollisions" propertyValue:false;
		
		
	//loop i from: 1 to: 20 step: 1 {  	
	//loop i over: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21] {
	// 	string objectName <- "Cube"+i;
	// 	int x <- rnd(-11,11);
	// 	int z <- rnd(-11,11);
	// 	int red <- rnd(0,255);
	// 	int green <- rnd(0,255);
	// 	int blue <- rnd(0,255);
	// 	
    //  	do newUnityObject objectName: objectName type:"Cube" color:rgb(red,green,blue) position: {x,0.5,z};	
    //   	do setUnityProperty objectName: objectName propertyName:"isTrigger" propertyValue:true;		// setUnityProperty
	//	do setUnityFields objectName: objectName attributes: map<string, string>(["isRotate"::true]);
	//	do setUnityProperty objectName: objectName propertyName:"tag" propertyValue:"Pick Up";
    //}
    
    create Player number: 2 {	
    	
    }
    create cubeAgent   number: 20 {	
			
	}
		

}
	
	reflex chekGameOver when: isGameOver {
			do disconnectMqttClient(); 
			do pause;
		}
	
}

species Player skills: [unity] {
	
	bool isNotifiyed <- false; 						// if true, a notification is received
	bool isCenter <- false; 						//  if true, go back to the scene center
	bool isNewPosition <- false;					// if true, move the ball to a new position
	bool isNewColor <- false; 						// if true, set a new color for the ball
	int counter <- 0;
	int newX <- 0;
	int newY <- 0;
	int newZ <- 0;
	
	int formIndex <- 0;
	
	int obstacleCounter <- 0;
	
	
	
	string playerName <- (self.name = "GamaAgent0" ? "Player0" : "Player1") ;
	int totalCount <-  15;
	int updateCycle <- (self.name = "GamaAgent0" ? 4 : 6) ;
	int updateCenter <- (self.name = "GamaAgent0" ? 6 : 9) ;
	
	init{
		shape <- sphere(4);
		//name <- playerName;
	 	
		//do connectMqttClient(); 
		
		// subscribe (to unity engine) to get notifiyed by the object: Player, when its field: count of type: field,  is eaqual (operator ==) to 4 
		do unityNotificationSubscribe notificationId: self.name+"_Notification_01" objectName: playerName fieldType: "field" fieldName: "count" fieldValue: totalCount fieldOperator: "==";
			
		// subscribe to the topic: notification, (Mqtt server) in order to receive notifications 
		do subscribe_To_Topic topic: "notification";
		name <- playerName;
		
	
	}

	aspect base {
		draw shape color: color;
	}

	reflex updatePosition when: cycle mod updateCycle = 0
	{
		isNewPosition <- true; 
		isNewColor <- true;
		isCenter <- false;
		newX <- rnd(-8, 8);
		newZ <- rnd(-8, 8);
		formIndex <- rnd(0, 3);
	 //	speed <- rnd(150.0);
	}
	
	reflex updateCenter when: cycle mod updateCenter = 0
	{
		isCenter <- true;
		isNewPosition <- false;
		counter <- cycle;
		
	}

	reflex resetPositionToCenter when: isCenter 
	{
		int nX <- rnd(0,2);
		int nZ <- rnd(0,2);
		
		// Reset the Ball to the center scene center (not a move)
		do setUnityPosition objectName: playerName position: {nX,0,nZ};
		
		//location <-{newX,0.5,newZ};
		
		isCenter <- false;
	}

	reflex moveToNewPosition when: isNewPosition 
	{
		
		// set text
		do callUnityMonoAction objectName: playerName actionName:"setWinText"  attribute:"My turn"+self.name;  
		
				
		// Move the ball to the new position (not a position reset). The movement speed is specified too.
		do unityMove objectName: playerName position: {newX,newY,newZ}  smoothMove: false;
		isNewPosition <- false;
		//location <-{x,y,z};
		// Create new GameObject:
		if (obstacleCounter < 4 and cycle > 10) {
			obstacleCounter <- obstacleCounter + 1;
			rgb colorRgb <- rnd_color(255);
			do newUnityObject objectName: "Test_" + obstacleCounter type: (formList[formIndex]) color: colorRgb position: {newX,1,newZ};
			write " Create New Object";
		}
	}

	reflex colorTopic when: isNewColor 
	{
		isNewColor <- false;
		//Change the Ball's color to red
		rgb colorR <- rnd_color (255);
		do setUnityColor objectName: playerName color: colorR;
		write "message color topic sent! --> "+colorR;
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




species cubeAgent skills: [unity] 
	{

		string objectName <- "";
	 	int agX <- rnd(-11,11);
	 	int agZ <- rnd(-11,11);
	 	rgb colorEl <- rnd_color(255);
	 	
	 	bool instanceCreated <- false;
	 	
	init {
	//	do connectMqttClient(); 
		
		objectName <- self.name;
		
		
		do newUnityObject objectName: objectName type:"Cube" color:colorEl position: {agX,1,agZ};	
		do setUnityProperty objectName: objectName propertyName:"tag" propertyValue:"Pick Up";
		//location <-{x,1,z};
       	do setUnityProperty objectName: objectName propertyName:"isTrigger" propertyValue:true;		// setUnityProperty
       	
      // 	scale <- {1,1,1};
      // 	rotation <- {45,45,45};
      // 	speed <- 12.0;
		
		do setUnityFields objectName: objectName attributes: map<string, string>(["isRotate"::true]);
		
		instanceCreated <- true;
		
	}
	
	reflex createInstance when: !instanceCreated{
		
		instanceCreated <- true;
	/* 	
		do newUnityObject objectName: objectName type:"Cube" color:colorEl position: {x,1,z};	
		do setUnityProperty objectName: objectName propertyName:"tag" propertyValue:"Pick Up";
		location <-{x,1,z};
       	do setUnityProperty objectName: objectName propertyName:"isTrigger" propertyValue:true;		// setUnityProperty
       	
       	scale <- {1,1,1};
       	rotation <- {45,45,45};
       	speed <- 12.0;
		
		do setUnityFields objectName: objectName attributes: map<string, string>(["isRotate"::true]);
	*/
	}
	
	
	
}

experiment RunGame type: gui {
	map<string, point>
	anchors <- ["center"::#center, "top_left"::#top_left, "left_center"::#left_center, "bottom_left"::#bottom_left, "bottom_center"::#bottom_center, "bottom_right"::#bottom_right, "right_center"::#right_center, "top_right"::#top_right, "top_center"::#top_center];
	output {
		display view type: opengl {
			species Player aspect: base;
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








