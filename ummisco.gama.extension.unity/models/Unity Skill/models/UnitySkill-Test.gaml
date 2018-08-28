/***
* Name: UnitySkill_Tests
* Author: sklab
* Description: Check all new actions.
* Tags: Tag1, Tag2, TagN
***/

model UnitySkill_Tests

 
global skills: [unity]
{ 
	string sentMsg <- "";
	string receivedMsg <- "";
	string playerName <- "Player";
	bool isNotifiyed <- false;
	init
	{
		create GamaAgent number: 1
		{
			color <- rnd_color(255);
			shape <- sphere(4);
			
			do connectMqttClient();
			write "connected";
			
			do subscribe_To_Topic topic:"Gama";
			do subscribe_To_Topic topic:"replay";
			do subscribe_To_Topic topic:"notification";
		}
		
		do callUnityMonoAction objectName: playerName actionName:"setWinText"  attribute:"- Game ON -";  
		
		// Create the North wall
		//TODO Change position to point
		do newUnityObject objectName: "NorthWall" type:"Cube" color:rgb(255,1,255)  position: {0,1,12};
		// Set localScale of the NorthWall
		do setUnityProperty objectName: "NorthWall" propertyName:"localScale" propertyValue:{25.0,2.0,0.3};
		//do setUnityProperty objectName: "NorthWall" propertyName:"isTrigger" propertyValue:"true";
		//Disable collisions with the wall
		do setUnityProperty objectName: "NorthWall" propertyName:"detectCollisions" propertyValue:false;
	 	
		// Create the South Wall
	
		do newUnityObject objectName: "SouthWall" type:"Cube" color:rgb(255,200,0)  position: {0,1,-12};
		// Set localScale of the SouthWall
		do setUnityProperty objectName: "SouthWall" propertyName:"localScale" propertyValue:{25.0,2.0,0.3};
		do setUnityProperty objectName: "SouthWall" propertyName:"detectCollisions" propertyValue:false;
	 	
		// Create the East Wall
		do newUnityObject objectName: "EastWall" type:"Cube" color:rgb(100,100,100) position: {12,1,0};
		// Set localScale of the EastWall
		do setUnityProperty objectName: "EastWall" propertyName:"localScale" propertyValue:{24.0,2.0,0.3};
		// Set localEulerAngles of the EastWall
		do setUnityProperty objectName: "EastWall" propertyName:"localEulerAngles" propertyValue:{0,90,0};
		do setUnityProperty objectName: "EastWall" propertyName:"detectCollisions" propertyValue:false;
		
		// Create the West Wall
		do newUnityObject objectName: "WestWall" type:"Cube" color:rgb(250,50,200) position: {-12,1,0};
		// Set localScale of the WestWall
		do setUnityProperty objectName: "WestWall" propertyName:"localScale" propertyValue:{24.0,2.0,0.3};
		// Set localEulerAngles of the WestWall
		do setUnityProperty objectName: "WestWall" propertyName:"localEulerAngles" propertyValue:{0,90,0};
		do setUnityProperty objectName: "WestWall" propertyName:"detectCollisions" propertyValue:false;
	}
}



species GamaAgent skills: [unity]
{
	aspect base
	{
		draw shape color: color;
	}

	reflex writeCycle
	{
		write"\n \n - Cycle "+cycle+" - \n";
	}
		

	reflex doTest when: cycle = 100
	{
		do doTest thisIsTest:"ceci est un test";
		do doTest thisIsTest:12;
		do doTest thisIsTest:0.45;
		do doTest thisIsTest:map<string, string>(["speed"::35]);
		do doTest thisIsTest:{1,0,5};
		do doTest thisIsTest:rgb(255,1,1);
		do doTest thisIsTest:0.45;
		
		do die;
	}

	reflex subscribeToNotification when: cycle = 1
	{
		//To be notifyied when totalBox is greater or equal to 5; 
		do unityNotificationSubscribe notificationId:"Notification_01" objectName: playerName fieldType:"field" fieldName:"count" fieldValue:"1" fieldOperator:">";
		write "Agent subscribed to unity notification";
	}
	
	reflex getTopic when: cycle = 2
	{
		//Get a field value (the value of the field speed) from a unity game object (Player)
		do  getUnityField objectName: playerName attribute:"speed"; 
		write "Request to get the speed value is sent!";
	}
	
	reflex setTopic when: cycle = 4
	{
		//Change the value of the field speed on the game object Player
		// it is possible to pas several attributes and their values
		map<string,string> attributesList <- map<string, string>(["speed"::35]);
		do  setUnityFields objectName: playerName attributes:attributesList; 
	}
	
	reflex getTopic when: cycle = 6
	{
		//Get a field value (the value of the field speed) from a unity game object (Player)
		do  getUnityField objectName: playerName attribute:"speed"; 
		write "Request to get the speed value is sent!";
	}
	
	reflex setPositionTopic when: cycle = 7 
	{
		do setUnityPosition objectName: playerName position:{1,0,5};
		write "New position request sent to positionTopic";
	}
	
	
	reflex setColorTopic when: cycle = 8
	{
		//Change the color of an object
		do setUnityColor  objectName: playerName color:rgb(255,1,1); 
		write "New color  request sent to colorTopic";
	}
	
	reflex moveTopic when: cycle  = 1
	{
		//Change the color value of the specified game object
		do unityMove objectName: playerName position:{5,5,5}; 
		write "New position to move towards is sent to to moveTopic";
	}
	
	
	reflex propertyTopic when: cycle  = 1
	{
		//Change the value of a given property of a unity game object (Player)
		do  setUnityProperty objectName: playerName propertyName:"isKinematic" propertyValue:true; 
		//write "message color topic sent!";
	}
	
	
	// ----------- end ------------
	
	
	
	reflex monoFreeTopic when: cycle mod 9 = 1
	{
		//Call the method setSpeed of the game object Player, with the parameter: s, and the value: speed
		do callUnityMonoAction objectName: playerName actionName:"setSpeed"  attribute:string(50);  
		do callUnityMonoAction objectName: playerName actionName:"setSpeed"  attribute:50; 
		// write "message mono free topic sent!";
	}
	
	reflex multipleFreeTopic when: cycle < 3
	{
		//Call a method (changeAllAttributes) -- having several parameters in the map list attributesList - on a unity game object Player. The called method is 
		map<string,string> attributesList <- ["s"::50, "count"::"This is count text from gama", "win"::"This is win text from Gama"];
		//do callUnityPluralAction objectName: playerName actionName:"changeAllAttributes"  attributes:attributesList; 
	 	//write "message speed sent!";
	 	
	}
	
	
	
	reflex createNewObjectTopic when: cycle  = 15
	{
		//Create a new game object on a unity scene
		//do  newUnityObject objectName: "Test" type:"Sphere" color:"white" position:"not implemented yet"; 
		//write "message color topic sent!";
	}
	

	reflex getReplayMessage 
	{
		// Get the requested field value.
		//TODO: Add the fieldName as a parameter
		string fieldValue <- get_unity_replay(); 
		if(fieldValue!="null"){
			write "The requested field value is: " + fieldValue;
		}
	}
	
	reflex getNotificationMessage {
		//string mes <- get_unity_notification();
		//write "cycle = "+cycle+" and received notification message is : --------------------> " + mes;
	}
	
	reflex checkNotification when: !isNotifiyed{
		isNotifiyed <- isNotificationTrue("Notification_01");
	//	write "Notification Note Received Yet ";

	}
	
	
	
	reflex checkNotification{
		//write "isNotifiyed----->>>> "+isNotifiyed;
		if(isNotifiyed){
			write "---------------------------------------------- The end";
			do die;
		}
		
	}
	
	

}

experiment UnitySkill_Tests_Experiment type: gui 
{
	
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






