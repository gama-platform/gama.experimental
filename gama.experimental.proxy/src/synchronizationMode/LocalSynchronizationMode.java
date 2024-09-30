package synchronizationMode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;

import distributionExperiment.DistributionExperiment;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.model.IModel;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.agent.ISerialisedAgent;
import gama.core.metamodel.agent.MinimalAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.shape.IShape.Type;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.species.ISpecies;
import mpi.MPI;
import mpi.MPIException;
import proxy.ProxyAgent;
import proxySkill.ProxyFunctions;

/**
 * Class used to define the way to access agent in distributed context
 * 
 * @author Lucas Grosjean
 *
 */
public class LocalSynchronizationMode implements SynchronizationModeAbstract
{
	static
	{
		DEBUG.OFF();
	}
	
	public IAgent proxiedAgent;

	public LocalSynchronizationMode(IAgent proxiedAgent)
	{
		this.proxiedAgent = proxiedAgent;
	}
	
	public void sendUpdate() // TODO : call this at the end of each cycle
	{
		// link this with endActionProxy
	}
	
	@Override
	public void setAgent(IAgent agent)
	{
		this.proxiedAgent = agent;
	}
	
	@Override
	public IAgent getAgent()
	{
		DEBUG.OUT("LocalSynchroMode getAgent : " + this.proxiedAgent);
		return this.proxiedAgent;
	}
	
	public void updateProxiedAgent(IAgent agentUpdated)
	{
		setAgent(agentUpdated);
	}
	
	
	@Override
	public IMap<String, Object> getOrCreateAttributes()
	{
		DEBUG.OUT("LocalSynchroMode getOrCreateAttributes : ");
		return this.proxiedAgent.getOrCreateAttributes();
	}
	
	
	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException
	{
		DEBUG.OUT("LocalSynchroMode stringValue : ");
		return this.proxiedAgent.stringValue(scope);
	}
	
	
	@Override
	public Object getAttribute(String key)
	{
		DEBUG.OUT("LocalSynchroMode getAttributes : " + key);
		Object obj = this.proxiedAgent.getAttribute(key);
		DEBUG.OUT("LocalSynchroMode getAttributes result : " + obj);
		return obj;
	}

	
	@Override
	public void setAttribute(String key, Object value) 
	{
		DEBUG.OUT("LocalSynchroMode setAttribute : " + key);
		this.proxiedAgent.setAttribute(key, value);
	}
	
	
	@Override
	public boolean hasAttribute(String key)
	{
		DEBUG.OUT("LocalSynchroMode hasAttribute : " + key);
		return this.proxiedAgent.hasAttribute(key);
	}
	
	
	@Override
	public GamaPoint getLocation() 
	{
		DEBUG.OUT("LocalSynchroMode getLocation");
		return this.proxiedAgent.getLocation();
	}
	
	
	@Override
	public GamaPoint setLocation(GamaPoint l)
	{
		DEBUG.OUT("LocalSynchroMode setLocation " + l);
		return this.proxiedAgent.setLocation(l);
	}
	
	
	@Override
	public boolean dead() 
	{
		DEBUG.OUT("LocalSynchroMode dead : ");
		return this.proxiedAgent.dead();
	}
	
	
	@Override
	public void updateWith(IScope s, ISerialisedAgent sa)
	{
		DEBUG.OUT("LocalSynchroMode updateWith : ");
		this.proxiedAgent.updateWith(s, sa);
	}
	
	
	@Override
	public IShape copy(IScope scope) {
		DEBUG.OUT("LocalSynchroMode copy : ");
		return this.proxiedAgent.copy(scope);
	}

	
	@Override
	public void dispose() {
		DEBUG.OUT("LocalSynchroMode dispose : ");
		this.proxiedAgent.dispose();
	}

	
	@Override
	public IShape.Type getGeometricalType() {
		DEBUG.OUT("LocalSynchroMode getGeometricalType : ");
		return this.proxiedAgent.getGeometricalType();
	}
	
	@Override
	public int compareTo(IAgent o) {
		DEBUG.OUT("LocalSynchroMode compareTo : ");
		return this.proxiedAgent.compareTo(o);
	}

	
	@Override
	public boolean init(IScope scope) throws GamaRuntimeException {
		return this.proxiedAgent.init(scope);
	}

	
	@Override
	public boolean step(IScope scope) throws GamaRuntimeException {
		DEBUG.OUT("localsynchro mode step ");
		return this.proxiedAgent.step(scope);
	}
	
	
	@Override
	public Object get(IScope scope, String index) throws GamaRuntimeException {
		DEBUG.OUT("LocalSynchroMode get : ");
		return this.proxiedAgent.get(scope, index);
	}
	
	
	@Override
	public Object getFromIndicesList(IScope scope, IList<String> indices) throws GamaRuntimeException {
		return this.proxiedAgent.getFromIndicesList(scope, indices);
	}

	
	@Override
	public IScope getScope() {
		DEBUG.OUT("LocalSynchroMode getScope : ");
		return this.proxiedAgent.getScope();
	}
	
	
	@Override
	public ITopology getTopology() {
		return this.proxiedAgent.getTopology();
	}
	
	
	@Override
	public void setPeers(IList<IAgent> peers) {
		 this.proxiedAgent.setPeers(peers);
	}
	
	
	@Override
	public IList<IAgent> getPeers() throws GamaRuntimeException {
		return this.proxiedAgent.getPeers();
	}
	
	
	@Override
	public String getName() {
		//DEBUG.OUT("LocalSynchroMode getName : ");
		return this.proxiedAgent.getName();
	}

	
	@Override
	public void setName(String name) {
		DEBUG.OUT("LocalSynchroMode setName : ");
		this.proxiedAgent.setName(name);	
	}
	
	
	@Override
	public GamaPoint getLocation(IScope scope) {
		DEBUG.OUT("LocalSynchroMode getLocation : ");
		return this.proxiedAgent.getLocation(scope);
	}
	
	
	@Override
	public GamaPoint setLocation(IScope scope, GamaPoint l) {
		DEBUG.OUT("LocalSynchroMode setLocation : ");
		return this.proxiedAgent.setLocation(scope, l);
	}
	
	
	@Override
	public IShape getGeometry(IScope scope) {
		DEBUG.OUT("LocalSynchroMode getGeometry : ");
		return this.proxiedAgent.getGeometry(scope);
	}
	
	
	@Override
	public IShape getGeometry() {
		DEBUG.OUT("LocalSynchroMode getGeometry2 : ");
		return this.proxiedAgent.getGeometry();
	}
	
	
	@Override
	public void setGeometry(IScope scope, IShape newGeometry) {
		DEBUG.OUT("LocalSynchroMode setGeometry : ");
		this.proxiedAgent.setGeometry(scope, newGeometry);
	}
	
	
	@Override
	public IMacroAgent getHost() {
		DEBUG.OUT("LocalSynchroMode getHost : ");
		return this.proxiedAgent.getHost();
	}
	
	
	@Override
	public void setHost(IMacroAgent macroAgent) {
		DEBUG.OUT("LocalSynchroMode setHost : ");
		this.proxiedAgent.setHost(macroAgent);
	}
	
	
	@Override
	public void schedule(IScope scope) {
		DEBUG.OUT("LocalSynchroMode schedule : ");
		this.proxiedAgent.schedule(scope);
	}
	
	
	@Override
	public int getIndex() {
		DEBUG.OUT("LocalSynchroMode getIndex : ");
		return this.proxiedAgent.getIndex();
	}
	
	
	@Override
	public String getSpeciesName() {
		DEBUG.OUT("LocalSynchroMode getSpeciesName : ");
		return this.proxiedAgent.getSpeciesName();
	}

	
	@Override
	public ISpecies getSpecies() {
		DEBUG.OUT("LocalSynchroMode getSpecies : ");
		return this.proxiedAgent.getSpecies();
	}

	
	@Override
	public IPopulation<? extends IAgent> getPopulation() {
		DEBUG.OUT("LocalSynchroMode getPopulation : ");
		return this.proxiedAgent.getPopulation();
	}

	
	@Override
	public boolean isInstanceOf(ISpecies s, boolean direct) {
		DEBUG.OUT("LocalSynchroMode isInstanceOf : " + s);
		return this.proxiedAgent.isInstanceOf(s, direct);
	}

	
	@Override
	public Object getDirectVarValue(IScope scope, String s) throws GamaRuntimeException {
		DEBUG.OUT("LocalSynchroMode getDirectVarValue : " + s);
		return this.proxiedAgent.getDirectVarValue(scope, s);
	}

	
	@Override
	public void setDirectVarValue(IScope scope, String s, Object v) throws GamaRuntimeException {

		DEBUG.OUT("LocalSynchroMode setDirectVarValue : " + s + " :: " + v);
		
		if(s.equals(IKeyword.SYNCMODE)) 
		{
			if(v != null && ((String)v).equals(IKeyword.HARDSYNC))
			{
				int myRank;
				try {
					myRank = MPI.COMM_WORLD.getRank();
					ProxyAgent proxy = ProxyFunctions.getProxyFromAgent(scope, this.proxiedAgent);
					
					HardSyncMode hs;
					if(proxy.copy)
					{
						DEBUG.OUT("isacopy");
						hs = new HardSyncMode(scope, myRank, proxy.originalProcessRank, proxy, false, this.getAgent()); // non-local hardsync
						
					}else
					{
						DEBUG.OUT("isnotacopy");
						hs = new HardSyncMode(scope, myRank, myRank, proxy, true, this.getAgent()); // local hardsync
					}
					
					DEBUG.OUT("new HardSyncMode set : " + hs);
					proxy.setSynchroMode(hs);
					DEBUG.OUT("proxy sync mode after updatd in setDirectVarValue " + proxy.synchroMode);
					
				} catch (MPIException e) {
					// TODO Auto-generated catch block
					DEBUG.OUT("setDirectVarValue MPIException ");
					e.printStackTrace();
				}
			}else if(v == null)
			{
				DEBUG.OUT("THIS AGENT HAS A VARIABLE \"syncmode\" undefined");
			}
		}
		this.proxiedAgent.setDirectVarValue(scope, s, v);	
	}

	@Override
	public List<IAgent> getMacroAgents() {
		DEBUG.OUT("LocalSynchroMode getMacroAgents : ");
		return this.proxiedAgent.getMacroAgents();	
	}

	
	@Override
	public IModel getModel() {
		DEBUG.OUT("LocalSynchroMode getModel : ");
		return this.proxiedAgent.getModel();	
	}

	
	@Override
	public boolean isInstanceOf(String skill, boolean direct) {
		DEBUG.OUT("LocalSynchroMode isInstanceOf : ");
		return this.proxiedAgent.isInstanceOf(skill,direct);	
	}

	
	@Override
	public IPopulation<? extends IAgent> getPopulationFor(ISpecies microSpecies) {
		DEBUG.OUT("LocalSynchroMode getPopulationFor : ");
		return this.proxiedAgent.getPopulationFor(microSpecies);	
	}

	
	@Override
	public IPopulation<? extends IAgent> getPopulationFor(String speciesName) {
		DEBUG.OUT("LocalSynchroMode getPopulationFor2 : ");
		return this.proxiedAgent.getPopulationFor(speciesName);	
	}	
	 
	
	@Override
	public Object primDie(IScope scope) throws GamaRuntimeException
	{
		DEBUG.OUT("primDIE synchro");
		return this.proxiedAgent.primDie(scope);
	}
	
	@Override
	public void updateAttributes(IAgent agent)
	{
		DEBUG.OUT("updateAttributes SynchronizationMode : " + agent);
	}

	
	@Override
	public boolean covers(IShape g) 
	{
		DEBUG.OUT("LocalSynchroMode covers : ");
		return this.proxiedAgent.covers(g);
	}

	
	@Override
	public boolean intersects(IShape g) {
		DEBUG.OUT("LocalSynchroMode intersects : ");
		return this.proxiedAgent.intersects(g);
	}
	
	@Override
	public boolean crosses(IShape g) {
		DEBUG.OUT("LocalSynchroMode crosses : ");
		return this.proxiedAgent.crosses(g);
	}

	@Override
	public void setInnerGeometry(Geometry geom) {
		this.proxiedAgent.setInnerGeometry(geom);
	}

	@Override
	public IList<GamaPoint> getPoints() {
		return this.proxiedAgent.getPoints();
	}

	@Override
	public void setDepth(double depth) {
		this.proxiedAgent.setDepth(depth);
	}

	@Override
	public void setGeometricalType(Type t) {
		this.proxiedAgent.setGeometricalType(t);
	}

	@Override
	public int intValue(IScope scope) {
		return this.proxiedAgent.intValue(scope);
	}

	@Override
	public Double getArea() {
		return this.proxiedAgent.getArea();
	}

	@Override
	public Double getVolume() {
		return this.proxiedAgent.getVolume();
	}

	@Override
	public double getPerimeter() {
		return this.proxiedAgent.getPerimeter();
	}

	@Override
	public IList<GamaShape> getHoles() {
		return this.proxiedAgent.getHoles();
	}

	@Override
	public GamaPoint getCentroid() {
		DEBUG.OUT("LocalSynchroMode getCentroid : ");
		return this.proxiedAgent.getCentroid();
	}

	@Override
	public GamaShape getExteriorRing(IScope scope) {
		return this.proxiedAgent.getExteriorRing(scope);
	}

	@Override
	public Double getWidth() {
		return this.proxiedAgent.getWidth();
	}

	@Override
	public Double getHeight() {
		return this.proxiedAgent.getHeight();
	}

	@Override
	public Double getDepth() {
		return this.proxiedAgent.getDepth();
	}

	@Override
	public GamaShape getGeometricEnvelope() {
		DEBUG.OUT("LocalSynchroMode getGeometricEnvelope : ");
		return this.proxiedAgent.getGeometricEnvelope();
	}

	@Override
	public IList<? extends IShape> getGeometries() {
		DEBUG.OUT("LocalSynchroMode getGeometries : ");
		return this.proxiedAgent.getGeometries();
	}

	@Override
	public boolean isMultiple() {
		return this.proxiedAgent.isMultiple();
	}

	@Override
	public boolean isPoint() {
		return this.proxiedAgent.isPoint();
	}

	@Override
	public boolean isLine() {
		return this.proxiedAgent.isLine();
	}

	@Override
	public Geometry getInnerGeometry() {
		DEBUG.OUT("LocalSynchroMode getInnerGeometry : ");
		return this.proxiedAgent.getInnerGeometry();
	}

	@Override
	public Envelope3D getEnvelope() {
		return this.proxiedAgent.getEnvelope();
	}

	@Override
	public double euclidianDistanceTo(IShape g) {
		return this.proxiedAgent.euclidianDistanceTo(g);
	}

	@Override
	public double euclidianDistanceTo(GamaPoint g) {
		return this.proxiedAgent.euclidianDistanceTo(g);
	}

	@Override
	public boolean partiallyOverlaps(IShape g) {
		DEBUG.OUT("LocalSynchroMode partiallyOverlaps : ");
		return this.proxiedAgent.partiallyOverlaps(g);
	}

	@Override
	public boolean touches(IShape g) {
		DEBUG.OUT("LocalSynchroMode touches : ");
		return this.proxiedAgent.touches(g);
	}

	@Override
	public void stepProxy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object _init_(IScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean initSubPopulations(IScope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, Object> getAttributes(boolean createIfNeeded) {
		DEBUG.OUT("LocalSynchroMode getAttributes : ");
		return this.proxiedAgent.getAttributes(createIfNeeded);
	}

	@Override
	public void setUUID(String uuid) {
		this.proxiedAgent.setUUID(uuid);
	}

	@Override
	public UUID getUUID() {
		return this.proxiedAgent.getUUID();
	}

	@Override
	public void setOriginalSimulationID(int originalSimulationID) {
		this.proxiedAgent.setOriginalSimulationID(originalSimulationID);		
	}

	@Override
	public int getOriginalSimulationID() {
		return proxiedAgent.getOriginalSimulationID();
	}
}