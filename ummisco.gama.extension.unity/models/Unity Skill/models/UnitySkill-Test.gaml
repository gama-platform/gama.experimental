/***
* Name: UnitySkill_Tests
* Author: sklab
* Description: This model is used to run experiments on new developped actions and operators, without a particular purpose!
* Tags: Tag1, Tag2, TagN
***/

model UnitySkill_Tests

 
global skills: [network]
{ 
	string sentMsg <- "";
	string receivedMsg <- "";
	bool isNotifiyed <- false;
	init
	{
		create GamaAgent number: 1
		{
			color <- rnd_color(255);
			shape <- sphere(4);
			string agentName <- "Agent 1";
			do connectMqttClient(agentName);
			write "connected";
			
			do subscribe_To_Topic idClient:agentName topic:"Gama";
			do subscribe_To_Topic idClient:agentName topic:"replay";
			do subscribe_To_Topic idClient:agentName topic:"notification";
			
			
		}
	}
}

species GamaAgent skills: [unity]
{
	aspect base
	{
		draw shape color: color;
	}

	reflex subscribeToNotification when: cycle = 2
	{
		//To be notifyied when totalBox is greater or equal to 5; 
		do unityNotificationSubscribe notificationId:"Notification_01" objectName: "Player" fieldType:"field" fieldName:"count" fieldValue:"1" fieldOperator:">";
	}
	
	reflex mainTopic when: cycle mod 5 = 1
	{
		//string bb <- setUnityPosition(0.1); 
		map<string,string> mapAtt <- ["moveHorizontal"::"0.5", "moveVertical"::"0.5"];
		//do send_unity_message senderU:"me" actionU:"UpdatePosition" objectU: "Player" attributeU:mapAtt topicU: "Unity"; 
	}
	
	
	reflex setTopic when: cycle = 2
	{
		//Change the value of the field speed on the game object Player
		int speed<- 35;
		map<string,string> attributesList <- map<string, string>(["speed"::speed]);
		//do  setUnityFields objectName: "Player" attributes:attributesList; 
		//write "Set value sent ";
	}
	
	reflex getTopic when: cycle = 3
	{
		//Get a field value (the value of the field speed) from a unity game object (Player)
		int speed<- 50;
	//	do  getUnityField objectName: "Player" attribute:"speed"; 
	//	write "Ask for replay sent ";
	}
	
	
	reflex monoFreeTopic when: cycle mod 9 = 1
	{
		//Call the method setSpeed of the game object Player, with the parameter: s, and the value: speed
		int speed<- 50;
		//map<string,string> att <- map<string, string>(["s"::speed]);
		//do callUnityMonoAction objectName: "Player" actionName:"setSpeed"  attribute:string(speed);  
	//	write "message mono free topic sent!";
	}
	
	reflex multipleFreeTopic when: cycle < 3
	{
		//Call a method (changeAllAttributes) -- having several parameters in the map list attributesList - on a unity game object Player. The called method is 
		int speed<- 50;
		map<string,string> attributesList <- ["s"::speed, "count"::"This is count text from gama", "win"::"This is win text from Gama"];
		//do callUnityPluralAction objectName: "Player" actionName:"changeAllAttributes"  attributes:attributesList; 
	 	//write "message speed sent!";
	 	
	}
	
	
	reflex colorTopic when: cycle mod 10 = 1
	{
		//Change the color value, to red, of the game object Player.
		//do setUnityColor  objectName: "Player" color:"red"; 
		//write "message color topic sent!";
	}
	
	
	reflex positionTopic //when: cycle mod 6 = 1
	{
		//Change the color value of the specified game object
		map<string,string>  pos<- map<string, string>(["x"::cycle/100,"y"::"0.0","z"::"0.0"]);
		string test <- "";
		//do  setUnityPosition  objectName: "Player" position:pos; 
		//write "message color topic sent!";
	}
	
	reflex moveTopic when: cycle  = 5
	{
		//Change the color value of the specified game object
		map<string,string> pos <- map<string, string>(["x"::1,"y"::"0","z"::"1"]);
		string test <- "";
		//do  unityMove objectName: "Player" position:pos; 
		//write "message color topic sent!";
	}
	
	reflex propertyTopic when: cycle  = 2
	{
		//Change the value of a given property of a unity game object (Player)
		//do  setUnityProperty objectName: "Player" propertyName:"isKinematic" propertyValue:"true"; 
		//write "message color topic sent!";
	}
	
	
	reflex createNewObjectTopic when: cycle  = 15
	{
		//Create a new game object on a unity scene
		//do  newUnityObject objectName: "Test" type:"Sphere" color:"white" position:"not implemented yet"; 
		//write "message color topic sent!";
	}
	

	reflex getMessage {
		//string mes <- get_unity_message();
		//write "received Message is :" + mes;
	}
	
	reflex getReplayMessage {
		//string mes <- get_unity_replay();
		//write "cycle = "+cycle+" and received Replay message is : --------------------> " + mes;
	}
	
	reflex getNotificationMessage {
		//string mes <- get_unity_notification();
		//write "cycle = "+cycle+" and received notification message is : --------------------> " + mes;
	}
	
	reflex checkNotification23 when: !isNotifiyed{
		isNotifiyed <- isNotificationTrue("Notification_01");
		write "Notification Note Received Yet ";
	}
	
	reflex checkNotification{
		write "isNotifiyed----->>>> "+isNotifiyed;
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






