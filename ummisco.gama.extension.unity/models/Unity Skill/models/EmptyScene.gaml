/***
* Name: EmptyScene
* Author: sklab
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model EmptyScene


global {
/** Insert the global definitions, variables and actions here */

	init{
		create CheckingAgent number: 1;
	}
}

 




species CheckingAgent skills: [unity]{
	string playerName <- "Player0";
	//string playerName <- "Object1";
	
	aspect default {
		draw circle(1) color:#red;
	}
	
	init{
		 do connect_unity  to:"localhost"  login:"admin" password:"admin" port: 1883;
		 
		 do subscribe_to_topic topic:"littosim";
		
		 write "connected";
		 
		
		//	do getUnityField objectName: playerName attribute:"speed"; 											    // getUnityField
		
		// do callUnityMonoAction objectName: playerName   actionName:"setWinText"  attribute:" --- Game ON --- "; // monoActionTopic
		
		
		map<string,string> AtList <- ["s"::50, "count"::"Count textchanged", "win"::"Win text changed"];
		do callUnityPluralAction objectName: playerName actionName:"changeAllAttributes"  attributes:AtList; 	// callUnityPluralAction
	
		do setUnityFields objectName: playerName attributes: map<string, string>(["speed"::35]);				// setUnityFields
	
		//	do setUnityFields objectName: playerName attributes: map<string, string>(["count"::10]);				// setUnityFields
		
		
		//TODO: Add scale as optionnal 
		do newUnityObject objectName: "EastWall" type:"Cube" color:rgb(255,1,144) position: {12,1,0};			// newUnityObject
		
		do setUnityProperty objectName: "EastWall" propertyName:"localScale" propertyValue:{100.0,2.0,0.3};		// setUnityProperty
	    
	    do setUnityProperty objectName: "EastWall" propertyName:"localEulerAngles" propertyValue:{0,90,0};		// setUnityProperty
	
		do setUnityProperty objectName: "EastWall" propertyName:"detectCollisions" propertyValue:false;			// setUnityProperty
	
		do callUnityMonoAction objectName: playerName actionName:"setSpeed"  attribute:string(50); 				// callUnityMonoAction
	
		do callUnityMonoAction objectName: playerName actionName:"setSpeed"  attribute:60; 						// callUnityMonoAction
	
		do setUnityColor objectName: playerName color: rgb(0,100,0);											// setUnityColor
	
	//	do setUnityPosition objectName: playerName position: {4,1,9};											// setUnityPosition
	
		
	//	do setUnityPosition objectName: playerName position: {4,0,7};	
		
	//	do unityMove objectName: playerName position: {1000,200,7} speed: 5 smoothMove: false;	 // unityMove
		
	//	do unityMove objectName: playerName position: {0,0,-2} speed: 100 smoothMove: true;						// unityMove
		
		do setUnityColor objectName: playerName color: rgb(50,50,50);	
		
		
	}
	
	
	reflex contactUnity when: cycle = 20
	{
			do callUnityMonoAction objectName: playerName   actionName:"setWinText"  attribute:" --- Call to move --- "; // monoActionTopic
		
			do unityMove objectName: playerName position: {10,20,7} speed: 5 smoothMove: false;	 // unityMove
			
			write "GameObject moved!";
	}
	
	
	
	reflex destroyObject when: cycle = 8 {
		
		//	string fieldValue <- get_unity_replay(); 																// get_unity_replay
		//	write "Field value is "+fieldValue;
		
		//	bool isNotifiyed <- isNotificationTrue("Notification_01");												// isNotifiyed
		//	write "Notification Statut "+isNotifiyed;
		
		//	string mes <- get_unity_notification();	
		//	write "Notification Message (if exist) "+mes;															// get_unity_notification
				
		//do destroyUnityObject objectName: playerName;							// destroyUnityObject
		//string msg<- getAllActionsMessage();
		//write msg; 															
	}
	
	aspect base {
		draw cube(3) color:#green;
	}
}


experiment RunExp type:gui {
/** Insert here the definition of the input and output of the model */
	output {
		display Dp1 type:opengl{
			species CheckingAgent aspect: base;
		}		
	}
}