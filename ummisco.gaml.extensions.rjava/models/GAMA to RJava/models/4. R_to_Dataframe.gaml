/**
* Name: RtoDataframe
* Author: Benoit Gaudou
* Description: Example model illustrating the to_R_dataframe transforming a species into a dataframe.
* Tags: R, dataframe
*/

model RtoDataframe

global skills: [RSkill] {
	
	init{
		do startR();
		
		create people number: 10;
		
		write to_R_dataframe(people);
		
		do R_eval("df<-" + to_R_dataframe(people));
		write R_eval("df");
		write R_eval("df$flipCoin");	
	}
}

species people {
	float f <- rnd(10.0);
	int i <- rnd(100);
	string names <- one_of(["abc", "def", "ghi"]);
	bool flipCoin <- flip(0.5);
}

experiment RtoDataframeEx type:gui {}

