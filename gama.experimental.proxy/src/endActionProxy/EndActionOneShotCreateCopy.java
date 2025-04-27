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
		//DEBUG.ON();
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
		
		removeDuplicates(scope); // todo : check and remove this function
		DEBUG.OUT(scope);
		
		for(var auto : proxyToCopy.entrySet())
		{
			DEBUG.OUT("proxy to copy list : " + auto);
			for(var agent : auto.getValue())
			{
				DEBUG.OUT("proxy to copy sending " + agent + " :: " + ((ProxyAgent)agent).getAttributes(false));
				DEBUG.OUT("proxy to copy location " + agent + " :: " + ((ProxyAgent)agent).getLocation());
			}
		}
		
		IMap<Integer, IList<?>> result = sendAgentToCopy(scope); // send agent to specific proc

		DEBUG.OUT("resultresultresultresult " + result);
		
		for(var auto : result.entrySet())
		{
			DEBUG.OUT("result proxy to copy list : " + auto);
			for(var agent : auto.getValue())
			{
				DEBUG.OUT("result sending " + agent + " :: " + ((ProxyAgent)agent).getAttributes(false));
				DEBUG.OUT("result location " + agent + " :: " + ((ProxyAgent)agent).getLocation());
			}
		}
		updateProxyToUpdate(scope); // update the list of proxy to update at each cyle
			
		return result; // returning the new agents copied this step
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
