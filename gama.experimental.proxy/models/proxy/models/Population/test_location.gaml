/**
* Name: test_location
* 
* Test of location with proxy
* 
* Author: Lucas Grosjean
* Tags: Proxy
*/

model test_location

import "../Models_to_distribute/MovingAgent.gaml"

global skills: [ProxySkill]
{		
	geometry circ <- circle(25);
	
	init
	{		
		create movingAgent number: 1;
	}
	
	reflex
	{		
		let inside_overlapping <- (movingAgent overlapping circ);
		let outside_overlapping <- movingAgent where ( not(each in inside_overlapping));
		
		write("inside_overlapping : " + inside_overlapping);
		write("outside_overlapping : " + outside_overlapping);
	}
}
experiment test_inside_proxy type: proxy 
{
	
	output{
		display inside_proxy type: 2d
		{
			graphics ""
			{
				draw circ;
			}
			species movingAgent aspect: classic;
		}
	}
}

experiment test_inside_distrib type: distribution 
{
	
	output{
		display inside_proxy type: 2d
		{
			graphics ""
			{
				draw circ;
			}
			species movingAgent aspect: classic;
		}
	}
}

experiment test_inside
{
	
	output{
		display inside type: 2d
		{
			graphics ""
			{
				draw circ;
			}
			species movingAgent aspect: classic;
		}
	}
}
