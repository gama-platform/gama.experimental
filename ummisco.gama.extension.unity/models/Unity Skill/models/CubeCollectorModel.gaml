/***
* Name: CubeCollectorModel
* Author: sklab
* Description: 
* Tags: Tag1, Tag2, TagN
***/

model Model_Test



global skills:[network, unity]{
	
	
	init{
		
		create player number: 1 {
			 do connect_unity  to:"localhost"  login:"admin" password:"admin" port: 1883;
		}
		
	}
	
}



species player skills:[unity]{
	
	
	string objectName <- "object";
	
	reflex updatePosition 
	{
		
		int nbr <- cycle + 1;
		int nbr1 <- cycle + 1;
		int nbr2 <- cycle + 1;
		do setUnityPosition objectName: "Object1" position: {cycle,cycle,0};
		
		do newUnityObject objectName: ""+objectName+nbr type:"Cube" color:rgb(255,1,144) position: {nbr1, nbr2, 0};
		
	//	do setUnityProperty objectName: objectName propertyName:"localScale" propertyValue:{25.0,2.0,0.3};		// setUnityProperty
	
	//	do setUnityProperty objectName: objectName propertyName:"localEulerAngles" propertyValue:{0,90,0};		// setUnityProperty
	
	//	do setUnityProperty objectName: objectName propertyName:"detectCollisions" propertyValue:false;			// setUnityProperty
		
	}
	
	
	
	aspect base {
		draw cube(2) color: #red;
	}
	
	
}








experiment RunGame type: gui {
	output {
		display view type: opengl {
			species player aspect: base;
			graphics Send_Receive {
	
			}

		}

	}

}

