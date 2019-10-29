/***
* Name: UnitySkill_AllActionsCollection
* Author: sklab
* Description: This model is used as a collection of all existant actions and operators!
* Tags: Tag1, Tag2, TagN
***/
model UnitySkill_AllActionsCollection

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
	int speed <- 100;
	int obstacleCounter <- 0;
	list colorlist <- ["black", "red", "blue", "white", "yellow"];
	list formList <- ["Capsule", "Cube", "Cylinder", "Sphere"];

	init {
		create GamaAgent number: 1 {
			color <- rnd_color(255);
			shape <- sphere(4);
			
			do connect_unity  to:"localhost"  login:"admin" password:"admin" port: 1883;
			write "connected";
			do subscribe_to_topic topic: "Gama";
			do subscribe_to_topic topic: "replay";
			do subscribe_to_topic topic: "notification";
			do unityNotificationSubscribe notificationId: "Notification_01" objectName: "Player0" fieldType: "field" fieldName: "count" fieldValue: "5" fieldOperator: "==";
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
			x <- x + 1;
			if (x >= 4) {
				x <- -4;
				z <- z + 1;
			}

			if (z >= 4) {
				z <- -4;
			}

			colorIndex <- colorIndex + 1;
			if (colorIndex >= 5) {
				colorIndex <- 0;
			}
		
			formIndex <- formIndex + 1;
			if (formIndex >= 4) {
				formIndex <- 0;
			}
				
			

		}

		if (dif = 9) {
			isCenter <- true;
			isNewPosition <- false;
			counter <- cycle;
		}

	}

	reflex setSpeedTopic when: cycle = 1 {
		//Change the value of the field speed on the game object Player
		map<string, string> attributesList <- map<string, string>(["speed"::35]);
		do setUnityFields objectName: "Player0" attributes: attributesList;
		//write "Set value sent ";
	}

	reflex subscribeToNotification when: cycle = 1 {
	//To be notifyied when totalBox is greater or equal to 5; 
	//do unityNotificationSubscribe notificationId:"Notification_01" objectName: "Player0" fieldType:"field" fieldName:"count" fieldValue:"4" fieldOperator:"==";
	}

	reflex moveBackToCenter when: isCenter {
	// Move the specified object to the introduced position
		map<string, string> pos <- map<string, string>(["x"::0, "y"::0, "z"::0]);
		//do  unityMove objectName: "Player0" position:pos; 
		//write "Comme back to center! x="+0+ " y="+0+" z="+0;
		//isCenter <- false;
	}

	reflex resetPositionToCenter when: isCenter {
	// Move the specified object to the introduced position
		
		do setUnityPosition objectName: "Player0" position: {0,0,0};
		//do  unityMove objectName: "Player0" position:pos; 
		isCenter <- false;
		write "Comme back to center! x=" + 0 + " y=" + 0 + " z=" + 0;
	}

	reflex moveToNewPosition when: isNewPosition 
	{	
		// set new Speed for the player object:
		map<string, string> speedValue <- map<string, string>(["speed"::speed+((cycle mod 9)* 10)]);
		do setUnityFields objectName: "Player0" attributes: speedValue;
		
		// Move the specified object to the introduced position

		do unityMove objectName: "Player0" position: {x,y,z} speed:speed+((cycle mod 9) * cycle);
		isNewPosition <- false;
		write "Move to new position!  x=" + x + " y=" + y + " z=" + z;
		
		// Create new GameObject:
		if(obstacleCounter < 4 and cycle > 10){
			obstacleCounter <- obstacleCounter + 1;
			do  newUnityObject objectName: "Test_"+obstacleCounter type:(formList[formIndex]) color:rgb(rnd(0,255),rgb(0,255),rgb(0,255)) position:{x,y,z}; 
			write " Create New Object";
		}
	}

	reflex colorTopic when: isNewColor {
		isNewColor <- false;
		string colorEl <- colorlist[colorIndex];
		//Change the color value, to red, of the game object Player.
		do setUnityColor objectName: "Player0" color: rgb(rnd(0,255),rgb(0,255),rgb(0,255));
		write "message color topic sent!";
	}

	reflex checkNotification when: !isNotifiyed {
		//isNotifiyed <- isNotificationTrue("Notification_01");
	}

	reflex endGame when: isNotifiyed
	{
		do destroyUnityObject objectName: "Player0";
		write "---------------------------------------------- The end";
		do die;
	}
	
	
	
	
	
}

experiment UnitySkill_AllActionsCollection type: gui {
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






