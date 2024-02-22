/**
* Name: self
* Test of statement "self" and "myself" with proxyAgent
* Author: Lucas Grosjean
* Tags: proxy, statement, self
*/

model self_proxy

import "../Models_to_distribute/MovingAgent.gaml"

global skills: [ProxySkill]
{
	list<agent> list_of_all_agents_without_self;
	list<agent> list_of_all_agents_without_myself;
	
	init
	{
		create movingAgent number: 4;
		
		ask movingAgent
		{
			list_of_all_agents_without_self <- (movingAgent - self);
		}	
			
		ask movingAgent[0]
		{
			ask movingAgent
			{
				list_of_all_agents_without_myself <- (movingAgent - myself);
			}
		}
		
		assert movingAgent != list_of_all_agents_without_myself;
		assert movingAgent != list_of_all_agents_without_self;
	}
}

experiment self_exp
{
}
experiment self_proxy type: proxy until: (cycle = 1) 
{
}

experiment self_distribution type: distribution until: (cycle = 1) 
{
}
