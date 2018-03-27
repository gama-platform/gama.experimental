/**
* Name: Testconnection
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model Testconnection

global skills:[RSkill]{
	file Rcode<-text_file("../includes/rScript.txt");
	
	init{
		do startR;

	 	loop s over:Rcode.contents{
			unknown a<- R_eval(s);
			write "R>"+s;
			write a;
		}
	}
	
}
experiment RJava type:gui{
	output{
	}
}
