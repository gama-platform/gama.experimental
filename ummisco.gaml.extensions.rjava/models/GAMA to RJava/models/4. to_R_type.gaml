/**
* Name: 4toRtype
* Author: ben
* Description: 
* Tags: Tag1, Tag2, TagN
*/

model toRtype

global skills:[RSkill] {
	init {
		do startR();
		
		string s2 <- "s2";
		list<int> numlist <- [1,2,3,4]; 

  		write R_eval("numlist = " +to_R_data(numlist));
	}
}

experiment toRtype type: gui {
	output { }
}
