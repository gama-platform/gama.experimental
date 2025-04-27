package endActionProxy;

import java.util.List;

import MPISkill.MPIFunctions;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.MinimalAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.statements.IExecutable;
import mpi.MPIException;
import proxy.ProxyAgent;
import proxySkill.ProxyFunctions;
import synchronizationMode.HardSyncMode;


/**
 * EndActionProxy
 * 
 * Action aimed to be executed between every turn of a distributed simulation
 * 
 * Example : sending agents in OLZ to neighbors, update agents in OLZ...
 *
 */

public class EndActionOneShotProxyUpdate implements IExecutable 
{

	static
	{
		//DEBUG.ON();
	}
	
	IMap<Integer, List<?>> proxyToUpdate;
	
	public EndActionOneShotProxyUpdate(IMap<Integer, List<?>> proxyToUpdate)
	{
		DEBUG.OUT("EndActionOneShotProxyUpdate created");
		this.proxyToUpdate = proxyToUpdate;
	}
	
	@Override
	public Object executeOn(IScope scope) throws GamaRuntimeException 
	{
		removeBuggedAgent();
		IMap<Integer, IList<?>> result = sendAgentToUpdate(scope);
		updateReceivedAgent(scope, result);
		
		return null;
	}
	
	// TODO fix bug then remove this
	private void removeBuggedAgent()
	{
		DEBUG.OUT("start EndActionOneShotProxyUpdate start ");
		if(proxyToUpdate != null)
		{	
			DEBUG.OUT("Proxy to send update to " + proxyToUpdate);
			for(var listOfProxys : proxyToUpdate.entrySet())
			{			
				DEBUG.OUT("proxyList proc index to send update to : " + listOfProxys.getKey());
				DEBUG.OUT("proxyList Listof agent to update : " + listOfProxys.getValue());
				
				List<?> valueList = listOfProxys.getValue();
				if(valueList.removeIf(agent -> agent instanceof MinimalAgent))
				{
					DEBUG.OUT("BGUYGUGUGUUGUGUGUUGUGUGUGUG");
				}
				if(valueList.removeIf(agent -> ((ProxyAgent)agent).getOrCreateAttributes().get("syncmode") != null && ((ProxyAgent)agent).getOrCreateAttributes().get("syncmode").equals(IKeyword.HARDSYNC)))
				{
					DEBUG.OUT("removed HardSyncMode from update gaent parce que c'est xommcme ça");
				}
			 	listOfProxys.setValue(valueList);  // Update the map with the modified list
				
				
				for(var proxy : listOfProxys.getValue())
				{
					if(proxy instanceof ProxyAgent pa)
					{
						DEBUG.OUT("proxy that wil send update : " + pa);
					
						((ProxyAgent)proxy).setAttribute(IKeyword.UUID, pa.getUUID().toString());
						DEBUG.OUT("proxy attribute that will be sent : " + pa.getAttributes(false));
					}
				}
			}
		}else
		{
			DEBUG.OUT("No proxy to send update to");
		}
	}
	
	/**
	 * sendAgentToUpdate : send agent with most recent data to all processor having a copy
	 * 
	 * @param scope
	 * @return received updated agent
	 */
	IMap<Integer, IList<?>> sendAgentToUpdate(IScope scope)
	{
		DEBUG.OUT("EndActionOneShotProxyUpdate MPI_ALLTOALLVMPI_ALLTOALLV : " + proxyToUpdate);
		return MPIFunctions.MPI_ALLTOALLV(scope, proxyToUpdate);
	}
	
	
	// TODO this could possibly fail if the copy died in the current cycle
	/**
	 * updateReceivedAgent : update the attribute of the copied agent on this processor
	 * 
	 * @param scope
	 * @param agentToUpdate
	 */
	private void updateReceivedAgent(IScope scope, IMap<Integer, IList<?>> agentToUpdate)
	{
		if(agentToUpdate != null && agentToUpdate.size() > 0)
		{
			for(var processor : agentToUpdate.entrySet())
			{
				for(var copiedAgent : processor.getValue())
				{
					if(copiedAgent instanceof ProxyAgent pa)
					{
						DEBUG.OUT("need to update agent("+copiedAgent+")");
						DEBUG.OUT("need to update agent class ("+copiedAgent.getClass()+")");
						DEBUG.OUT("need to update agent with hashcode ("+((IAgent)copiedAgent).getUUID()+")");
						DEBUG.OUT("proxy attribute that will be updated : " + ((ProxyAgent)copiedAgent).getOrCreateAttributes());
						
						
						ProxyAgent proxyToUpdate = ProxyFunctions.getProxyFromAgent(scope, (IAgent) copiedAgent); // get agent on this proc
						DEBUG.OUT("proyxu pdate ???? ("+proxyToUpdate+")");
						DEBUG.OUT("proyxu pdate ???? synchroMode ("+proxyToUpdate.synchroMode+")");
						
						proxyToUpdate.synchroMode.updateAttributes((IAgent) copiedAgent); // update attributes with new data
					}else
					{
						DEBUG.OUT("BUGUGUGUGUGUGUGUG"); // should not happen
					}
				}
			}
		}
		
	}
}
