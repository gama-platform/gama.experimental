package endActionProxy;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import MPISkill.MPIFunctions;
import distributionExperiment.DistributionExperiment;
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

public class EndActionOneShotCreateCopy implements IExecutable 
{

	static
	{
		DEBUG.ON();
	}
	
	IMap<Integer, List<?>> proxyToCopy;
	int index;
	
	public EndActionOneShotCreateCopy(IMap<Integer, List<?>> proxyToCopy, int index)
	{
		DEBUG.OUT("EndActionOneShotCreateCopy created " + index);
		this.proxyToCopy = proxyToCopy;
		this.index = index;
	}

	@Override
	public Object executeOn(IScope scope) throws GamaRuntimeException 
	{
		// todo don't send copied agent
		
		if(proxyToCopy.size() > 0 )
		{			
			DEBUG.OUT("-----------------CreateCopy-------------------------------" + this.index + "------------------------------------------------");
			DEBUG.OUT("proxy to copy ; " + proxyToCopy);
			
			for(var auto : proxyToCopy.entrySet())
			{
				
				for(var autoLi : auto.getValue())
				{	
					DEBUG.OUT("name of agents to copy : " + ((ProxyAgent)autoLi).getName());	
					DEBUG.OUT("synch mod of agents to copy : " + ((ProxyAgent)autoLi).getSynchroMode());	
					
					((ProxyAgent)autoLi).setAttribute(IKeyword.HASHCODE, ((ProxyAgent)autoLi).getHashCode());
					DEBUG.OUT("proxy attribute that will be sent : " + ((ProxyAgent)autoLi).getAttributes(false));
				}
			}
			
			
		}else
		{
			DEBUG.OUT("nothing to copy");
		}
		DEBUG.OUT("start EndActionOneShotCreateCopy start " + this.index);
		try {
			DEBUG.OUT("EndActionOneShotCreateCopy MPI_ALLTOALLVMPI_ALLTOALLV");

			
			for(var auto : proxyToCopy.entrySet())
			{
				for(var autoLi : auto.getValue())
				{	
					DEBUG.OUT("proxy attribute22222 that will be sent : " + ((ProxyAgent)autoLi).getAttributes(false));
				}
			}
			IList<?> result = MPIFunctions.MPI_ALLTOALLV(scope, proxyToCopy);
			if(result != null && result.size() > 0)
			{
				DEBUG.OUT("RESULT COPY("+index+") : " + result);
				for(var copiedAgent : result)
				{
					ProxyAgent proxy = ProxyFunctions.getProxy(scope, (IAgent) copiedAgent);
					DEBUG.OUT("SETTING PROXY("+proxy+") as distant");
					DEBUG.OUT("zadhbjzgdjahzjda("+proxy.getHashCode()+")");
					DEBUG.OUT("copied agent attributes : " + proxy.getAttributes(false));
					
					ProxyFunctions.setAgentAsDistant(scope, (IAgent) copiedAgent);
					
					DEBUG.OUT("DISTANT PROXY("+proxy+")");
					DEBUG.OUT("DISTANT PROXY MODE ("+proxy.synchroMode+")");
				}
			}
			
			if(((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther != null)
			{
				((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther.addAll((Collection<?>) result);
			}else
			{
				((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther = (List<Object>) result;
			}
			
			DEBUG.OUT("SETTING proxyToUpdate to " + proxyToCopy);
			if(((DistributionExperiment)scope.getExperiment()).proxyToUpdate != null)
			{
				DEBUG.OUT("proxyToCopy : " + proxyToCopy);
				DEBUG.OUT("((DistributionExperiment)scope.getExperiment()).proxyToUpdate : " + ((DistributionExperiment)scope.getExperiment()).proxyToUpdate);
				
				proxyToCopy.forEach((key, value) -> ((DistributionExperiment)scope.getExperiment()).proxyToUpdate.merge(key, value, (oldValue, newValue) -> {	   
					return Stream.concat(oldValue.stream(), newValue.stream()).collect(Collectors.toList()); // merge proxyToUpdate and proxyToCopy
				}));
				
				DEBUG.OUT("((DistributionExperiment)scope.getExperiment()).proxyToUpdate after the ùerge : " + ((DistributionExperiment)scope.getExperiment()).proxyToUpdate);
				
			}else
			{
				((DistributionExperiment)scope.getExperiment()).proxyToUpdate = proxyToCopy; // empty
			}
			
			
			return result;
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
