/**
* Name: contains
* 
* contains test with proxy
* 
* Author: Lucas Grosjean
* Tags: Proxy
*/

model contains

import "../Models_to_distribute/MovingAgent.gaml"

global skills: [ProxySkill]
{		
	movingAgent agt;
	list<movingAgent> species_inside;
	list<movingAgent> agents_inside;
	
	init
	{		
		create movingAgent number: 4;
		agt <- one_of(movingAgent); // random agents
		species_inside <- movingAgent inside shape; // all agents
		
		list agents <- movingAgent;
		agents_inside <- agents inside shape; // all agents
		
		bool res <- species_inside contains one_of(agt);
		bool res2 <- agents_inside contains one_of(agt);
		write("contains : " + res);
		write("contains : " + res2);
	}
}
	
	
experiment contains
{
}

experiment contains_proxy type: proxy 
{
}

experiment contains_distrib type: distribution 
{
}
