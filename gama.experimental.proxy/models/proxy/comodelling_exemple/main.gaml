
model main

import "sub.gaml" as Thematic2



global
{	
	init
	{	
		create Thematic2.Thematic_experiment;
		create mainSpecies;
		create blob;
	}
}

species mainSpecies
{
	int k <- 5;
}

species blob
{
	reflex
	{
		list<subSpecies> li;
		list<mainSpecies> li2 <- list(mainSpecies);
		ask Thematic2.Thematic_experiment[0]
		{
			li <- list(subSpecies);
		}
		loop tmp over: li
		{			
			write("closest : " + li2 closest_to tmp);
		}
		
	}
}

experiment mainExp
{
	
}