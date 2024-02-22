/**
* Name: contains
* 
* check if proxy still exist from a minimalAgent list
* 
* Author: Lucas Grosjean
* Tags: Proxy
*/

model proxydisparition

global skills: [ProxySkill]
{		
	
	init
	{		
		create emptyAgent number: 1
		{
			location <- {10,10};	
			shape <- circle(10);
		}
		
		let li <- list(emptyAgent) inside shape; // correctly return proxy
		write(sample(li));
		
		assert(length(li) > 0);
		
		emptyAgent empty <- one_of(li);
		write(sample(getClass(empty)));
		
		write(empty.location); // pass through proxy
		write(empty.name); // pass through proxy
	}
}
species emptyAgent
{
	
}
	
experiment disparition
{
}

experiment disparition_proxy type: proxy 
{
}

experiment disparition_distribution type: distribution 
{
}
