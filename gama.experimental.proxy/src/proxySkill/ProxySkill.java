package proxySkill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import MPISkill.IMPISkill;
import distributionExperiment.DistributionExperiment;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.MinimalAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.extension.serialize.gaml.SerialisationOperators;
import gama.extension.serialize.implementations.BinarySerialisation;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;
import mpi.MPI;
import proxy.ProxyAgent;
import proxyPopulation.ProxyPopulation;
import synchronizationMode.LocalSynchronizationMode;

@skill (name = "ProxySkill",
		doc = @doc ("Skill to test ProxyAgent behavior"))
public class ProxySkill extends Skill 
{
	static 
	{
		DEBUG.ON();
	}
	
	@SuppressWarnings("unchecked")
	@action (
		name = "setAgentsAsDistant",
		args = {
			@arg (
				name = "agentsToSetAsDistantAgent",
				type = IType.LIST,
				optional = false,
				doc = @doc ("Set the list of agents as distant agents, their proxyAgent will now update their attributes according to the politic of their proxy"))})
	public List<IAgent> setAgentsAsDistantAction(final IScope scope)
	{
		DEBUG.OUT(scope.getArg("agentsToSetAsDistantAgent"));
		List<IAgent> agentsToSetAsDistant = (List<IAgent>)scope.getArg("agentsToSetAsDistantAgent");
		for(var agentToSetAsDistant : agentsToSetAsDistant)
		{			
			ProxyFunctions.setAgentAsDistant(scope, agentToSetAsDistant);
		}
		return agentsToSetAsDistant;
	}
	
	@action (
		name = "setAgentAsDistant",
		args = {
			@arg (
				name = "agentToSetAsDistantAgent",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("Set this agent as a distant agent, his proxyAgent will now update his attributes according to the politic of this proxy"))})
	public IAgent setAgentAsDistantAction(final IScope scope)
	{
		IAgent agentToSetAsDistant = (IAgent) scope.getArg("agentToSetAsDistantAgent");
		ProxyFunctions.setAgentAsDistant(scope, agentToSetAsDistant);
		DEBUG.OUT("postsetDistant skill");
		return agentToSetAsDistant;
	}
	
	@action (
		name = "updateProxys",
		args = { @arg (
					name = "AgentsWithData",
					type = IType.LIST,
					optional = false,
					doc = @doc ("list of ProxyAgent to update"))})
	public void updateProxysAction(final IScope scope)
	{
		DEBUG.OUT("agentsToSetAsDistantAgent : " + scope.getArg("agentsToSetAsDistantAgent"));
		List<IAgent> agentsWithData = (List<IAgent>)scope.getArg("AgentsWithData");
		DEBUG.OUT("agentsWithData : " + agentsWithData);
		
		for(var agentWithData : agentsWithData)
		{			
			ProxyFunctions.updateProxy(scope, agentWithData);
		}
		return;
	}
	
	@action (
		name = "updateProxy",
		args = { @arg (
					name = "AgentWithData",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("ProxyAgent to update"))})
	public void updateProxyAction(final IScope scope)
	{
		ProxyFunctions.updateProxy(scope, (IAgent) scope.getArg("AgentWithData"));
	}

	@SuppressWarnings("unchecked")
	@action (
			name = "createCopyAgents",
			args = { @arg (
						name = "agentToCopy",
						type = IType.LIST,
						optional = false,
						doc = @doc ("Agents to create in current simulation"))},
			doc = @doc("create copy of the agents to this simulation"))
	public List<IAgent> createCopyAgentsAction(final IScope scope)
	{
		DEBUG.OUT("createCopyAgentsAction" + scope.getArg("agentToCopy"));
		List<IAgent> agentsToCopy = (List<IAgent>) scope.getArg("agentToCopy");
		DEBUG.OUT("agentsToCopy" + agentsToCopy);
		
		List<IAgent> newAgents = new ArrayList<IAgent>();
		for(var agentToCopy : agentsToCopy)
		{
			DEBUG.OUT("agentToCopy : " + agentToCopy);
			DEBUG.OUT("agentToCopy class " + agentToCopy.getClass());
			IAgent agent = ProxyFunctions.createCopyAgent(scope, agentToCopy);
			newAgents.add(agent);
		}
		DEBUG.OUT("newAgents" + newAgents);
		
		return newAgents;
	}
	
	@action (
		name = "createCopyAgent",
		args = { @arg (
					name = "agentToCopy",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("Agent to migrate to another simulation"))},
		doc = @doc("create copy of the agents to this simulation"))
	public IAgent createCopyAgentAction(final IScope scope)
	{
		return ProxyFunctions.createCopyAgent(scope, (IAgent) scope.getArg("agentToCopy"));
	}
	
	@action (name = "deleteDistant",
		args = { @arg (
				name = "distantToDelete",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("Agent to check if he is proxy"))})
	public void deleteDistant(IScope scope)
	{
		ProxyFunctions.deleteDistant(scope, (IAgent) scope.getArg("distantToDelete"));
	}
	
	@action (name = "deleteDistants",
			args = { @arg (
				name = "distantsToDelete",
				type = IType.LIST,
				optional = false,
				doc = @doc ("Agent to check if he is proxy"))})
	public void deleteDistants(IScope scope)
	{
		DEBUG.OUT("distantsToDelete " + scope.getArg("distantsToDelete"));
		List<IAgent> agentsToDelete = (List<IAgent>) scope.getArg("distantsToDelete");
		DEBUG.OUT(agentsToDelete);
		
		for(var agentToDelete : agentsToDelete)
		{
			ProxyFunctions.deleteDistant(scope, agentToDelete);
		}
	}

	@action (name = "checkHashCode",
		args = { @arg (
				name = "ProxyAgent",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("Agent to check HashCode")),
				@arg (
					name = "SimulationID",
					type = IType.INT,
					optional = false,
					doc = @doc ("The simulation's ID where the agent is from"))
		},
	doc = @doc("Display the HashCode of a ProxyAgent in the Eclipse console"))
	public void checkHashCode(IScope scope)
	{
		ProxyAgent proxy = (ProxyAgent) scope.getArg("ProxyAgent");
		int simulationID = (Integer) scope.getArg("SimulationID");
		
		DEBUG.OUT("\n");
		DEBUG.OUT("ProxyAgent hashcode in simulation(" + simulationID + ") : " + proxy.getUUID());
		DEBUG.OUT("\n");
		
		var agent = ProxyFunctions.getProxyFromAgent(scope, proxy.getAgent());
		DEBUG.OUT("agentagentagentagent : " + agent);
	}
	
	@action (name = "isProxy",
		args = { @arg (
				name = "testProxy",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("Agent to check if he is proxy"))})
	public String isProxy(IScope scope)
	{
		String agentName = ((IAgent)scope.getArg("testProxy")).getName();
		DEBUG.OUT("ISPROXY : " + "agent(" + agentName + ") : " + scope.getArg("testProxy").getClass());
		return "agent(" + agentName + ") : " + scope.getArg("testProxy").getClass();
	}
	
	@action (name = "printSyncMode",
		args = { @arg (
				name = "agent",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("Agent to check if he is proxy"))})
	public void printSyncMode(IScope scope)
	{
		DEBUG.OUT("printSyncMode class : " + ((IAgent)scope.getArg("agent")).getClass());
		ProxyAgent agent = ((ProxyAgent)scope.getArg("agent"));
		if(agent != null)
		{
			DEBUG.OUT("agent sync mode : " + agent + "sync(" + agent.getSynchroMode().getClass());
		}
	}
	
	@action (name = "getClass",
		args = { @arg (
				name = "agent",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("Agent to check if he is proxy"))})
	public String getClass(IScope scope)
	{
		IAgent agent = ((IAgent)scope.getArg("agent"));
		DEBUG.OUT("getClassgetClass agent : " + agent);
		if(agent != null)
		{
			DEBUG.OUT("agent get class : " + agent);
			ProxyFunctions.getProxyFromAgent(scope, agent).toString();
			return ((IAgent)scope.getArg("agent")).getClass().toString();
		}
		return "";
	}
	
	@action (name = "hasProxy",
		args = { @arg (
				name = "agent",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("Agent to check if he is proxy"))})
	public String hasProxy(IScope scope)
	{
		DEBUG.OUT("hasProxy class : " + ((IAgent)scope.getArg("agent")).getClass());
		
		return ProxyFunctions.getProxyFromAgent(scope, (IAgent)scope.getArg("agent")).toString();
	}
	
	@action (name = "getProxy",
		args = { @arg (
				name = "agent",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("get the proxy of an agent"))})
	public ProxyAgent getProxy(IScope scope)
	{
		IAgent agent = ((IAgent)scope.getArg("agent"));
		
		DEBUG.OUT("getProxy : " + agent);
		
		
		if(agent instanceof ProxyAgent)
		{
			DEBUG.OUT("agent instanceof ProxyAgent : " + agent);
			return (ProxyAgent)agent;
		}
		if(ProxyPopulation.getMapProxyID() != null)
		{
			DEBUG.OUT("ProxyPopulation.getMapProxyID(: " + agent);
			return ProxyPopulation.getProxyFromHashCode(agent.getUUID());
		}
		return null;
	}
	
	@action (name = "getSynchro",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("get the sycnhromode of an agent"))})
		public String getSynchro(IScope scope)
		{
			IAgent agent = ((IAgent)scope.getArg("agent"));
			
			DEBUG.OUT("getSynchro for : " + agent);
			
			
			if(agent instanceof ProxyAgent)
			{
				DEBUG.OUT("agent instanceof ProxyAgent : " + agent);
				return ((ProxyAgent)agent).synchroMode.getClass().toGenericString();
			}
			if(ProxyPopulation.getMapProxyID() != null)
			{
				DEBUG.OUT("ProxyPopulation.getMapProxyID(: " + agent);
				return ProxyPopulation.getProxyFromHashCode(agent.getUUID()).synchroMode.getClass().toGenericString();
			}
			return "";
		}
	

	
	@action (name = "getMinimalAgent",
		args = { @arg (
				name = "agent",
				type = IType.AGENT,
				optional = false,
				doc = @doc ("get MinimalAgent of a proxy"))})
	public IAgent getMinimalAgent(IScope scope)
	{
		IAgent agent = ((IAgent)scope.getArg("agent"));
		DEBUG.OUT("getMinimalAgent agent(: " + agent);
		
		if(agent instanceof ProxyAgent)
		{
			DEBUG.OUT("agent instanceof ProxyAgent");
			return ((ProxyAgent)agent).getAgent();
		}
		return agent;
	}
	
	@action (name = "printPopulationState")
	public String printPopulationState(IScope scope)
	{
		String ret = "";
		var pops = scope.getSimulation().getMicroPopulations();
		ret = ret + "pops : " + pops + "\n";
		DEBUG.OUT("pops : " + pops);
		for(var pop : pops)
		{
			ret = ret + "pop : " + pop + "\n";
			DEBUG.OUT("pop : " + pop);
			for(var agent : pop)	
			{
				if(agent instanceof ProxyAgent)
				{
					ret = ret + "agent : " + agent + " :: " + ((ProxyAgent)agent).getUUID() + "\n";
					DEBUG.OUT("agent : " + agent + " :: " + ((ProxyAgent)agent).getUUID());
				}else
				{
					ret = ret + "agent : " + agent + "\n";
					DEBUG.OUT("agent : " + agent);
				}
			}
		}
		
		return ret;
	}
	
	@action (name = "testGetPopulationFor",
			args = { @arg (
					name = "agent",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("Agent to check if he is proxy"))})
	public IPopulation<? extends IAgent> testGetPopulationFor(IScope scope)
	{
		IAgent agent = (IAgent) scope.getArg("agent");
		final IPopulation<? extends IAgent> microPopulation = scope.getSimulation().getPopulationFor(agent.getGamlType().toString());
		return microPopulation;
	}
	
	@action (
		name = "agentsToCopy",
		args = {
			@arg (
				name = "agentsToCopy",
				type = IType.MAP,
				optional = false,
				doc = @doc ("Set the list of agents as distant agents, their proxyAgent will now update their attributes according to the politic of their proxy"))})
	public void agentsToCopy(final IScope scope)
	{
		DEBUG.OUT("agentsToCopy start : ");
		IMap<Integer, List<?>> agentsToCopy = (IMap<Integer, List<?>>)scope.getArg("agentsToCopy");
		
		for(var entry : agentsToCopy.entrySet())
		{
			List<IAgent> agentListProxy= ((List<IAgent>)entry.getValue()).stream().map(element -> {
                if (element instanceof MinimalAgent) {
                	DEBUG.OUT("swapped a minimal agent to his proxy");
                    return ProxyFunctions.getProxyFromAgent(scope, element); // swap minimal to ProxyAgent
                } else {
                    return element;
                }
            }).collect(Collectors.toList());
			
			boolean removedAny = agentListProxy.removeIf(each -> ((ProxyAgent)each).copy); // remove copied agant
			if(removedAny)
			{
				DEBUG.OUT("Removed copied agent from agntsToCopy");
			}
			
			entry.setValue(agentListProxy);
		}
		
		((DistributionExperiment)scope.getExperiment()).proxyToCopy = agentsToCopy;
	}
	
	@action (
		name = "agentsToMigrate",
		args = {
			@arg (
				name = "agentsToMigrate",
				type = IType.MAP,
				optional = false,
				doc = @doc ("Set the list of agents as distant agents, their proxyAgent will now update their attributes according to the politic of their proxy"))})
	public void agentsToMigrate(final IScope scope)
	{
		DEBUG.OUT("agentsToMigrate start : ");
		IMap<Integer, List<?>> agentsToMigrate = (IMap<Integer, List<?>>)scope.getArg("agentsToMigrate");

		DEBUG.OUT("agentsToMigrate size : " + agentsToMigrate.size());
		DEBUG.OUT("agentsToMigrate : " + agentsToMigrate);
		for(var auto : agentsToMigrate.entrySet())
		{
			for(var migratedAgent : auto.getValue())
			{
				DEBUG.OUT("auto migratedAgent : " + auto.getClass().toString());
				ProxyAgent proxy = ProxyFunctions.getProxyFromAgent(scope, (IAgent) migratedAgent);
				DEBUG.OUT("migratedAgent  proxy: " + proxy);
			}
		}
		DEBUG.OUT("agentsToMigrateend : " + agentsToMigrate);
		((DistributionExperiment)scope.getExperiment()).proxyToMigrate = agentsToMigrate;
		DEBUG.OUT("end setting agentsToMigrate : " + ((DistributionExperiment)scope.getExperiment()).proxyToMigrate);
		
	}
	
	@action (
			name = "printHash",
			args = {
				@arg (
					name = "agent",
					type = IType.AGENT,
					optional = false,
					doc = @doc ("Set the list of agents as distant agents, their proxyAgent will now update their attributes according to the politic of their proxy"))})
		public void printHash(final IScope scope)
		{
			DEBUG.OUT("printHash start : ");
			IAgent agent = (IAgent) scope.getArg("agent");

			DEBUG.OUT("agentsToMigrate getUUID : " + agent.getUUID());
			if(agent instanceof ProxyAgent pa)
			{
				DEBUG.OUT(pa.getName() + " :: " + pa.getUUID());
			}
			
		}
	
	@action (
			name = "analyzeProxy",
			args = {
				@arg (
					name = "agents",
					type = IType.LIST,
					optional = false,
					doc = @doc ("Set the list of agents as distant agents, their proxyAgent will now update their attributes according to the politic of their proxy"))})
		public void analyzeProxy(final IScope scope)
		{
			IList<IAgent> agents = (IList<IAgent>) scope.getArg("agents");
			DEBUG.OUT("analyzeProxy " + agents);
			for(var agent : agents)
			{
				ProxyAgent proxy;
				if(agent instanceof MinimalAgent)
				{
					proxy = ProxyFunctions.getProxyFromAgent(scope, agent);
				}else
				{
					proxy = (ProxyAgent) agent;
				}
				
				DEBUG.OUT("proxy prox : " + agent);
				DEBUG.OUT("proxy agent name : " + agent.getAgent().getName());
			}
		}	
	
	@action (
			name = "testSerialize",
			args = {
				@arg (
					name = "agents",
					type = IType.LIST,
					optional = false,
					doc = @doc ("Set the list of agents as distant agents, their proxyAgent will now update their attributes according to the politic of their proxy"))})
		public void testSerialize(final IScope scope)
		{
			IList<IAgent> agents = (IList<IAgent>) scope.getArg("agents");
			DEBUG.OUT("testSerialize with this list of agents " + agents);
			String conversion = SerialisationOperators.serialize(scope, agents);
			final byte[] message = conversion.getBytes();
			
			DEBUG.OUT("serialize done with " + agents.size());	
			
			return;
		}
	
	static int[] computeDispl(int tasks, int[] buffSendSize)
    {
        int[] displs = new int[tasks];
        displs[0] = 0;
        
        StringBuilder str = new StringBuilder("computeDispl : \n");
        str.append("displs" + 0 + " :: " + displs[0]+ "\n");

        for(int index = 1; index < buffSendSize.length; index++)
        {
            str.append(index + " :: " + buffSendSize[index]+ "\n");
            displs[index] = displs[index-1] + buffSendSize[index-1];
            str.append("displs" + index + " :: " + displs[index]+ "\n");
        }
        //System.out.println(str);
        return displs;
    }
	
	
	@action (
			name = "testSerializeWithAlltoAll",
			args = {
					@arg (
							name = IMPISkill.MESG,
							type = IType.MAP,
							doc = @doc ("mesg message"))
			})
		public void testSerializeWithAlltoAll(final IScope scope)
		{

			final IMap<Integer, List<?>> msg = (IMap<Integer,  List<?>>) scope.getArg(IMPISkill.MESG, IType.MAP);
			
			DEBUG.OUT("MPI_ALLTOALLV " + msg);
			
			int my_rank = 0;
			int world_size = 4;
			
			try {
		        
		        DEBUG.OUT("my_rank : " + my_rank);
		        DEBUG.OUT("world_size : " + world_size);
		        
		        DEBUG.OUT("msg number of elem  : " + msg.length(scope));
		        DEBUG.OUT("msg size  : " + msg.size());
		        
		        DEBUG.OUT("msg.entrySet() : " + msg.entrySet());
		        for(var auto : msg.entrySet())
				{
		        	DEBUG.OUT("rank : " + auto.getKey());
					for(var copyAgent : auto.getValue())
					{
						DEBUG.OUT("agent to send : " + copyAgent);
					}
				}
		
		        
		        int bufferReceiveSize[] = new int[world_size]; // buffer to receive size of incoming buffer in allToAllv
		        int buffSendSize[] = new int[world_size]; // buffer to send size of incoming buffer to all
		        
		        List<byte[]> serializedMessage = new ArrayList<byte[]>();
		        
		        for(int index = 0; index < world_size; index++)
		        {
		        	if(msg.get(index) != null && msg.get(index).size() != 0)
		        	{
		        		
		        		String conversion = SerialisationOperators.serialize(scope, msg.get(index));
		        		//DEBUG.OUT("conversion: " +conversion);
		        		
		        		final byte[] message = conversion.getBytes();
		        		
		        		buffSendSize[index] = message.length;
		        		serializedMessage.add(message);
		        	}else
		        	{
		        		buffSendSize[index] = 0;
		        	}
		        }
		         
		        byte[] finalMessage = new byte[Arrays.stream(buffSendSize).sum()];
		        int offset = 0;
		        for (byte[] byteArray : serializedMessage) {
		            System.arraycopy(byteArray, 0, finalMessage, offset, byteArray.length);
		            offset += byteArray.length;
		        }
		        
				DEBUG.OUT("finalMessage lenght : " +finalMessage.length);
		        
		        int displsSend[] = computeDispl(world_size, buffSendSize); // displs of send buffer
				DEBUG.OUT("computeDispl displsSend ");
		
				/*DEBUG.OUT("1st all to all ");
		        MPI.COMM_WORLD.allToAll(buffSendSize, 1, MPI.INT, bufferReceiveSize, 1, MPI.INT); // send to all + receive from all size of incoming buffer
		
				DEBUG.OUT("bufferReceiveSize received : " + bufferReceiveSize.length);
				
		        int displsReceive[] = computeDispl(world_size, bufferReceiveSize); // displs of receive buffer
				DEBUG.OUT("computeDispl displsReceive ");
		        byte bufferReceiveData[] = new byte[Arrays.stream(bufferReceiveSize).sum()]; // buffer to receive data*/
				    
				/*DEBUG.OUT("bufferReceiveData");
		        MPI.COMM_WORLD.allToAllv(finalMessage, buffSendSize, displsSend, MPI.BYTE, bufferReceiveData, bufferReceiveSize, displsReceive, MPI.BYTE); // send to all + receive from all with different size
		        
		        IMap<Integer, IList<?>> ma = GamaMapFactory.create();
		        byte b1[];
		
				DEBUG.OUT("displsReceive.length : " + displsReceive.length);
		
		        int subBufferStart;
		        int subBufferEnd;
		        for(int index = 0; index < displsReceive.length; index++)
		        {        
		        	DEBUG.OUT("index " + index);
		        	DEBUG.OUT("displsReceive.length " + displsReceive.length);
		    	    if(index != displsReceive.length-1)
		            {
		                DEBUG.OUT("start displ["+index+"] from displ " + (displsReceive[index]));
		                DEBUG.OUT("end displ[" + (index+1) +"] " + (displsReceive[index+1]));         
		                subBufferStart = displsReceive[index];
		                subBufferEnd = displsReceive[index+1];
		           
		            }else
		            {           
		                subBufferStart = displsReceive[index];  
		            	subBufferEnd = bufferReceiveData.length;
		            }
		    		if(subBufferStart != subBufferEnd)
		    		{
		    	        IList<?> li = GamaListFactory.create();
		    			b1 = Arrays.copyOfRange(bufferReceiveData, subBufferStart, subBufferEnd);
		    			li.addAll((List)BinarySerialisation.createFromString(scope, new String(b1)));
		    			DEBUG.OUT("created li : " + li);
		    			
		    			ma.put(index, li);
		    		}
				}
		
				DEBUG.OUT("returning alltoall li : " + ma);
		        
				return ma;*/
	
			} catch (Exception e) {
				DEBUG.OUT("MPI_ALLTOALLV exception " + e);
				e.printStackTrace();
			} // rank of process
			
			return;
		}
}
