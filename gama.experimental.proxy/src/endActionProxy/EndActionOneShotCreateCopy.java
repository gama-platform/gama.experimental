package endActionProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import MPISkill.MPIFunctions;
import distributionExperiment.DistributionExperiment;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.MinimalAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.statements.IExecutable;
import mpi.MPI;
import mpi.MPIException;
import proxy.ProxyAgent;
import proxyPopulation.ProxyPopulation;
import proxySkill.ProxyFunctions;
import synchronizationMode.HardSyncMode;
import synchronizationMode.DistantSynchronizationMode;


/**
 * EndActionOneShotCreateCopy
 * 
 * Action aimed to be executed between every turn of a distributed simulation, this action aimed to send agent to be copied on other processor
 * Agent copied on this processor are not sent to other processor
 * 
 *
 */

public class EndActionOneShotCreateCopy implements IExecutable 
{

	static
	{
		DEBUG.ON();
	}
	
	IMap<Integer, List<?>> proxyToCopy;
	int current_step;
	
	public EndActionOneShotCreateCopy(IMap<Integer, List<?>> proxyToCopy, int current_step)
	{
		DEBUG.OUT("EndActionOneShotCreateCopy created " + current_step);
		this.proxyToCopy = proxyToCopy;
		this.current_step = current_step;
	}

	@Override
	public Object executeOn(IScope scope) throws GamaRuntimeException 
	{
		DEBUG.OUT("-----------------CreateCopy-------------------------------" + this.current_step + "------------------------------------------------");
		DEBUG.OUT("proxy to copy : " + proxyToCopy);
		
		replaceBuggedAgentBeforeSend(scope); // remove bugged agent TODO
		removeCopiedProxyFromProxyToCopy(scope); // remove all the copy from agent to send
		removeDuplicates(scope);
		DEBUG.OUT(scope);
		IMap<Integer, IList<?>> result = sendAgentToCopy(scope); // send agent to specific proc

		DEBUG.OUT("resultresultresultresult " + result);
		updateCopiedProxyFromOther(scope, result); // uppdate copiedProxyFromOther
		updateProxyToUpdate(scope); // update the list of proxy to update at each cyle
			
		return result; // returning the new agents copied this step
	}

	/**
	 * To be removed after fixing the bug of regular agent appearing when using spatial operator
	 * 
	 */
	private void replaceBuggedAgentBeforeSend(IScope scope)
	{		
		DEBUG.OUT("replaceBuggedAgentBeforeSend " + proxyToCopy);
		for(var auto : proxyToCopy.entrySet())
		{
			List<IAgent> agents = (List<IAgent>) auto.getValue();
			DEBUG.OUT("replaceBuggedAgentBeforeSend agents" + agents);
			for (int i = 0; i < agents.size(); i++) 
			{
				var agent = agents.get(i);
				DEBUG.OUT("cuurently on agent" + agents);
				if(agent instanceof MinimalAgent agt)
				{
					DEBUG.OUT("agent : " + agent + " is a MinimalAgent ");
					ProxyAgent proxy = ProxyFunctions.getProxyFromAgent(scope, agt);
					DEBUG.OUT("proxy of " + agent + " : " + proxy);
					agents.set(i, proxy);
				}else
				{
					DEBUG.OUT("agent : " + agent + " is a proxy ");
				}
			}
			proxyToCopy.put(auto.getKey(), agents);
			DEBUG.OUT("replaceBuggedAgentBeforeSend after " + auto.getValue());
		}
		DEBUG.OUT("replaceBuggedAgentBeforeSend riught? " + proxyToCopy);
	}
	
	/**
	 * removeCopiedProxyFromProxyToCopy : remove all element of values of copiedProxyFromOther from proxyToCopy
	 * as we don't want to send copied agent to other processor
	 * 
	 * @param scope
	 */
	private void removeCopiedProxyFromProxyToCopy(IScope scope)
	{
		IMap<Integer, IList<?>> copiedProxyFromOther = ((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther;
		
		if(copiedProxyFromOther == null){
			DEBUG.OUT("copiedProxyFromOther IS NULL");
			return;
		}
		DEBUG.OUT("copiedProxyFromOther " + copiedProxyFromOther);
		DEBUG.OUT("removeCopiedProxyFromProxyToCopy before " + proxyToCopy);
		
		for (var entry : copiedProxyFromOther.entrySet()) {
		    int key = entry.getKey();
		    List<?> valuesToRemove = entry.getValue();

		    // Get the corresponding list in the first map, creating it if needed
		    List<?> listInFirstMap = proxyToCopy.getOrDefault(key, GamaListFactory.create());

		    // Efficiently remove values using removeAll()
		    listInFirstMap.removeAll(valuesToRemove);

		    // If the list is now empty, remove the entry entirely
		    if (listInFirstMap.isEmpty()) {
		    	DEBUG.OUT("listInFirstMap removing " + key);
		    	proxyToCopy.remove(key);
		    } else {
		        // Update the modified list in the first map
		    	DEBUG.OUT("proxyToCopy.put " + listInFirstMap);
		    	proxyToCopy.put(key, (IList<?>) listInFirstMap);
		    }
		}
		DEBUG.OUT("removeCopiedProxyFromProxyToCopy end " + proxyToCopy);
	}
	
	
	private IMap<Integer, IList<?>> sendAgentToCopy(IScope scope)
	{
		ProxyPopulation.setCopyFlag(true); // all agent create from here are copy
		IMap<Integer, IList<?>> result = MPIFunctions.MPI_ALLTOALLV(scope, proxyToCopy); // send and receive agents, agents are instanciated at reception !!
		ProxyPopulation.setCopyFlag(false); // unset the flag
		
		DEBUG.OUT("RESULT OF COPY " + result);
		
		return result;
	}
	
	/**
	 * updateCopiedProxyFromOther : we may have received agent from other processors with the global communication, we need to add these agent to copiedProxyFromOther
	 * in that way, we won't send these agent to other processor later on.
	 * 
	 * 
	 * @param scope
	 * @param result
	 */
	private void updateCopiedProxyFromOther(IScope scope, IMap<Integer, IList<?>> result)
	{
		DEBUG.OUT("updateCopiedProxyFromOther " + result);
		IMap<Integer, IList<?>> newMap;
		if(((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther != null) // we already have agent copied from other processor so we have to add the recived agent from this step
		{
			DEBUG.OUT("copiedProxyFromOther != nll " + ((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther);
			newMap = ((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther;
		}else // we don't have copied agent before this step
		{
			DEBUG.OUT("copiedProxyFromOther == nll ");
			newMap = GamaMapFactory.create();
		}

		DEBUG.OUT("newMap before " + newMap);
		for(var newAgents : result.entrySet())
		{
			DEBUG.OUT("newAgents entry " + newAgents);
			if(newMap.get(newAgents.getKey()) != null)
			{
				DEBUG.OUT("newMap.get(newAgents.getKey()) != null");
				DEBUG.OUT("newAgents " + newAgents);
				newMap.put(newAgents.getKey(), newAgents.getValue());
			}else
			{
				DEBUG.OUT("nononononoon newMap.get(newAgents.getKey()) != null");
				/*DEBUG.OUT("newMapb4 " + newMap);
				newMap.put(newAgents.getKey(),(IList<?>)Stream.concat(newAgents.getValue().stream(), newMap.get(newAgents.getKey()).stream())
	                .collect(Collectors.toList()));
				DEBUG.OUT("newMapafter " + newMap);*/
				
				DEBUG.OUT("newMapb4 " + newMap);
				
				Integer key = newAgents.getKey();
				IList<?> newList = newAgents.getValue();

				IList<?> existingList = newMap.get(key);
				IList<?> mergedList = existingList != null ?
				(IList<?>) Stream.concat(newList.stream(), existingList.stream()).collect(Collectors.toList()) :
				    newList;

				newMap.put(key, mergedList);

				DEBUG.OUT("newMapafter " + newMap);
			}
		}
		
		DEBUG.OUT("newMap after " + newMap);
		((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther = newMap;
	}
	
	/**
	 * removeHardSyncFromProxyToCopy : removing HardSync agent from the proxyToCopy as they don't need to be updated
	 * 
	 * @param scope
	 */
	private void removeHardSyncFromProxyToUpdate(IScope scope)
	{
		if(proxyToCopy == null)
		{
			return;
		}
		IMap<Integer, List<?>> proxyToUpdate = ((DistributionExperiment)scope.getExperiment()).proxyToUpdate;
		DEBUG.OUT("removeHardSyncFromProxyToUpdate proxyToUpdate " + proxyToUpdate);
		for(var entry : proxyToUpdate.entrySet()) 
		{
			List<?> valueList = entry.getValue();
		  
			for(var agent : valueList)
			{
				if(((ProxyAgent)agent).synchroMode instanceof HardSyncMode)
				{
					DEBUG.OUT("we remove agent  ["+ ((ProxyAgent)agent) +"]" + ((ProxyAgent)agent).getUUID() + " because of HardSync Policy");
				}
			}
			valueList.removeIf(agent -> ((ProxyAgent)agent).synchroMode instanceof HardSyncMode);// Remove HardSync agent
			entry.setValue(valueList);  // Update the map with the modified list
		}
		
		((DistributionExperiment)scope.getExperiment()).proxyToUpdate = proxyToUpdate;
		DEBUG.OUT("removeHardSyncFromProxyToUpdate final proxyToUpdate " + proxyToUpdate);
	}
	
	/**
	 * 
	 * @param scope
	 */
	private void updateProxyToUpdate(IScope scope)
	{
		DEBUG.OUT("SETTING proxyToUpdate to " + proxyToCopy);
		if(((DistributionExperiment)scope.getExperiment()).proxyToUpdate != null) // we sent proxyToCopy to other processors, we now have to update them every step
		{
			DEBUG.OUT("proxyToCopy : " + proxyToCopy);
			DEBUG.OUT("((DistributionExperiment)scope.getExperiment()).proxyToUpdate : " + ((DistributionExperiment)scope.getExperiment()).proxyToUpdate);
			
			proxyToCopy.forEach((key, value) -> 
				((DistributionExperiment)scope.getExperiment()).proxyToUpdate.merge(key, value, (oldValue, newValue) -> 
					{	   
						return Stream.concat(oldValue.stream(), newValue.stream()).collect(Collectors.toList()); // merge proxyToUpdate and proxyToCopy
					}));
			
			DEBUG.OUT("((DistributionExperiment)scope.getExperiment()).proxyToUpdate after the ùerge : " + ((DistributionExperiment)scope.getExperiment()).proxyToUpdate);
			
		}else // we didntn't had any agent to update before this step
		{
			DEBUG.OUT("proxyToCopy : " + null);
			if(proxyToCopy != null) // we sent agents to be copied this step
			{
				DEBUG.OUT("proxyToCopy != null");
				((DistributionExperiment)scope.getExperiment()).proxyToUpdate = proxyToCopy; // we update proxyToUpdate with proxyToCopy
			}
		}
		removeHardSyncFromProxyToUpdate(scope); // we don't want to update hardSync agent
	}
	
	/**
	 * remove duplicates before sending
	 * 
	 * @param scope
	 */
	private void removeDuplicates(IScope scope) 
	{
		DEBUG.OUT("removeDuplicates " + proxyToCopy);
		for (var entry : proxyToCopy.entrySet()) 
		{
			DEBUG.OUT("entry before removing duplicats " + entry);
			// Create a new HashSet to store unique elements
			Set<?> uniqueList = new HashSet<>(entry.getValue());
			// Update the map value with the deduplicated list
			entry.setValue(new ArrayList<>(uniqueList));
			DEBUG.OUT("entry after removing duplicats " + uniqueList);
		}
	}
}
