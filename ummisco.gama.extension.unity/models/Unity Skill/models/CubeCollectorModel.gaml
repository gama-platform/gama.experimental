/***
* Name: CubeCollector
* Author: sklab
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model CubeCollector

global {

	int counter <- 0;
	
	init{
		create gama_agent number: 1;
	}
}

 




species gama_agent skills: [unity]{
	string playerName <- "Player"; // <- "Object1";
	
	
	aspect default {
		draw circle(1) color:#red;
	}
	
	init{
		 do connect_unity  to:"localhost"  login:"admin" password:"admin" port: 1883;
		 
		 do subscribe_to_topic topic:"littosim";
		 do subscribe_to_topic topic:"replay";
		
		 write "connected";
		
		// do getUnityField objectName: playerName attribute:"speed"; 											    // getUnityField
		
	   	// do callUnityMonoAction objectName: playerName   actionName:"setWinText"  attribute:" --- Game ON --- "; // monoActionTopic
		
	}
	
	
//	reflex contactUnity when: cycle mod 20 = 0
//	{
//			do callUnityMonoAction objectName: playerName   actionName:"setWinText"  attribute:" --- Call to move --- "; // monoActionTopic
//		
//			do unityMove objectName: playerName position: {cycle,cycle,7} speed: 50 smoothMove: true;	 // unityMove
//			
//			write "GameObject moved!";
//	}
	
	
	reflex sendAction when: cycle mod 20 = 0
	{
		counter <- counter + 1;
			
		switch counter { 
	        match 1 {
	        	map<string,string> AtList <- ["s"::50, "count"::"Count Text Changed", "win"::"Win Text Changed"];
	        	do callUnityPluralAction objectName: playerName actionName:"changeAllAttributes"  attributes:AtList; 	// callUnityPluralAction
	        	write " Mise à jour des attributs par appel de méthode. (Speed = 50, count text = Count Text Changed, win text = Win Text Changed)";
	        } 
	        match 2 {
	        	do setUnityFields objectName: playerName attributes: map<string, string>(["speed"::35]);
	        	write " Mise à jour de l'attribut speed = 35";
	        }				
	        match 3 {
	        	do setUnityFields objectName: playerName attributes: map<string, string>(["count"::10]);
	        	write " Mise à jour de l'attribut count = 10";
	        }				
	        match 4 {
	        	do newUnityObject objectName: "CubeTest" type:"Cube" color:rgb(255,1,144) position: {-9,0.5,8};
	        	write " Création d'un cube 'CubeTest'";
	        }			
	        match 5 {
	        	do setUnityProperty objectName: "CubeTest" propertyName:"localScale" propertyValue:{2,2,2};
	        	write " 1- Changer les dimensions du cube 'CubeTest'";
	        }		
	        match 6 {
	        	do setUnityProperty objectName: "CubeTest" propertyName:"localScale" propertyValue:{1,10,2};
	        	write " 2- Changer les dimensions du cube 'CubeTest'";
	        }		
	        match 7 {
	        	do setUnityProperty objectName: "West Wall" propertyName:"localScale" propertyValue:{0.5,3,20.5};
	        	write " Changer les dimensions de 'West Wall'";
	        }		
	        match 8 {
	        	do setUnityProperty objectName: "East Wall" propertyName:"localScale" propertyValue:{0.5,3,20.5};
	        	write " Changer les dimensions de 'East Wall'";
	        }		
	        // match 9 {do setUnityProperty objectName: "North Wall" propertyName:"localScale" propertyValue:{20.5,3,0.5};}		
	        // match 10 {do setUnityProperty objectName: "South Wall" propertyName:"localScale" propertyValue:{20.5,3,0.5};}		
	        match 11 {
	        	do setUnityProperty objectName: "CubeTest" propertyName:"localEulerAngles" propertyValue:{90,90,90};
	        	write " Tourner le  'CubeTest'";
	        }		
	        //match 12 {do setUnityProperty objectName: "CubeTest" propertyName:"detectCollisions" propertyValue:false;}		
	        match 13 {
	        	do callUnityMonoAction objectName: playerName actionName:"setSpeed"  attribute:string(55);
	        	write " Set speed = 55";
	        } 				
	        match 14 {
	        	do callUnityMonoAction objectName: playerName actionName:"setSpeed"  attribute:10;
	        	write " Set speed = 10";
	        } 						
	        match 15 {
	        	do setUnityColor objectName: playerName color: rgb(0,100,0);
	        	write " Changer couleur de Player";
	        }											
	        match 16 {
	        	do setUnityPosition objectName: playerName position: {0,-0.5,7};
	        	write " Changer position de Player";
	        }											
	        match 17 {
	        	do setUnityPosition objectName: playerName position: {0,-0.5,0};
	        	write " Changer position de Player";
	        }	      
	        match 18 {
	        	do unityMove objectName: playerName position: {6,-0.5,0.5} speed: 20 smoothMove: false;
	        	write " Changer position de Player ";
	        }	
	        match 19 {
	        	do setUnityPosition objectName: playerName position: {0,-0.5,0};
	        	write " Changer position de Player ";
	        }
	        match 20 {
	        	do callUnityMonoAction objectName: playerName   actionName:"setWinText"  attribute:" --- Call to move --- ";
	        } // monoActionTopic}
	   }
		
		
		
		//	do unityMove objectName: playerName position: {-6,-0.5,0.5} speed: 20 smoothMove: true;						// unityMove
		
		//	do setUnityColor objectName: playerName color: rgb(50, -50, -50);	
					
		//	do unityMove objectName: playerName position: {cycle,cycle,7} speed: 50 smoothMove: true;	 // unityMove
			
		write "cycle : "+cycle;
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
	
	reflex check_new_message when: has_next_message_topic("replay"){
		
		write " Good, a new message";
		
		string msg <- get_unity_filtered("replay");
		
		write " the message is : ";
		
		write msg;
		
	} 
	
	aspect base {
		draw cube(3) color:#green;
	}
}


experiment RunExp type:gui {
	output {
		display Display1 type:opengl{
			species gama_agent aspect: base;
		}		
	}
}