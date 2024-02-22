package endActionProxy;

import java.util.List;

import MPISkill.MPIFunctions;
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

public class EndActionOneShotMigration implements IExecutable 
{

	static
	{
		DEBUG.ON();
	}
	
	IMap<Integer, List<?>> proxyToMigrate;
	int index;
	
	public EndActionOneShotMigration(IMap<Integer, List<?>> proxyToMigrate, int index)
	{
		DEBUG.OUT("EndActionOneShotMigration created " + index);
		this.proxyToMigrate = proxyToMigrate;
		this.index = index;
	}

	@Override
	public Object executeOn(IScope scope) throws GamaRuntimeException 
	{
		if(proxyToMigrate.size() > 0 )
		{			
			DEBUG.OUT("------------Migration------------------------------------" + this.index + "------------------------------------------------");
			DEBUG.OUT("proxy to migrate ; " + proxyToMigrate);
			
			/*var map = ((DistributionExperiment)scope.getExperiment()).proxyToUpdate;
			
			DEBUG.OUT("map before removing : " + map);
			
			for(var entry : map.entrySet())
			{
				if(proxyToMigrate.get(entry.getKey()) != null)
				{
					map.get(entry.getKey()).remove(proxyToMigrate.get(entry.getKey()));
				}
			}
			DEBUG.OUT("map init after removing : " + map);
			((DistributionExperiment)scope.getExperiment()).proxyToUpdate = map;*/
			
		}else
		{
			DEBUG.OUT("nothing to migrate");
		}
		
		DEBUG.OUT("start EndActionOneShotMigration start " + this.index);
		try {
			DEBUG.OUT("nEndActionOneShotMigration MPI_ALLTOALLVMPI_ALLTOALLV");
			
			for(var auto : proxyToMigrate.entrySet())
			{
				for(var proxy : auto.getValue())
				{
					DEBUG.OUT("proxy : " + ((ProxyAgent)proxy).getSynchroMode());
				}
			}
			
			IList<?> result = MPIFunctions.MPI_ALLTOALLV(scope, proxyToMigrate);
			if(result != null && result.size() > 0)
			{
				DEBUG.OUT("RESULT MIGRATION("+index+") : " + result);
			}

			for(var auto : result)
			{
				DEBUG.OUT("auto : " + auto);
				DEBUG.OUT("proxy auto : " + ((ProxyAgent)auto));
				DEBUG.OUT("proxy type sycnfho : " + ((ProxyAgent)auto).getSynchroMode());
				//((ProxyAgent)auto).setSynchronizationMode(new LocalSynchronizationMode((IAgent)auto));
			}
			
			DEBUG.OUT("List of agent to set as distant : ");
			for(var entry : proxyToMigrate.entrySet()) // these proxy have migrated, we need to set the remaining copy to a distant agent
			{
				for(var migratedAgent : entry.getValue())
				{
					ProxyAgent proxy = ProxyFunctions.getProxy(scope, (IAgent) migratedAgent);
					DEBUG.OUT("SETTING PROXY("+proxy+") as distant");
					//ProxyFunctions.setAgentAsDistant(scope, proxy);
				}
			}
			
			return result;
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
