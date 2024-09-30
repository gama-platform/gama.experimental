package hardSyncModeComm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import MPISkill.IMPISkill;
import MPISkill.MPIFunctions;
import distributionExperiment.DistributionExperiment;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.species.ISpecies;
import proxy.ProxyAgent;
import synchronizationMode.HardSyncMode;

/**
 * Request for HardSyncMode communication :
 * 
 * 1) Read : Client : send to server a request to read a given attribute Server
 * : receive a request to send a given attribute to the sender
 * 
 * 2) Write : Client : send to server a request to change a given attribute to a
 * given value Server : receive a request to change a given attribute to a given
 * value
 * 
 */
public class HardSyncRequestRunnable implements Runnable {

	RequestType requestType;
	public int rankWithLocalAgent;
	int myRank;
	String uniqueID;
	String agentType;
	String attribute;
	Object attributeValue;
	boolean local;

	IScope scope;
	
	boolean needAnswer = false;

	static {
		DEBUG.ON();
	}

	public HardSyncRequestRunnable(RequestType requestType, String agentType, String uniqueID, int myRank,
			int rankWithLocalAgent, String attributeToRead, boolean local) {
		//DEBUG.OUT("HardSyncRequestRunnable1 " + requestType);
		this.requestType = requestType;
		this.agentType = agentType;
		this.uniqueID = uniqueID;
		this.myRank = myRank;
		this.rankWithLocalAgent = rankWithLocalAgent;
		this.attribute = attributeToRead;
		this.local = local;
		//DEBUG.OUT("HardSyncRequestRunnable " + this);
	}

	/**
	 * Request to Get something
	 * 
	 * @param requestType
	 * @param uniqueID
	 * @param myRank
	 * @param rankWithLocalAgent
	 */
	public HardSyncRequestRunnable(RequestType requestType, String agentType, String uniqueID, int myRank,
			int rankWithLocalAgent, boolean local) {
		//DEBUG.OUT("HardSyncRequestRunnable2 " + requestType);
		this.requestType = requestType;
		this.agentType = agentType;
		this.uniqueID = uniqueID;
		this.myRank = myRank;
		this.rankWithLocalAgent = rankWithLocalAgent;
		this.attribute = "";
		this.local = local;
		//DEBUG.OUT("HardSyncRequestRunnable " + this);
	}

	/**
	 * Request to set an attribute
	 * 
	 * @param requestType
	 * @param uniqueID
	 * @param myRank
	 * @param rankWithLocalAgent
	 * @param attributeToRead
	 * @param valueToWrite
	 */
	public HardSyncRequestRunnable(RequestType requestType, String agentType, String uniqueID, int myRank,
			int rankWithLocalAgent, String attributeToWrite, Object valueToWrite, boolean local) {
		//DEBUG.OUT("HardSyncRequestRunnable3 " + requestType);
		this.requestType = requestType;
		this.agentType = agentType;
		this.uniqueID = uniqueID;
		this.myRank = myRank;
		this.rankWithLocalAgent = rankWithLocalAgent;
		this.attribute = attributeToWrite;
		this.attributeValue = valueToWrite;
		this.local = local;
		//DEBUG.OUT("HardSyncRequestRunnable " + this);
	}

	/**
	 * Request to set an attribute
	 * 
	 * @param requestType
	 * @param uniqueID
	 * @param myRank
	 * @param rankWithLocalAgent
	 * @param attributeToRead
	 * @param valueToWrite
	 */
	public HardSyncRequestRunnable(RequestType requestType, String agentType, String uniqueID, int myRank,
			int rankWithLocalAgent, Object valueToWrite, boolean local) {
		//DEBUG.OUT("HardSyncRequestRunnable4 " + requestType);
		this.requestType = requestType;
		this.agentType = agentType;
		this.uniqueID = uniqueID;
		this.myRank = myRank;
		this.rankWithLocalAgent = rankWithLocalAgent;
		this.attribute = "";
		this.attributeValue = valueToWrite;
		this.local = local;
		//DEBUG.OUT("HardSyncRequestRunnable " + this);
	}
	
	public HardSyncRequestRunnable(RequestType requestType)
	{
		this.requestType = requestType;
	}
	
	public void needAnswer()
	{
		this.needAnswer = true;
	}
	
	void sendAnswer(Object value)
	{
		DEBUG.OUT("sendAnswer from  " + this.requestType);
		DEBUG.OUT("sendAnswer sendAnswersendAnswersendAnswersendAnswer " + value);
		if(this.local)
		{
			DEBUG.OUT("sendAnswer lcoal " + value);
			if(value == null) 
			{
				value = new Object();
			}
			((DistributionExperiment)scope.getExperiment()).getHardSyncServer().setAnswer(value);
			DEBUG.OUT("caleld answer lcoal " + value);
			return;
		}

		DEBUG.OUT("sendAnswer no lcoallcoal");
		MPIFunctions.MPI_SEND(this.scope, value, this.myRank, IMPISkill.REQUEST_READ);
	}

	@Override
	public void run() {
		DEBUG.OUT("HardSyncRequestRunnable run " + this.requestType);
		DEBUG.OUT("HardSyncRequestRunnable hashcode " + this.uniqueID);
		switch (this.requestType) {
		case _init_:
			_init_();
			break;
		case compareTo:
			compareTo();
			break;
		case copy:
			copy();
			break;
		case dead:
			dead();
			break;
		case getAgent:
			getAgent();
			break;
		case dispose:
			dispose();
			break;
		case get:
			get();
			break;
		case getAttribute:
			getAttribute();
			break;
		case setAttribute:
			setAttributes();
			break;
		case getAttributes:
			getAttributes();
			break;
		case getDirectVarValue:
			getDirectVarValue();
			break;
		case getFromIndicesList:
			break;
		case getLocation:
			getLocation();
			break;
		case getMacroAgents:
			getMacroAgents();
			break;
		case getOrCreateAttributes:
			DEBUG.OUT("server1 getOrCreateAttributes");
			getOrCreateAttributes();
			break;
		case getPeers:
			getPeers();
			break;
		case getPopulation:
			getPopulation();
			break;
		case getPopulationFor:
			getPopulationFor();
			break;
		case getSpecies:
			getSpecies();
			break;
		case hasAttribute:
			hasAttribute();
			break;
		case init:
			// init();
			break;
		case initSubPopulations:
			// initSubPopulations();
			break;
		case primDie:
			primDie();
			break;
		case schedule:
			schedule();
			break;
		case setAgent:
			setAgent();
			break;
		case setDirectVarValue:
			setDirectVarValue();
			break;
		case setGeometry:
			setGeometry();
			break;
		case getGeometry:
			getGeometry();
			break;
		case setLocation:
			setLocation();
			break;
		case setName:
			setName();
			break;
		case getName:
			getName();
			break;

		case getHost: // ??
			break;
		case setHost: // ??
			break;
		case getScope: // ??
			break;
		case getModel: // ??
			break;
		default:
			break;
		}
	}

	private IAgent getAgentRequested(IScope scope) 
	{	
		DEBUG.OUT("getAgentRequested for resquest " + this.requestType);
		DEBUG.OUT("getAgentRequested this.agentType " + this.agentType);
		final IPopulation<? extends IAgent> pop = scope.getSimulation().getPopulationFor(this.agentType);

		DEBUG.OUT("foudn the pop : " + pop);
		DEBUG.OUT("looking for hashcode : " + this.uniqueID);
		for (var auto : pop) {
			DEBUG.OUT("		auto " + auto);
			DEBUG.OUT("		auto.getUUID() " + auto.getUUID());
			if (this.uniqueID.equals(auto.getUUID().toString())) {
				DEBUG.OUT("foudn the agent : " + auto);

				if(auto instanceof ProxyAgent pa)
				{
					DEBUG.OUT("synchro mode : " + pa.synchroMode);
					if(pa.synchroMode instanceof HardSyncMode hs)
					{
						DEBUG.OUT("HardSyncMode hs : " + hs);
						if(hs.local)
						{
							DEBUG.OUT("hs.localhs.local : " + hs.localHardSyncedAgent);
							return hs.localHardSyncedAgent;
						}
					}
				}
				return auto;
			}
		}
		DEBUG.OUT("didnt foudn the agent " + this.requestType);
		//throw new Exception("Agent not found");
		return null;
	}
	
	public String toString() {

		String str = "toString requestType " + this.requestType + "\n";
		str += " rankWithLocalAgent " + this.rankWithLocalAgent + "\n";
		str += " myRank " + this.myRank + "\n";
		str += " uniqueID " + this.uniqueID + "\n";
		str += " agentType " + this.agentType + "\n";
		str += " attribute " + this.attribute + "\n";
		str += " attributeValue " + this.attributeValue + "\n";
		str += " local? " + this.local + "\n";
		return str;
	}

	private void _init_() {
		DEBUG.OUT("_init_ answer ");
		// getAgentRequested(scope)._init(this.scope);
	}

	private void getAttribute() {
		DEBUG.OUT("getAttribute answer ");
		DEBUG.OUT("getAttribute tryuing to get " + this.attributeValue);
		IAgent agent = getAgentRequested(scope);
		Object value = agent.getAttribute((String)this.attributeValue);

		DEBUG.OUT("send value " + value);
		sendAnswer(value);
	}

	private void getAgent() 
	{
		DEBUG.OUT("getAgent answer ");
		IAgent agent = getAgentRequested(scope);
		DEBUG.OUT("send getAgent value " + agent);
		sendAnswer(agent);
	}

	private void setAttributes() {
		DEBUG.OUT("setAttributes answer ");
		DEBUG.OUT("setAttributes " + this.attribute + " :: " + this.attributeValue);
		getAgentRequested(scope).setAttribute(this.attribute, this.attributeValue); // check if the change is linked to
																					// the real agent
	}

	private void getAttributes() {
		DEBUG.OUT("getAttributes answer ");
		DEBUG.OUT("getAttributes create?? " + this.attributeValue);
		IAgent agent = getAgentRequested(scope);
		Map<String, Object> value = agent.getAttributes((boolean) this.attributeValue);

		DEBUG.OUT("getAttributes answer " + value);
		sendAnswer(value);
	}

	private void dead() {
		DEBUG.OUT("dead answer");
		IAgent agent = getAgentRequested(scope);
		if(agent != null)
		{
			boolean dead = agent.dead();
			DEBUG.OUT("agent dead ? " + dead);
			sendAnswer(dead);
		}else
		{
			DEBUG.OUT("agent = null");
		}
	}

	private void setDirectVarValue() {
		DEBUG.OUT("setDirectVarValue answer");
		DEBUG.OUT("setDirectVarValue " + this.attribute + " :: " + this.attributeValue);
		getAgentRequested(scope).setDirectVarValue(scope, this.attribute, this.attributeValue); // check if the change is linked to the real agent
	}

	private void getDirectVarValue() {
		DEBUG.OUT("getDirectVarValue answer");
		IAgent agent = getAgentRequested(scope);
		DEBUG.OUT("getDirectVarValue of " + ((String) this.attributeValue));
		Object value = agent.getDirectVarValue(scope, ((String) this.attributeValue));
																		// agent
		DEBUG.OUT("getDirectVarValue[" + ((String) this.attributeValue) + "] value" + value);
		sendAnswer(value);
	}

	private void primDie() {
		DEBUG.OUT("primDie answer");
		getAgentRequested(scope).primDie(scope);
	}

	private void dispose() {
		DEBUG.OUT("dispose answer");
		getAgentRequested(scope).dispose();
	}

	private void getLocation() {
		DEBUG.OUT("getLocation answer");
		IAgent agent = getAgentRequested(scope);
		GamaPoint value = agent.getLocation(); // check if the change is linked to the real agent
		DEBUG.OUT("getLocation answer " + value);
		sendAnswer(value);
	}

	private void setLocation() {
		DEBUG.OUT("setLocation answer");
		IAgent agent = getAgentRequested(scope);
		DEBUG.OUT("setLocation " + this.attributeValue);
		GamaPoint value = agent.setLocation((GamaPoint) this.attributeValue); // check if the change is linked to the
																				// real agent
		DEBUG.OUT("setLocation value " + value);
		sendAnswer(value);
	}

	private void setName() {
		DEBUG.OUT("setName answer " + attributeValue);
		getAgentRequested(scope).setName((String) attributeValue);
	}

	private void getName() {
		DEBUG.OUT("getName answer ");
		IAgent agent = getAgentRequested(scope);
		String name = agent.getName();
		DEBUG.OUT("getName answer " + name);
		sendAnswer(name);
	}

	private void getGeometry() {
		DEBUG.OUT("getGeometry asnwer ");
		IAgent agent = getAgentRequested(scope);
		IShape geometry = agent.getGeometry();
		DEBUG.OUT("getGeometry result from server " + geometry);
		sendAnswer(geometry);
	}

	private void setGeometry() {
		DEBUG.OUT("setGeometry asnwer ");
		getAgentRequested(scope).setGeometry(scope, (IShape) this.attributeValue);
	}

	private void setAgent() {
		DEBUG.OUT("setAgent asnwer ");
		getAgentRequested(scope).setAgent((IAgent) this.attributeValue);
	}

	private void schedule() {
		DEBUG.OUT("schedule asnwer ");
		getAgentRequested(scope).schedule(scope);
	}

	private void hasAttribute() {
		DEBUG.OUT("hasAttribute asnwer " + this.attribute);
		IAgent agent = getAgentRequested(scope);
		boolean hasAttributes =  agent.hasAttribute(this.attribute);
		DEBUG.OUT("hasAttribute " + hasAttributes);
		sendAnswer(hasAttributes);
	}

	private void getSpecies() {
		DEBUG.OUT("getSpecies asnwer ");
		IAgent agent = getAgentRequested(scope);
		ISpecies species = agent.getSpecies();
		DEBUG.OUT("getSpecies " + species);
		sendAnswer(species);
	}

	private void getPopulationFor() {
		DEBUG.OUT("getPopulationFor asnwer ");
		IAgent agent = getAgentRequested(scope);
		ISpecies species = agent.getSpecies();
		DEBUG.OUT("getPopulationFor species  " + species);
		IPopulation pop = agent.getPopulationFor(species);
		DEBUG.OUT("getPopulationFor pop  " + pop);
		sendAnswer(pop);
	}

	private void getPopulation() {
		DEBUG.OUT("getPopulation ");
		IAgent agent = getAgentRequested(scope);
		DEBUG.OUT("agent request in get population " + agent);
		IPopulation pop = agent.getPopulation();
		DEBUG.OUT("getPopulation pop " + pop);
		sendAnswer(pop);
	}

	private void getPeers() {
		DEBUG.OUT("getPeers asnwer ");
		IAgent agent = getAgentRequested(scope);
		IList<IAgent> peers = agent.getPeers();
		DEBUG.OUT("getPeers peers " + peers);
		sendAnswer(peers);
	}

	private void getMacroAgents() {
		DEBUG.OUT("getMacroAgents asnwer ");
		IAgent agent = getAgentRequested(scope);
		List<IAgent> macro = agent.getMacroAgents();
		DEBUG.OUT("getMacroAgents macro " + macro);
		sendAnswer(macro);
	}

	private void get() {
		DEBUG.OUT("get asnwerf ");
		IAgent agent = getAgentRequested(scope);
		Object value = agent.get(scope, this.attribute);
		DEBUG.OUT("get value " + value);
		sendAnswer(value);
	}

	private void copy() {
		DEBUG.OUT("copy asnwerf ");
		IAgent agent = getAgentRequested(scope);
		IShape shape = agent.copy(scope);
		sendAnswer(shape);
	}

	private void compareTo() {
		DEBUG.OUT("compareTo asnwerf ");
		IAgent agent = getAgentRequested(scope);
		int compare = agent.compareTo((IAgent) this.attributeValue);
		DEBUG.OUT("compareTo compare " + compare);
		sendAnswer(compare);
	}

	private void getOrCreateAttributes() {
		DEBUG.OUT("server getOrCreateAttributes");
		IAgent agent = getAgentRequested(scope);
		IMap<String, Object> attributes = agent.getOrCreateAttributes();
		DEBUG.OUT("server getOrCreateAttributes attributes " + attributes);
		sendAnswer(attributes);
	}
}
