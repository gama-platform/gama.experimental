package proxyPopulation;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Iterables;

import gama.core.common.interfaces.IKeyword;
import gama.core.common.util.RandomUtils;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.agent.MinimalAgent;
import gama.core.metamodel.population.GamaPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.FlowStatus;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.gaml.compilation.IAgentConstructor;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.variables.IVariable;
import proxy.ProxyAgent;
import synchronizationMode.HardSyncMode;
import synchronizationMode.LocalSynchronizationMode;


// TODO notifier update code

/**
 * Population of Proxy Agent
 * 
 * @author Lucas Grosjean
 *
 */
@SuppressWarnings("serial")
public class ProxyPopulation extends GamaPopulation<ProxyAgent>
{
	
	static
	{
		DEBUG.ON();
	}
	
	static Map<String, ProxyAgent> hashMapProxyID;
	boolean emptyShell = true;
	static boolean copyFlag = false;
	int last;
	
	public ProxyPopulation(IMacroAgent host, ISpecies species) 
	{
		super(host, species);
		hashMapProxyID = new HashMap<String, ProxyAgent>();
	}
	
	@Override
	public IList<ProxyAgent> createAgents(final IScope scope, final int number,
			final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
			final boolean toBeScheduled, final RemoteSequence sequence) throws GamaRuntimeException
	{
		DEBUG.OUT("createAgents 1");
		DEBUG.OUT("createAgents initialValues " + initialValues);
		if (number == 0) return GamaListFactory.EMPTY_LIST;
		
		DEBUG.OUT("NEW getGamlType().getContentType() " + getGamlType().getContentType());
		final IList<MinimalAgent> agentList = GamaListFactory.create(getGamlType().getContentType(), number);
		final IAgentConstructor<IAgent> constr = species.getDescription().getAgentConstructor();
		
		for (int i = 0; i < number; i++) 
		{
			IShape shape;
			@SuppressWarnings ("unchecked") final IAgent agent = constr.createOneAgent(this, currentAgentIndex++);
			DEBUG.OUT("NEW NEW NEW NEW NEW HASHASH " + agent.getUUID());
			if (initialValues != null && !initialValues.isEmpty()) 
			{
				final Map<String, Object> init = initialValues.get(i);
				DEBUG.OUT("init attributes ::  " + init);
				
				if (init.containsKey(IKeyword.SHAPE)) 
				{
					final Object val = init.get(IKeyword.SHAPE);
					if (val instanceof GamaPoint) 
					{
						agent.setGeometry(new GamaShape((GamaPoint) val));
					} else 
					{
						agent.setGeometry((IShape) val);
					}
					init.remove(IKeyword.SHAPE);
				}else if (init.containsKey(IKeyword.LOCATION)) 
				{
					agent.setLocation(scope, (GamaPoint) init.get(IKeyword.LOCATION));
					init.remove(IKeyword.LOCATION);
				}
				
				if(init.containsKey(IKeyword.UUID))
				{
					DEBUG.OUT("found this in value init :  " + init.get(IKeyword.UUID));
					DEBUG.OUT("wh?, " + agent.getClass());
					agent.setUUID((String)init.get(IKeyword.UUID));
					DEBUG.OUT("hehehehehhehe " +agent.getUUID());
				}
			}
			agentList.add((MinimalAgent)agent); // no hashcode in the attributes
		}
		createVariablesForProxiedAgent(scope, agentList, initialValues, sequence);
		
		return createProxys(agentList, scope, sequence, isRestored, toBeScheduled);
	}
	
	@Override
	public ProxyAgent createAgentAt(final IScope scope, final int index, final Map<String, Object> initialValues,
			final boolean isRestored, final boolean toBeScheduled) throws GamaRuntimeException 
	{		
		DEBUG.OUT("ProxyAgent createAgentAt index " + index);
		DEBUG.OUT("ProxyAgent initialValues " + initialValues);
		DEBUG.OUT("ProxyAgent isRestored " + isRestored);
		
		final List<Map<String, Object>> mapInitialValues = new ArrayList<>();
		mapInitialValues.add(initialValues);

		final int tempIndexAgt = currentAgentIndex;

		currentAgentIndex = index;
		final IList<ProxyAgent> proxyList = createAgents(scope, 1, mapInitialValues, isRestored, toBeScheduled, null);
		currentAgentIndex = tempIndexAgt;

		return proxyList.firstValue(scope);
	}

	@Override
	public IList<ProxyAgent> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries) {
		DEBUG.OUT("createAgentAt2");
		final int number = geometries.length(scope);
		
		if (number == 0) return GamaListFactory.EMPTY_LIST;
	
		final IList<MinimalAgent> agentList = GamaListFactory.create(getGamlType().getContentType(), number);
		final IAgentConstructor<IAgent> constr = species.getDescription().getAgentConstructor();
		
		for (final IShape geom : geometries.iterable(scope)) 
		{
			final IAgent agent = constr.createOneAgent(this, currentAgentIndex++);
			agent.setGeometry(geom);
			agentList.add((MinimalAgent)agent);
		}
		
		createVariablesForProxiedAgent(scope, agentList, EMPTY_LIST, null);
		
		return createProxys(agentList, scope, null, false, false);
	}
	
	/**
	 * 
	 * Create proxys
	 * 
	 * @param agentList :  list of agent to create proxys for
	 * @param scope
	 * @param sequence
	 * @return
	 */
	private IList<ProxyAgent> createProxys(IList<MinimalAgent> agentList, IScope scope, RemoteSequence sequence, boolean isRestored, boolean isScheduled )
	{
		DEBUG.OUT("createProxys(");
		DEBUG.OUT("is restores ?? ? ?? ?? ? ? ? ? " + isRestored);
		final IList<ProxyAgent> proxyList = GamaListFactory.create(getGamlType().getContentType(), agentList.size());
		for (final MinimalAgent agent : agentList) {
			DEBUG.OUT("agrnt attirubte to give prox " + agent.getAttributes(false));
			ProxyAgent proxy;
			proxy = new ProxyAgent(agent, this, scope, copyFlag, agent.originalSimulationID);
			proxy.fixTopology();
			proxyList.add(proxy);
			DEBUG.OUT("New agent(" + agent.getName() + ") hashcode : " + agent.getUUID());
			hashMapProxyID.put(agent.getUUID().toString(), proxy);		
		}
		
		scheduleProxy(proxyList, scope, sequence, isRestored);
		this.addAll(proxyList);
		fireAgentsAdded(scope, proxyList);
		
		return proxyList;
	}
	
	/**
	 * 
	 * Schedule the Proxy Agent
	 * 
	 * @param proxyList : list of Proxy to schedule
	 * @param scope
	 * @param sequence
	 */
	private void scheduleProxy(IList<ProxyAgent> proxyList, IScope scope, RemoteSequence sequence, boolean isRestored)
	{
		if (!isRestored) {
			for(final ProxyAgent proxy : proxyList) 
			{
				proxy.schedule(scope);
			}
			
			if (sequence != null && !sequence.isEmpty()) {
				for (final IAgent proxy : proxyList) {
					if (!scope.execute(sequence, proxy, null).passed()
							|| scope.getAndClearBreakStatus() == FlowStatus.BREAK) {
						break;
					}
				}
			}
		}
		fireAgentsAdded(scope, proxyList);
	}
	
	/**
	 * Creates the variables for proxied agent.
	 *
	 * @param scope
	 *            the scope
	 * @param agents
	 *            the agents
	 * @param initialValues
	 *            the initial values
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("null")
	// TODO duplicate from createVariablesFor from GamaPopulation
	public void createVariablesForProxiedAgent(final IScope scope, final List<MinimalAgent> agents,
			final List<? extends Map<String, Object>> initialValues, RemoteSequence remote) throws GamaRuntimeException 
	{
		if (agents == null || agents.isEmpty()) return;
		final boolean empty = initialValues == null || initialValues.isEmpty();
		
		Map<String, Object> inits;
		for (int i = 0, n = agents.size(); i < n; i++) {
			final IAgent a = agents.get(i);
			inits = empty ? EMPTY_MAP : initialValues.get(i);
			for (final IVariable var : orderedVars) {
				final Object initGet =
						empty || !allowVarInitToBeOverridenByExternalInit(var) ? null : inits.get(var.getName());
				var.initializeWith(scope, a, initGet);
			}
			// Added to fix #3266 -- saves the values of the "extra" attributes found in the files
			if (!empty) {
				inits.forEach((name, v) -> { if (!orderedVarNames.contains(name)) { a.setAttribute(name, v); } });
			}
		}
	}

	@Override
	public void fireAgentRemoved(final IScope scope, final IAgent agent) {
		DEBUG.OUT("fireAgentRemoved " + agent);
		try {
			if(agent instanceof ProxyAgent pa)
			{
				if(pa.synchroMode instanceof HardSyncMode)
				{
					DEBUG.OUT("fireAgentRemoved : HARDSYNC " + pa);
					DEBUG.OUT("agent instanceof ProxyAgent ");
					ProxyAgent proxy = (ProxyAgent) agent;
					DEBUG.OUT("proxy found " + proxy);
					proxy.setSynchronizationMode(new LocalSynchronizationMode(proxy.getAgent()));
					this.remove(proxy);
				}
				
			}else
			{
				DEBUG.OUT("agent not instanceof ProxyAgent ");
				ProxyAgent proxy = getProxyFromHashCode(((MinimalAgent)agent).getUUID());
				DEBUG.OUT("getProxyFromHashCode " + proxy);
				if(proxy.synchroMode instanceof HardSyncMode)
				{
					DEBUG.OUT("fireAgentRemoved : HARDSYNC agent " + proxy);
					DEBUG.OUT("proxy found " + proxy);
					proxy.setSynchronizationMode(new LocalSynchronizationMode(agent));
					this.remove(proxy);
					return;
				}
				
				this.remove(proxy);

			}
		} catch (final RuntimeException e) {
			DEBUG.OUT("fireAgentRemoved RuntimeException " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Get proxy linked to the agent with the given hashcode
	 * 
	 * @param hashcode
	 * @return
	 */
	static public ProxyAgent getProxyFromHashCode(UUID uniqueID)
	{	
		DEBUG.OUT("getProxyFromHashCode :: " + uniqueID);
		ProxyAgent proxy = hashMapProxyID.get(uniqueID.toString());
		
		DEBUG.OUT("Proxy hashcode in the population :: ");
		for(var auto : hashMapProxyID.entrySet())
		{
			DEBUG.OUT(auto.getValue().getName() + " :: " + auto.getKey());
		}
		if(proxy!=null)
		{
			DEBUG.OUT("proxy from hashcode(" + uniqueID + ") : " + proxy.getName());
		}
		
		return proxy;
	}
	
	public static Map<String, ProxyAgent> getMapProxyID()
	{
		return ProxyPopulation.hashMapProxyID;
	}
	
	@Override
	public ProxyAgent anyValue(final IScope scope) 
	{	
		final RandomUtils r = scope.getRandom();
		List<String> keysAsArray = new ArrayList<String>(hashMapProxyID.keySet());
		DEBUG.OUT("keysAsArray" + " :: " + keysAsArray.size());
		
		for(var auto : ProxyPopulation.hashMapProxyID.entrySet())
		{
			DEBUG.OUT("agents in map Proxy" + " :: " + auto.getKey() + " :: " + auto.getValue());
		}
		if(keysAsArray.size() > 0)
		{
			var auto = hashMapProxyID.get(keysAsArray.get(r.between(0, keysAsArray.size() - 1)));
			DEBUG.OUT("returning  : " + auto);
			// TODO FIX HERE
			return auto;
		}
		
		return null;
	}
	
	@SuppressWarnings ("unchecked")
	@Override
	public ProxyAgent getOrCreateAgent(final IScope scope, Integer index, Map<String, Object> initValues) 
	{
		DEBUG.OUT("getOrCreateAgentgetOrCreateAgentgetOrCreateAgentgetOrCreateAgent current index " + this.currentAgentIndex);
		if(emptyShell)
		{
			DEBUG.OUT("emptyShell true " + emptyShell);
			DEBUG.OUT("emptyShell initValues " + initValues);
			emptyShell = false;
			last = currentAgentIndex;
			
			DEBUG.OUT("creat empty shell at index " + last);
			//ProxyAgent agt = createAgentAt(scope, currentAgentIndex, initValues, false, true);
			DEBUG.OUT("empty shell created" + last);
			//return agt;
			return null;
		}else
		{
			DEBUG.OUT("emptyShell false " + emptyShell);
			emptyShell = true;
			
			DEBUG.OUT("initValues empty " + initValues);
			String myUUID = (String) initValues.get(IKeyword.UUID);

			DEBUG.OUT("looking for hash " + myUUID + " in pop " + this);
			DEBUG.OUT("pop we azrfe looking into " + this.size());
			
			for(ProxyAgent auto : this)
			{
				DEBUG.OUT("agent in pop :  " + auto.getOrCreateAttributes() + " :: " + auto.getUUID().toString());
				if(auto.getUUID() == null)
				{
					DEBUG.OUT("ONE AGENT IN POP DONT HAVE UUID " + auto);
					break;
				}
				
				if(myUUID == null)
				{
					DEBUG.OUT("LY AGENT DONT HAVE UUID " + this);
					break;
				}
						
				if(myUUID.equals(auto.getUUID().toString()))
				{				
					DEBUG.OUT("WE FOUND THE RIGHT AGENT at index : " + auto.getIndex());				
					DEBUG.OUT("WE FOUND THE RIGHT AGENT at index : " + auto.getOrCreateAttributes());
					DEBUG.OUT("WE FOUND THE RIGHT AGENT : " + auto);

					DEBUG.OUT("number of agent " + this.size());
					
					//this.get(last).primDie(scope); // we kill the empty shell because we found the right agent to recreate in
					//this.remove(last);
					
					//this.currentAgentIndex--;
					//this.last = this.currentAgentIndex;
					
					return auto;
				}
			}
			
			DEBUG.OUT("Agent don't exist so we create it " + last);
			
			ProxyAgent proxy = createAgentAt(scope, last, initValues, false, true);
			DEBUG.OUT("resuklt of created agent :  " + proxy.getIndex());
			
			DEBUG.OUT("number of agent " + this.size());
			for(ProxyAgent auto : this)
			{
				DEBUG.OUT("agent in after pop :  " + auto.getOrCreateAttributes());
				DEBUG.OUT("agent in after index :  " + auto.getIndex());
				DEBUG.OUT("agent in after name :  " + auto.getName());
			}
			

			
			return proxy;
		}
	}
	
	@Override
	public ProxyAgent getAgent(final Integer index) {
		DEBUG.OUT("getAgent proxy override " + index);	
		ProxyAgent pro = Iterables.find(this, each -> each.getIndex() == index, null);
		DEBUG.OUT("pro pro pro " + pro);
		return pro;
	}
	
	public static void setCopyFlag(boolean copyFlagValue)
	{
		copyFlag = copyFlagValue;
	}
}
