/**
* Name: testServer
* Based on the internal empty template. 
* Author: lucas
* Tags: 
*/


model testServer

/* Insert your model definition here */

global skills: [ProxySkill]
{
	init
	{
		do startServer;
	}
}

experiment testServer
{
}