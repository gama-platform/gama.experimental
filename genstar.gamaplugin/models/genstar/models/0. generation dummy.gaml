/**
* Name: Test
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model Test

global {
	init {
		write dummy_generator(40);
		
		create people from: dummy_generator(10);
		
		write people accumulate(each.iris);
	}
}

species people {
	string iris;
	
	aspect default { 
		draw circle(0.5) color: #red border: #black;
	}
}


experiment defaultGen type: gui {
	output {
		display map {
			species people;
		}
	}
}
