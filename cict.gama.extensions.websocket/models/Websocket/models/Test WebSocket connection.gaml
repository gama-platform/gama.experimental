/**
* Name: Testconnection
* Author: hqnghi
* Description: 
* Tags: Tag1, Tag2, TagN
*/
model Testconnection

global { 
	init { 
		create A number:100;
	}
 

} 
species A skills:[moving]{
	reflex ss{
		do wander;
	}
	
}
experiment mike type: gui {
	output {
		display "s" type: web background:#black synchronized:true{ 
			species A;
		}

	}

}
