/***
* Name: testAgentAttributes
* Author: youcef sklab
* Description: This model aims to test if the new attributes of an agent well defined.
* Tags: Tag1, Tag2, TagN
***/
model testAgentAttributes

global {
/** Insert the global definitions, variables and actions here */

	init{
		create agentAttributes number: 1{	
			speed <- 12.0;
		}
	}
}

 




species agentAttributes skills: [unity]{
	string playerName <- "Player0";
	aspect default {
		draw circle(1) color:#red;
	}
	
	init{
		self.name <- "mainAgent";
		do connectMqttClient();
		
		do newUnityObject objectName: self.name type:"Cube" color:rgb(255,1,144) position: {-5,1,-8};			// newUnityObject
		
		speed <- 12.0;
		location <-{1.0,1.0,1.0};
		rotation <- {45,45,45};
		scale <- {2,2,2};
		
		write "speed is : "+speed;
		
		write "connected";
	}
	
	reflex contactUnity when: cycle = 1
	{
	
	}
}


experiment runTest type:gui {
/** Insert here the definition of the input and output of the model */
	output {
		display Dp1 type:opengl{
			species agentAttributes;
		}
		
	}
}