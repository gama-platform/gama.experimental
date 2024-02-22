/**
* Name: proxyequals
* equals() between a proxyAgent and the minimalAgent associated to it
* Author: lucas
* Tags: 
*/


model proxyequals

global skills: [ProxySkill]
{
	init
	{
		create emptyAgent;
		
		agent proxy <- getProxy(emptyAgent[0]);
		write(getClass(proxy));
		
		agent minimal <- getMinimalAgent(emptyAgent[0]);
		write(getClass(minimal));
		
		
		assert(proxy = minimal); // proxy.equals(minimal)
		assert(minimal = proxy); // minimal.equals(proxy)
	}
}

species emptyAgent skills: [ProxySkill]
{
}


experiment equals
{
}

experiment equals_proxy type: proxy 
{
}

experiment equals_distribution type: distribution 
{
}