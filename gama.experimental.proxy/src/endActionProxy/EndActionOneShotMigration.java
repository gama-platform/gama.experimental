package endActionProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import MPISkill.MPIFunctions;
import distributionExperiment.DistributionExperiment;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.statements.IExecutable;
import proxy.ProxyAgent;
import proxySkill.ProxyFunctions;
import synchronizationMode.LocalSynchronizationMode;


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
		//DEBUG.ON();
	}
	
	IMap<Integer, List<?>> proxyToMigrate;
	int current_step;
	
	public EndActionOneShotMigration(IMap<Integer, List<?>> proxyToMigrate, int current_step)
	{
		DEBUG.OUT("EndActionOneShotMigration created " + current_step);
		this.proxyToMigrate = proxyToMigrate;
		this.current_step = current_step;
	}

	@Override
	public Object executeOn(IScope scope) throws GamaRuntimeException 
	{
		DEBUG.OUT("------------Migration------------------------------------" + this.current_step + "------------------------------------------------");
		DEBUG.OUT("proxy to migrate : " + proxyToMigrate);
		
		removeAgentsToMigrateFromAgentsToUpdate(scope); // update AgentsToUpdate
		IMap<Integer, IList<?>> result = migrateAgents(scope); // migrate and receive agents
		deleteMigratedAgent(scope); // delete migrated agent
		if(result.size() > 0)
		{
			setLocalSynchro(scope, result); // update the syncmode of newly migrated agents
			//updateCopiedFromOther(scope, result);
		}
		
		return result;
	}

	/**
	 * removeAgentsToMigrateFromAgentsToUpdate : update AgentsToUpdate by removing all agent we are going to migrate
	 * 
	 * @param scope
	 */
	private void removeAgentsToMigrateFromAgentsToUpdate(IScope scope)
	{
		DEBUG.OUT("start EndActionOneShotMigration start ");
		if(proxyToMigrate.size() > 0 )
		{			
			var map = ((DistributionExperiment)scope.getExperiment()).proxyToUpdate;
			
			DEBUG.OUT("map before removing : " + map);
			
			for(var entry : map.entrySet())
			{
				if(proxyToMigrate.get(entry.getKey()) != null)
				{
					map.get(entry.getKey()).remove(proxyToMigrate.get(entry.getKey()));
				}
			}
			DEBUG.OUT("map init after removing : " + map);
			((DistributionExperiment)scope.getExperiment()).proxyToUpdate = map;
			
		}else
		{
			DEBUG.OUT("nothing to migrate");
		}
		
		for(var auto : proxyToMigrate.entrySet())
		{
			for(var proxy : auto.getValue())
			{
				DEBUG.OUT("proxy : " + ((ProxyAgent)proxy).getSynchroMode());
			}
		}
	}
	
	/**
	 * migrateAgents : migrate and receive agent
	 * 
	 * @param scope
	 * @return
	 */
	private IMap<Integer, IList<?>> migrateAgents(IScope scope)
	{
		IMap<Integer, IList<?>> result = MPIFunctions.MPI_ALLTOALLV(scope, proxyToMigrate);
		if(result != null && result.size() > 0)
		{
			DEBUG.OUT("RESULT MIGRATION("+current_step+") : " + result);
		}
		
		return result; 
	}
	
	/**
	 * setDistantSynchro : change the synchromode of the newly migrated agent depending on their DSP (TODO)
	 * 
	 * 
	 * @param scope
	 * @param newlyMigratedAgent
	 */
	private void setLocalSynchro(IScope scope, IMap<Integer, IList<?>> newlyMigratedAgent)
	{
		for(var processor : newlyMigratedAgent.entrySet())
		{
			for(var agent : processor.getValue())
			{
				if(agent instanceof ProxyAgent pa)
				{
					
					DEBUG.OUT("agent : " + agent);
					DEBUG.OUT("proxy agent : " + pa);
					DEBUG.OUT("proxy type sycnfho : " + pa.getSynchroMode());	
					pa.setSynchronizationMode(new LocalSynchronizationMode(pa.getAgent()));
				}else
				{
					DEBUG.OUT("MIGRATION " + agent + " not a proxy");
				}
			}
		}
	}
	
	private void updateCopiedFromOther(IScope scope, IMap<Integer, IList<?>> result) {
		var copiedProxyFromOther = ((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther;
		
		DEBUG.OUT("copiedProxyFromOther before update " + copiedProxyFromOther);
		for(var entry : result.entrySet())
		{
			DEBUG.OUT("entry before update " + entry);
			if (copiedProxyFromOther != null && copiedProxyFromOther.containsKey(entry.getKey())) 	 
		 	{
				DEBUG.OUT("copiedProxyFromOther have entry " + entry.getKey());
				Set<?> setB = new HashSet<>(entry.getValue());  // Convert list B to a set for faster lookups
		        Set<?> difference = new HashSet<>(copiedProxyFromOther.getKeys());  // Copy list A to a set

				DEBUG.OUT("setB " + setB);
				DEBUG.OUT("difference " + difference);
				
		        difference.removeAll(setB);  // Remove elements in B from the difference set
				DEBUG.OUT("difference removeAll " + difference);
		        
		        copiedProxyFromOther.put(entry.getKey(), (IList<?>) new ArrayList(difference));
				DEBUG.OUT("copiedProxyFromOther " + copiedProxyFromOther.get(entry.getKey()));
	        }
		}
		((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther = copiedProxyFromOther;
		DEBUG.OUT("copiedProxyFromOther after update " + ((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther);
	}
	
	
	private void deleteMigratedAgent(IScope scope)
	{
		DEBUG.OUT("deleteMigratedAgent " + proxyToMigrate);
		for(var entry : proxyToMigrate.entrySet()) // these proxy have migrated, we need to set the remaining copy to a distant agent
		{
			DEBUG.OUT("deleteMigratedAgent entry " + entry.getValue());
			for(var migratedAgent : entry.getValue())
			{
				ProxyAgent proxy = ProxyFunctions.getProxyFromAgent(scope, (IAgent) migratedAgent);

				DEBUG.OUT("deleting proxy " + proxy);
				proxy.primDie(scope);
			}
		}
	}
}
