/**
* Name: dispose
* 
* Test how proxy agent are disposed
* 
* Author: Lucas Grosjean
* Tags: HPC, Proxy
*/


model container

import "../Models_to_distribute/MovingAgent.gaml"


global skills: [ProxySkill]
{
	list<movingAgent> list_of_agent;
	map<int, list<movingAgent>> map_of_agent <- map<int, list<movingAgent>>([]);				// agents currently in OLZ
	
	movingAgent testAgent;
	
	init
	{
		create movingAgent;
		testAgent <- movingAgent[0];
		
		ask testAgent
		{		
			list_of_agent <- [self];
			write("list_of_agent0 " + getClass(list_of_agent[0]));
		}
		ask testAgent
		{		
			map_of_agent[0] <- [self];
			write("map_of_agent[0] " + getClass(map_of_agent[0][0]));
		}
	}
	
	reflex
	{
		ask movingAgent
		{
			write("solo " + getClass(self));
		}
		
		write("contians list ? : " + list_of_agent contains testAgent);
		ask list_of_agent
		{
			write("list_of_agent " + getClass(self));
		}
		
		write("contians map ? : " + map_of_agent[0] contains testAgent);
		ask map_of_agent[0]
		{
			write("map_of_agent[0] " + getClass(self));
			write(" self = map : " + (self = testAgent));
		}
	}
	
	reflex when: cycle = 10
	{
	 	ask experiment
	 	{	 		
	 		do die;	
	 	}
	}
}
experiment container_proxy type: proxy
{
}
experiment container_proxy_distrib type: distribution until: (cycle = 1)
{
}