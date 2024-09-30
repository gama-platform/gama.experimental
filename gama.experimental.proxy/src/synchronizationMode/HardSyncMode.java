package synchronizationMode;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import MPISkill.IMPISkill;
import MPISkill.MPIFunctions;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.model.IModel;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.agent.ISerialisedAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.species.ISpecies;
import hardSyncModeComm.HardSyncRequestRunnable;
import hardSyncModeComm.RequestType;
import mpi.MPI;
import mpi.MPIException;
import proxy.ProxyAgent;
import distributionExperiment.DistributionExperiment;

/**
 * HardSyncMode : synchronization mode sending request to Processor mpiRankOfLocalAgent to get/set data of agent with "uniqueID" uniqueID
 * 
 * 
 * 
 */
public class HardSyncMode extends BaseDistantSyncMode {
	
	public int myRank;
	public int mpiRankOfLocalAgent;
	public String agentType;
	public String uniqueID;
	public IScope scope;
	
	public IAgent localHardSyncedAgent; /* only used by local, this agent prevent local request to loop infinitely
										* as the request will get the local agent which is synchronized with this same syncmode
										* that will then send the exact same request lol 
										* this is ugly as hell but this also mean that we don't have to duplicate this class into :
										* 
										* localHardSyncmode et distantHardSyncMode
										* 
										* maybe it would be better but this is too hard to say at the moment
 										*/
	
	public boolean local;
	public int originalProcessRank;
	
	public Map<String, Object> constants; // no need to send request for theses constants
	
	static
	{
		DEBUG.ON();
	}

	
	public HardSyncMode(IScope scope, int myRank, int mpiRankOfLocalAgent, IAgent agent, boolean local, IAgent localHardSyncedAgent) 
	{

		DEBUG.OUT("HardSyncMode scope " + scope);
		DEBUG.OUT("HardSyncMode myRank " + myRank);
		DEBUG.OUT("HardSyncMode mpiRankOfLocalAgent " + mpiRankOfLocalAgent);
		DEBUG.OUT("HardSyncMode agent " + agent);
		
		DEBUG.OUT("HardSyncMode agenttype " + agent.getSpeciesName());
		DEBUG.OUT("HardSyncMode local " + local);
		
		Map<String, Object> constants = GamaMapFactory.create();	
		constants.put(IKeyword.INDEX, agent.getIndex());
		constants.put(IKeyword.TYPE, agent.getSpeciesName());
		constants.put(IKeyword.POPULATION, agent.getPopulation());
		
		this.myRank = myRank;
		this.mpiRankOfLocalAgent = mpiRankOfLocalAgent;
		this.agentType = agent.getSpeciesName();
		this.uniqueID = ((ProxyAgent)agent).getUUID().toString();
		
		this.constants = constants;
		this.local = local;
		this.originalProcessRank = ((ProxyAgent)agent).originalProcessRank;
		DEBUG.OUT("HardSyncMode ?? local or not ? : " + local);
		DEBUG.OUT("HardSyncMode ?? originalProcessRankoriginalProcessRank ? : " + originalProcessRank);
		this.scope = scope;
		this.localHardSyncedAgent = localHardSyncedAgent;
		
		/*
		DEBUG.OUT("Hard hashCode1 " + ((ProxyAgent)agent).getUUID());
		DEBUG.OUT("Hard hashCode2 " + ((ProxyAgent)agent).synchroMode.getUUID());
		DEBUG.OUT("Hard inti myRank " + this.scope);
		DEBUG.OUT("Hard myRank " + this.myRank);
		DEBUG.OUT("Hard mpiRankOfLocalAgent " + this.mpiRankOfLocalAgent);
		DEBUG.OUT("Hard agentType " + this.agentType);
		DEBUG.OUT("Hard hashCode " + this.uniqueID);
		DEBUG.OUT("Hard constants " + this.constants);
		*/
	}	
	
	HardSyncRequestRunnable setupRequest(RequestType requestType)
	{

		/*DEBUG.OUT("setupRequest 1 " + requestType);
		DEBUG.OUT(" 	inti myRank " + this.scope);
		DEBUG.OUT(" 	myRank " + this.myRank);
		DEBUG.OUT(" 	mpiRankOfLocalAgent " + this.mpiRankOfLocalAgent);
		DEBUG.OUT(" 	agentType " + this.agentType);
		DEBUG.OUT(" 	hashCode " + this.uniqueID);
		DEBUG.OUT(" 	constants " + this.constants);*/
		return new HardSyncRequestRunnable(requestType, this.agentType, this.uniqueID, this.myRank, this.mpiRankOfLocalAgent, this.local);
	}
	
	HardSyncRequestRunnable setupRequest(RequestType requestType, Object value)
	{

		/*DEBUG.OUT("setupRequest 2 " + requestType);
		DEBUG.OUT(" 	inti myRank " + this.scope);
		DEBUG.OUT(" 	myRank " + this.myRank);
		DEBUG.OUT(" 	mpiRankOfLocalAgent " + this.mpiRankOfLocalAgent);
		DEBUG.OUT(" 	agentType " + this.agentType);
		DEBUG.OUT(" 	hashCode " + this.uniqueID);
		DEBUG.OUT(" 	constants " + this.constants);*/
		return new HardSyncRequestRunnable(requestType, this.agentType, this.uniqueID, this.myRank, this.mpiRankOfLocalAgent, value, this.local);
	}
	
	HardSyncRequestRunnable setupRequest(RequestType requestType, String attribute, Object value)
	{

		/*DEBUG.OUT("setupRequest 3 " + requestType);
		DEBUG.OUT(" 	inti myRank " + this.scope);
		DEBUG.OUT(" 	myRank " + this.myRank);
		DEBUG.OUT(" 	mpiRankOfLocalAgent " + this.mpiRankOfLocalAgent);
		DEBUG.OUT(" 	agentType " + this.agentType);
		DEBUG.OUT(" 	hashCode " + this.uniqueID);
		DEBUG.OUT(" 	constants " + this.constants);*/
		return new HardSyncRequestRunnable(requestType, this.agentType, this.uniqueID, this.myRank, this.mpiRankOfLocalAgent, attribute, value, this.local);
	}
	
	void sendRequest(HardSyncRequestRunnable request)
	{
		DEBUG.OUT("sendRequest" + request );
		if(local)
		{
			DEBUG.OUT("sendRequest is local");
			if(scope.getExperiment() != null && ((DistributionExperiment)scope.getExperiment()).getHardSyncServer() != null) 
			{
				((DistributionExperiment)scope.getExperiment()).getHardSyncServer().addRequest(request);
			}else
			{
				DEBUG.OUT("sendRequest failed because the server is not up or experiment is disposed");
			}
			return;
		}
		DEBUG.OUT("sendRequest is distant : sending to rank " + request.rankWithLocalAgent);
		MPIFunctions.MPI_SEND(this.getScope(), request, request.rankWithLocalAgent, IMPISkill.REQUEST_TYPE); // receive request
	}
	
	Object receiveResult()
	{
		// todo if local -> lock or mutex
		// else MPI_receive
		if(local)
		{
			Object value;
			DEBUG.OUT(" for lcoal ");
			if(scope.getExperiment() != null &&  ((DistributionExperiment)scope.getExperiment()).getHardSyncServer() != null) 
			{
				value = ((DistributionExperiment)scope.getExperiment()).getHardSyncServer().getAnswer();
			}else
			{
				value = null;
				DEBUG.OUT("receiveResult failed because the server is not up or experiment is disposed");
			}
			DEBUG.OUT("receiveResult : " + value);
			
			return value;
		}

		DEBUG.OUT("NOT LOCAL");
		Object res = MPIFunctions.MPI_RECV(this.getScope(), mpiRankOfLocalAgent, IMPISkill.REQUEST_READ);
		DEBUG.OUT("receiveResult for distant : " + res);
		
		return res; 
	}
	
		
	@Override
	public IAgent getAgent()
	{
		DEBUG.OUT("DistantHardSync getAgent ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getAgent);
		request.needAnswer();
		sendRequest(request);
		Object res = receiveResult();
		DEBUG.OUT("DistantHardSync getAgent res " + res);
		return (IAgent) res;
	}
	
	@Override
	public void stepProxy()
	{
	}
	
	@Override
	public boolean step(IScope scope) throws GamaRuntimeException {
		if(local)
		{
			DEBUG.OUT("steppuin g local");
			this.localHardSyncedAgent.step(scope);
		}
		return true;
	}
	
	@Override
	public IMap<String, Object> getOrCreateAttributes()
	{
		DEBUG.OUT("DistantHardSync getOrCreateAttributes ");
		
		HardSyncRequestRunnable request = setupRequest(RequestType.getOrCreateAttributes);
		request.needAnswer();
		sendRequest(request);
		Object res = receiveResult();
		DEBUG.OUT("DistantHardSync getOrCreateAttributes  res  : " + res);
		return (IMap<String, Object>) res;
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		DEBUG.OUT("DistantHardSync stringValue ");
		return serializeToGaml(true); // todo ???
	}
	
	@Override
	public Object getAttribute(String key)
	{
		DEBUG.OUT("DistantHardSync getAttribute " + key);
		HardSyncRequestRunnable request = setupRequest(RequestType.getAttribute, key);
		request.needAnswer();
		sendRequest(request);
		Object res = receiveResult();
		DEBUG.OUT("DistantHardSync getAttribute " + key + " //// " + res);
		DEBUG.OUT("DistantHardSync getAttribute("+key+")  res  " + res.toString());
		
		return res;
	}

	@Override
	public void setAttribute(String key, Object value) 
	{
		DEBUG.OUT("DistantHardSync setAttribute " + key + " :: " + value);
		sendRequest(setupRequest(RequestType.setAttribute, key, value));
	}
	
	@Override
	public boolean hasAttribute(String key)
	{
		DEBUG.OUT("DistantHardSync hasAttribute " + key);
		HardSyncRequestRunnable request = setupRequest(RequestType.hasAttribute);
		request.needAnswer();
		sendRequest(request);
		Object res = receiveResult();
		DEBUG.OUT("DistantHardSync hasAttribute  res " + res);
		return (boolean) res;
	}
	
	@Override
	public GamaPoint getLocation() 
	{
		DEBUG.OUT("DistantHardSync getLocation ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getLocation);
		request.needAnswer();
		sendRequest(request);
		Object location = receiveResult();
		DEBUG.OUT("DistantHardSync getLocation  res " + location);
		
		return (GamaPoint) location;
	}
	
	@Override
	public GamaPoint setLocation(GamaPoint l)
	{
		DEBUG.OUT("DistantHardSync setLocation " + l);
		HardSyncRequestRunnable request = setupRequest(RequestType.setLocation, l);
		request.needAnswer();
		sendRequest(request);
		Object location = receiveResult();
		DEBUG.OUT("DistantHardSync setLocation  res " + location);
		return (GamaPoint) location;
	}
	
	@Override
	public boolean dead() 
	{
		DEBUG.OUT("DistantHardSync dead ");
		HardSyncRequestRunnable request = setupRequest(RequestType.dead);
		request.needAnswer();
		sendRequest(request);
		Object dead = receiveResult();
		DEBUG.OUT("DistantHardSync dead  res " + dead);
		return (boolean) dead;
	}
	
	@Override
	public Object getDirectVarValue(IScope scope, String s) throws GamaRuntimeException {
		DEBUG.OUT("DistantHardSync getDirectVarValue " + s);
		DEBUG.OUT("DistantHardSync getDirectVarValue name of attribute " + s);

		HardSyncRequestRunnable request = setupRequest(RequestType.getDirectVarValue, s);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getDirectVarValue  res " + value);
		return value;
	}

	
	@Override
	public void setDirectVarValue(IScope scope, String s, Object v) throws GamaRuntimeException {
		DEBUG.OUT("DistantHardSync setDirectVarValue ");
		sendRequest(setupRequest(RequestType.setDirectVarValue, s, v));
	}
	
	@Override
	public void updateWith(IScope scope, ISerialisedAgent sa)
	{
		DEBUG.OUT("DistantHardSync updateWith ");
		final Map<String, Object> mapAttr = sa.attributes();
		for (final Entry<String, Object> attr : mapAttr.entrySet()) {
			this.setDirectVarValue(scope, attr.getKey(), attr.getValue()); // todo opti this because we sending sa.attributes().lenght messages
		}
	}
	
	@Override
	public IShape copy(IScope scope) 
	{
		DEBUG.OUT("DistantHardSync copy ");
		HardSyncRequestRunnable request = setupRequest(RequestType.copy);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync copy  res " + value);
		return (IShape) value;
	}

	@Override
	public void dispose() 
	{
		// TODO DISPOSe
		/*DEBUG.OUT("dispose : " + this.local);
		if(local)
		{
			this.localHardSyncedAgent.dispose();;
		}*/
		
		DEBUG.OUT("DistantHardSync dispose ");
		sendRequest(setupRequest(RequestType.dispose));
	}

	@Override
	public boolean init(IScope scope) throws GamaRuntimeException 
	{
		DEBUG.OUT("DistantHardSync init ");
		/*sendRequest(setupRequest(RequestType.init));
		Object value = receiveResult();
		return (boolean) value;*/
		return true;
	}
	
	@Override
	public Object _init_(final IScope scope)
	{
		DEBUG.OUT("DistantHardSync _init_ ");
		/*sendRequest(setupRequest(RequestType._init_));
		Object value = receiveResult();
		return (boolean) value;*/
		return null;
	}
	
	@Override
	public boolean initSubPopulations(final IScope scope) {
		DEBUG.OUT("DistantHardSync initSubPopulations ");
		/*sendRequest(setupRequest(RequestType.initSubPopulations));
		Object value = receiveResult();
		return (boolean) value;*/
		return true;
	}

	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException 
	{
		DEBUG.OUT("DistantHardSync get ");
		HardSyncRequestRunnable request = setupRequest(RequestType.get, index);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync get  res " + value);
		return (boolean) value;
	}
	
	@Override
	public String getName() {
		DEBUG.OUT("DistantHardSync getName ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getName);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getName  res " + value);
		return (String) value;
	}
	
	@Override
	public void setName(String name) {
		DEBUG.OUT("DistantHardSync setName ");
		sendRequest(setupRequest(RequestType.setName, name)); // notify the name change
	}
	
	@Override
	public GamaPoint getLocation(IScope scope) {
		DEBUG.OUT("DistantHardSync getLocation ");
		return (GamaPoint) getAttribute(IKeyword.LOCATION);
	}

	
	@Override
	public GamaPoint setLocation(IScope scope, GamaPoint l) {
		DEBUG.OUT("DistantHardSync setLocation ");
		setAttribute(IKeyword.LOCATION, l); // todo check if correct
		return l;
	}

	
	@Override
	public IShape getGeometry(IScope scope) {
		DEBUG.OUT("DistantHardSync getGeometry ");
		return (GamaPoint) getAttribute(IKeyword.SHAPE);
	}
	
	@Override
	public IShape getGeometry() {
		//DEBUG.OUT("DistantHardSync getGeometry2 ");
		//return (GamaPoint) getAttribute(IKeyword.SHAPE); // 1
		HardSyncRequestRunnable request = setupRequest(RequestType.getGeometry);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getGeometry2 " + value);
		return (IShape) value;
		// 2 

		//DEBUG.OUT("DistantHardSync getGeometry " + getAttribute(IKeyword.SHAPE));
		//return (IShape) getAttribute(IKeyword.SHAPE);
	}

	
	@Override
	public void setGeometry(IScope scope, IShape newGeometry) {
		DEBUG.OUT("DistantHardSync setGeometry ");
		sendRequest(setupRequest(RequestType.setGeometry, newGeometry));
	}
	
	@Override
	public void schedule(IScope scope) {
		DEBUG.OUT("DistantHardSync schedule ");
		sendRequest(setupRequest(RequestType.schedule));
	}

	
	@Override
	public int getIndex() { // constant
		DEBUG.OUT("DistantHardSync getIndex ");
		return (int) this.constants.get(IKeyword.INDEX);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPopulation<? extends IAgent> getPopulation() {
		/*DEBUG.OUT("DistantHardSync getPopulation ");
		sendRequest(setupRequest(RequestType.getPopulation));
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getPopulation res " + value);
		return (IPopulation<? extends IAgent>) value;*/
		
		IPopulation pop = (IPopulation) this.constants.get(IKeyword.POPULATION);
		DEBUG.OUT("DistantHardSync getSpecies  res " + pop);
		
		return pop;
	}
	
	@Override
	public String getSpeciesName() {
		DEBUG.OUT("DistantHardSync getSpeciesName ");
		return (String) this.constants.get(IKeyword.TYPE);
	}

	
	@Override
	public ISpecies getSpecies() {
		/*
		 
		sendRequest(setupRequest(RequestType.getSpecies));
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getSpecies  res " + value);
		return (ISpecies) value;
		
		*/
		ISpecies specie = getPopulation().getSpecies();
		DEBUG.OUT("DistantHardSync getSpecies  res " + specie);
		
		return specie;
	}
	
	@Override
	public IScope getScope() {
		return this.scope;
	}
	
	@Override
	public IMacroAgent getHost() {
		DEBUG.OUT("DistantHardSync getHost ");
		return getPopulation().getHost();
	}

	@Override
	public boolean isInstanceOf(ISpecies s, boolean direct) {
		DEBUG.OUT("DistantHardSync isInstanceOf "); 
		return this.getSpecies().getName().equals(s.getName());
	}
	
	@Override
	public boolean isInstanceOf(String skill, boolean direct) {
		DEBUG.OUT("DistantHardSync isInstanceOf2 "); 
		return getSpecies().implementsSkill(skill);
	}

	@Override
	public IModel getModel() {
		DEBUG.OUT("DistantHardSync getModel ");
		return null;	// TODO : does it matter if we get the model on the current proc?
	}
	 
	@Override
	public Object primDie(IScope scope) throws GamaRuntimeException{
		DEBUG.OUT("DistantHardSync primDie ");
		HardSyncRequestRunnable request = setupRequest(RequestType.primDie);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync primDie  res " + value);
		return value;
	}
	
	@Override
	public boolean covers(IShape g)
	{
		DEBUG.OUT("DistantHardSync covers ");
		IShape shape = this.getGeometry();
		return shape.covers(g);
	}
	
	@Override
	public boolean intersects(IShape g)
	{
		DEBUG.OUT("DistantHardSync intersects ");
		IShape shape = this.getGeometry();
		return shape.intersects(shape);
	}
	
	@Override
	public boolean crosses(IShape g)
	{
		DEBUG.OUT("DistantHardSync crosses ");
		IShape shape = this.getGeometry();
		return shape.crosses(shape);
	}
	
	@Override
	public void setInnerGeometry(final Geometry geom) {
		DEBUG.OUT("DistantHardSync setInnerGeometry ");
		this.getGeometry().setInnerGeometry(geom);
	}

	@Override
	public IList<GamaPoint> getPoints() {

		DEBUG.OUT("DistantHardSync getPoints ");
		IShape shape = this.getGeometry();
		if (shape == null) return GamaListFactory.EMPTY_LIST;
		return shape.getPoints();
	}

	@Override
	public void setDepth(final double depth) 
	{
		DEBUG.OUT("DistantHardSync setDepth ");
		IShape shape = this.getGeometry();
		if (shape == null) return;
		shape.setDepth(depth);
	}
	
	@Override
	public void setGeometricalType(final IShape.Type t) 
	{
		DEBUG.OUT("DistantHardSync setGeometricalType ");
		this.getGeometry().setGeometricalType(t);
	}

	@Override
	public int intValue(final IScope scope) 
	{
		DEBUG.OUT("DistantHardSync intValue ");
		return this.getIndex();
	}
	
	@Override
	public Double getArea() { 
		DEBUG.OUT("DistantHardSync getArea ");
		return this.getGeometry().getArea(); 
	}

	@Override
	public Double getVolume() { 
		DEBUG.OUT("DistantHardSync getVolume ");
		return this.getGeometry().getVolume(); 
	}

	@Override
	public double getPerimeter() { 
		DEBUG.OUT("DistantHardSync getPerimeter ");
		return this.getGeometry().getPerimeter(); }

	@Override
	public IList<GamaShape> getHoles() { 
		DEBUG.OUT("DistantHardSync getHoles ");
		return this.getGeometry().getHoles(); 
	}

	@Override
	public GamaPoint getCentroid() { 
		DEBUG.OUT("DistantHardSync getCentroid ");
		return this.getGeometry().getCentroid(); 
	}

	@Override
	public GamaShape getExteriorRing(final IScope scope) { 
		DEBUG.OUT("DistantHardSync getExteriorRing ");
		return this.getGeometry().getExteriorRing(scope); 
	}

	@Override
	public Double getWidth() { 
		DEBUG.OUT("DistantHardSync getWidth ");
		return this.getGeometry().getWidth(); 
	}

	@Override
	public Double getHeight() { 
		DEBUG.OUT("DistantHardSync getHeight ");
		return this.getGeometry().getHeight(); 
	}

	@Override
	public Double getDepth() { 
		DEBUG.OUT("DistantHardSync getDepth ");
		return this.getGeometry().getDepth(); 
	}

	@Override
	public GamaShape getGeometricEnvelope() { 
		DEBUG.OUT("DistantHardSync getGeometricEnvelope ");
		return this.getGeometry().getGeometricEnvelope(); 
	}

	@Override
	public IList<? extends IShape> getGeometries() { 
		DEBUG.OUT("DistantHardSync getGeometries ");
		return this.getGeometry().getGeometries(); 
	}

	@Override
	public boolean isMultiple() { 
		DEBUG.OUT("DistantHardSync isMultiple ");
		return this.getGeometry().isMultiple(); 
	}

	@Override
	public boolean isPoint() { 
		DEBUG.OUT("DistantHardSync isPoint ");
		return this.getGeometry().isPoint(); 
	}

	@Override
	public boolean isLine() { 
		DEBUG.OUT("DistantHardSync isLine ");
		return this.getGeometry().isLine(); 
	}

	@Override
	public Geometry getInnerGeometry() { 
		DEBUG.OUT("DistantHardSync getInnerGeometry ");
		return this.getGeometry().getInnerGeometry(); 
	}

	@Override
	public Envelope3D getEnvelope() {
		DEBUG.OUT("DistantHardSync getEnvelope ");
		final IShape g = this.getGeometry();
		return g == null ? null : g.getEnvelope();
	}
	
	@Override
	public double euclidianDistanceTo(final IShape g) {
		DEBUG.OUT("DistantHardSync euclidianDistanceTo ");
		return this.getGeometry().euclidianDistanceTo(g);
	}
	
	@Override
	public double euclidianDistanceTo(final GamaPoint g) {
		DEBUG.OUT("DistantHardSync euclidianDistanceTo2 ");
		return this.getGeometry().euclidianDistanceTo(g);
	}
	
	@Override
	public boolean partiallyOverlaps(final IShape g) {
		DEBUG.OUT("DistantHardSync partiallyOverlaps ");
		return this.getGeometry().partiallyOverlaps(g);
	}
	
	@Override
	public boolean touches(final IShape g) {
		DEBUG.OUT("DistantHardSync touches ");
		return this.getGeometry().touches(g);
	}

	@Override
	public ITopology getTopology() {
		DEBUG.OUT("DistantHardSync getTopology ");
		return getPopulation().getTopology();
	}

	@Override
	public void setPeers(IList<IAgent> peers) { // OK
		DEBUG.OUT("DistantHardSync setPeers ");
	}

	@Override
	public IList<IAgent> getPeers() throws GamaRuntimeException {
		DEBUG.OUT("DistantHardSync getPeers ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getPeers);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getPeers  res " + value);
		return (IList<IAgent>) value;
	}

	@Override
	public void setHost(IMacroAgent macroAgent) 
	{
		DEBUG.OUT("DistantHardSync setHost ");
		sendRequest(setupRequest(RequestType.setHost, macroAgent)); // todo is that correct?
	}

	@Override
	public List<IAgent> getMacroAgents() {
		DEBUG.OUT("DistantHardSync getMacroAgents ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getMacroAgents);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getMacroAgents  res " + value);
		return (List<IAgent>) value;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(ISpecies microSpecies) {
		DEBUG.OUT("DistantHardSync getPopulationFor ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getPopulationFor, microSpecies);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getPopulationFor  res " + value);
		return (IPopulation<? extends IAgent>) value;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(String speciesName) {
		DEBUG.OUT("DistantHardSync getPopulationFor2 ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getPopulationFor, speciesName);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getPopulationFor2  res " + value);
		return (IPopulation<? extends IAgent>) value;
	}

	@Override
	public Type getGeometricalType() {
		DEBUG.OUT("DistantHardSync getGeometricalType ");
		return getGeometry().getGeometricalType();
	}

	@Override
	public int compareTo(IAgent o) {
		DEBUG.OUT("DistantHardSync compareTo ");
		HardSyncRequestRunnable request = setupRequest(RequestType.compareTo, o);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync compareTo  res " + value);
		return (int) value;
	}

	@Override
	public Object getFromIndicesList(IScope scope, IList<String> indices) throws GamaRuntimeException {
		DEBUG.OUT("DistantHardSync getFromIndicesList ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getFromIndicesList, indices);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getFromIndicesList  res " + value);
		return value;
	}

	@Override
	public Map<String, Object> getAttributes(boolean createIfNeeded) {
		DEBUG.OUT("DistantHardSync getAttributes ");
		HardSyncRequestRunnable request = setupRequest(RequestType.getAttributes, createIfNeeded);
		request.needAnswer();
		sendRequest(request);
		Object value = receiveResult();
		DEBUG.OUT("DistantHardSync getAttributes  res " + value);
		return (Map<String, Object>) value;
	}

	@Override
	public void setAgent(IAgent agent) {
		DEBUG.OUT("DistantHardSync setAgent ");
		sendRequest(setupRequest(RequestType.setAgent, agent));
	}
	
	@Override
	public void setOriginalSimulationID(int originalSimulationID) {
		this.originalProcessRank = originalSimulationID;
	}

	@Override
	public int getOriginalSimulationID() {
		return this.originalProcessRank;
	}

}
