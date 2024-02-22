package proxySkill;

import java.util.ArrayList;
import java.util.List;

import HardSyncModeComm.HardSyncModeProducerConsumer;
import distributionExperiment.DistributionExperiment;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;
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
	
	@action (name = "addDistantAgentsToUpdate",
		args = { @arg (
				name = "ProxyAgents",
				type = IType.LIST,
				optional = false,
				doc = @doc ("Agent to add distant agent")),
				@arg (
					name = "SimulationID",
					type = IType.INT,
					optional = false,
					doc = @doc ("The simulation's ID where the agent to update is located"))
		},
	doc = @doc("Add SimulationID to procsWithDistantAgent of a local ProxyAgent"))
	public void addDistantAgentsToUpdate(IScope scope)
	{
		List<ProxyAgent> proxys = (List<ProxyAgent>) scope.getArg("ProxyAgents");
		DEBUG.OUT("addDistantAgentsToUpdate : " + proxys);
		int simulationID = (Integer) scope.getArg("SimulationID");
		
		for(var agentToAddAsDistant : proxys)
		{
			if(agentToAddAsDistant.getSynchroMode() instanceof LocalSynchronizationMode)
			{
				DEBUG.OUT("Adding " + simulationID + " to " + agentToAddAsDistant.getName());
				((LocalSynchronizationMode)agentToAddAsDistant.getSynchroMode()).addProcs(simulationID);
			}
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
		DEBUG.OUT("ProxyAgent hashcode in simulation(" + simulationID + ") : " + proxy.getHashCode());
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
		if(agent != null)
		{
			DEBUG.OUT("agent get class : " + agent);
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
		
		return ProxyFunctions.getProxy(scope, (IAgent)scope.getArg("agent")).toString();
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
			return ProxyPopulation.getProxyFromHashCode(agent.hashCode());
		}
		return null;
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
					ret = ret + "agent : " + agent + " :: " + ((ProxyAgent)agent).getHashCode() + "\n";
					DEBUG.OUT("agent : " + agent + " :: " + ((ProxyAgent)agent).getHashCode());
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

		List<Object> copiedAgents = ((DistributionExperiment)scope.getExperiment()).copiedProxyFromOther;
		DEBUG.OUT("agentsToCopy size : " + agentsToCopy.size());
		for(var auto : agentsToCopy.entrySet())
		{
			for(var copyAgent : auto.getValue())
			{
				if(copiedAgents != null && copiedAgents.contains(copyAgent))
				{
					DEBUG.OUT("FOUND A COPY " + copyAgent);
					continue; // skip this agent as it is already copied on this processor
				}
				DEBUG.OUT("auto 2222 : " + auto.getClass().toString());
				ProxyAgent proxy = ProxyFunctions.getProxy(scope, (IAgent) copyAgent);
				DEBUG.OUT("copyAgent  proxy: " + proxy);
			}
		}
		DEBUG.OUT("agentsToCopy : " + agentsToCopy);
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
				ProxyAgent proxy = ProxyFunctions.getProxy(scope, (IAgent) migratedAgent);
				DEBUG.OUT("migratedAgent  proxy: " + proxy);
			}
		}
		DEBUG.OUT("agentsToMigrateend : " + agentsToMigrate);
		((DistributionExperiment)scope.getExperiment()).proxyToMigrate = agentsToMigrate;
		DEBUG.OUT("end setting agentsToMigrate : " + ((DistributionExperiment)scope.getExperiment()).proxyToMigrate);
		
	}
	@action (name = "startServer")
	public void startServer(final IScope scope)
	{
		new HardSyncModeProducerConsumer(scope);
	}
	
	@action (name = "sendRead",
		args = { @arg (
			name = "agent",
			type = IType.AGENT,
			optional = false,
			doc = @doc ("get MinimalAgent of a proxy")),
			@arg (
				name = "rank",
				type = IType.INT,
				optional = false,
				doc = @doc ("get MinimalAgent of a proxy")),
			@arg (
				name = "rankOther",
				type = IType.INT,
				optional = false,
				doc = @doc ("get MinimalAgent of a proxy"))
			})
	public void sendRead(final IScope scope)
	{
		/*IAgent agent = (IAgent) scope.getArg("agent");
		int rankMPI = (int) scope.getArg("rank");
		int rankOther = (int) scope.getArg("rankOther");
		HardSyncRequestRunnable rq = new HardSyncRequestRunnable(HardSyncModeComm.RequestType.READ, agent.hashCode(), rankMPI, rankOther, "name");
		byte[] requestSerialized = rq.serializeObject();
		try {
			MPI.COMM_WORLD.send(requestSerialized, requestSerialized.length, MPI.BYTE, rankOther, IMPISkill.REQUEST_TYPE);
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // send request*/
	}
	
	public void sendWrite() 
	{
		
	}
}
