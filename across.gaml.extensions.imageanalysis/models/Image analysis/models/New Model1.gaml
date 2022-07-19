/**
* Name: NewModel1
* Based on the internal empty template. 
* Author: admin_ptaillandie
* Tags: 
*/


model NewModel1
global {
	init {
		matrix<int> mm <- matrix(2,2);
		write sample(mm);
		Lego l <-lego_with ("test", {2,2}, 1);
		write sample(l);
	}
	
	
	 
}


experiment t;