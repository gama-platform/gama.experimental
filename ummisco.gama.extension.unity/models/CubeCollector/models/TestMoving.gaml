/***
* Name: TestMoving
* Author: sklab
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model TestMoving

global skills: [network] {
	string sentMsg <- "";
	string receivedMsg <- "";
	bool isNotifiyed <- false;
	bool isCenter <- false;
	bool isNewPosition <- false;
	bool isNewColor <- false;
	
	int counter <- 0;
	int x <- 0;
	int y <- 0;
	int z <- 0;
	int colorIndex <- 0;
	int formIndex <- 0;
	int speed <- 10;
	int obstacleCounter <- 0;
	list colorlist <- ["black", "red", "blue", "white", "yellow"];
	list formList <- ["Capsule", "Cube", "Cylinder", "Sphere"];

	init {
		create GamaAgent number: 1 {
			color <- rnd_color(255);
			shape <- sphere(4);
			string agentName <- "Agent 1";
			do connectMqttClient(agentName);
			write "connected";
			do subscribe_To_Topic idClient: agentName topic: "Gama";
			do subscribe_To_Topic idClient: agentName topic: "replay";
			do subscribe_To_Topic idClient: agentName topic: "notification";
			do unityNotificationSubscribe notificationId: "Notification_01" objectName: "Player" fieldType: "field" fieldName: "count" fieldValue: "4" fieldOperator: "==";
		}

	}

}

species GamaAgent skills: [unity] {

	aspect base {
		draw shape color: color;
	}

	reflex updatePosition {
		int dif <- cycle - counter;
		if (dif = 4) {
			isNewPosition <- true;
			isNewColor <- true;
			isCenter <- false;
			x<-rnd(-8,8);
			z<-rnd(-8,8);
			colorIndex <- rnd(0,4);
			formIndex <- rnd(0,3);
			speed <- rnd(10, 150);
		}

		if (dif = 9) {
			isCenter <- true;
			isNewPosition <- false;
			counter <- cycle;
		}

	}



	reflex resetPositionToCenter when: isCenter {
	// Move the specified object to the introduced position
		map<string, string> pos <- map<string, string>(["x"::0, "y"::0, "z"::0]);
		//do setUnityPosition objectName: "TestObject" position: pos;
		do  setUnityPosition objectName: "Player" position:pos; 
		isCenter <- false;
		write "Comme back to center! x=" + 0 + " y=" + 0 + " z=" + 0;
	}

	reflex moveToNewPosition when: isNewPosition 
	{	
		
		//z <- 7;
		// Move the specified object to the introduced position
		map<string, string> pos <- map<string, string>(["x"::x, "y"::y, "z"::z]);
		//do unityMove objectName: "TestObject" position: pos speed:speed*((cycle mod 6));
		do unityMove objectName: "Player" position: pos speed:speed;
		isNewPosition <- false;
		write "Move to new position!  x=" + x + " y=" + y + " z=" + z;
		
		// Create new GameObject:
		if(obstacleCounter < 4 and cycle > 10){
				obstacleCounter <- obstacleCounter + 1;
				map<string, string> posi <- map<string, string>(["x"::x, "y"::1, "z"::z]);
				do  newUnityObject objectName: "Test_"+obstacleCounter type:(formList[formIndex]) color:(colorlist[colorIndex]) position:posi; 
				write " Create New Object";
		}
		
		// Create new GameObject:
		
	}

	reflex colorTopic when: isNewColor {
		isNewColor <- false;
		string colorEl <- colorlist[colorIndex];
		//Change the color value, to red, of the game object Player.
		do setUnityColor objectName: "TestObject" color: colorEl;
		write "message color topic sent!";
	}
	
	
	reflex checkNotification when: !isNotifiyed {
		isNotifiyed <- isNotificationTrue("Notification_01");
	}
	
	reflex endGame when: isNotifiyed
	{
		do destroyUnityObject objectName: "Player";
		write "Game Over  --------------- The end";
		do die;
	}
	
	
	
	

	
	
}

experiment CubeCollectorGame type: gui {
	map<string, point>
	anchors <- ["center"::#center, "top_left"::#top_left, "left_center"::#left_center, "bottom_left"::#bottom_left, "bottom_center"::#bottom_center, "bottom_right"::#bottom_right, "right_center"::#right_center, "top_right"::#top_right, "top_center"::#top_center];
	output {
		display view type: opengl {
			species GamaAgent aspect: base;
			graphics Send_Receive {
				draw world.shape empty: true color: #black;
				draw circle(0.5) at: {50, 5} color: #red;
				draw sentMsg at: {52, 5} anchor: {0.8, 0.8} color: #black font: font("Helvetica", 13, #bold);
				draw circle(0.5) at: {50, 10} color: #red;
				draw receivedMsg at: {52, 10} anchor: {0.8, 0.8} color: #blue font: font("Helvetica", 13, #bold);
			}

		}

	}

}








