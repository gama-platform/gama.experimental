/**
* Name: toRType
* Author: Benoit Gaudou
* Description: Model illustrating the conversion of a GAML complex type (list) into a R type.
* Tags: R, type, to_R_data
*/

model toRtype

global skills:[RSkill] {
	init {
		do startR();
		
		string s2 <- "s2";
		list<int> numlist <- [1,2,3,4]; 

  		write R_eval("numlist = " + to_R_data(numlist));
	}
}

experiment toRtype type: gui {}