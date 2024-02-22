/**
* Name: population_operators
* testing operations on population
* Author: lucas
* Tags: 
*/


model population_operators

import "../Models_to_distribute/MovingAgent.gaml"

global skills: [ProxySkill]
{		
	geometry circ <- circle(25);
	int lenght_moving <- 4;
	
	init
	{		
		create agentSpecie number: lenght_moving;
		
		agentSpecie l1 <- one_of(agentSpecie);
		list<agentSpecie> l2 <- agentSpecie - l1;
		
		write("l1 : " + l1);
		write("l2 : " + l2);
		
		write("lenght l1 " + length(l1));
		write("lenght l2 :  " + length(l2));
		
		//assert (length(l1) + length(l2)) = lenght_moving;
	}
}

species agentSpecie
{
	
}
experiment test_inside_proxy type: proxy 
{
}

experiment test_inside_distrib type: distribution 
{
}

experiment test_inside
{
}
