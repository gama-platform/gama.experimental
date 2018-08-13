/**
* Name: Testconnection
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model Testconnection

global skills:[RSkill]{
	
	init{
		do startR;
		write R_eval("x<-1");
		write R_eval("rnorm(50,0,5)");
	}
	
}
experiment RJava type:gui{
	output{}
}
