package endActionProxy;

import java.util.List;

import MPISkill.MPIFunctions;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.statements.IExecutable;
import mpi.MPIException;
import proxy.ProxyAgent;
import proxySkill.ProxyFunctions;


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
		DEBUG.ON();
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

		DEBUG.OUT("start EndActionOneShotProxyUpdate start ");
		if(proxyToUpdate != null)
		{	
			DEBUG.OUT("Proxy to send update to " + proxyToUpdate);
			for(var listOfProxys : proxyToUpdate.entrySet())
			{			
				DEBUG.OUT("proxyList proc index to send update to : " + listOfProxys.getKey());
				DEBUG.OUT("proxyList Listof agent to update : " + listOfProxys.getValue());
				
				for(var proxy : listOfProxys.getValue())
				{
					DEBUG.OUT("proxy that wil send update : " + proxy);
					
					((ProxyAgent)proxy).setAttribute(IKeyword.HASHCODE, ((ProxyAgent)proxy).getHashCode());
					DEBUG.OUT("proxy attribute that will be sent : " + ((ProxyAgent)proxy).getAttributes(false));
					
				}
				
				// ask proxys if update required
				// send agent to update
			}
		}else
		{
			DEBUG.OUT("No proxy to send update to");
		}
		
		try {
			DEBUG.OUT("EndActionOneShotProxyUpdate MPI_ALLTOALLVMPI_ALLTOALLV : " + proxyToUpdate);
			IList<?> result = MPIFunctions.MPI_ALLTOALLV(scope, proxyToUpdate);
			if(result != null && result.size() > 0)
			{
				for(var copiedAgent : result)
				{
					DEBUG.OUT("need to update agent("+copiedAgent+")");
					DEBUG.OUT("proxy attribute that will be updated : " + ((ProxyAgent)copiedAgent).getAttributes(false));
					//getProxyFromHashCode()
					ProxyAgent proxyToUpdate = ProxyFunctions.getProxy(scope, (IAgent) copiedAgent);
					DEBUG.OUT("proyxu pdate ???? ("+proxyToUpdate+")");
					DEBUG.OUT("proyxu pdate ???? synchroMode ("+proxyToUpdate.synchroMode+")");
					proxyToUpdate.synchroMode.updateAttributes((IAgent) copiedAgent);
					// todo update agent with correct data
				}
			}
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
