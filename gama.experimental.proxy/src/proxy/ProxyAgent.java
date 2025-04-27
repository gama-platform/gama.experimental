package proxy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.core.common.geometry.Envelope3D;
import gama.core.kernel.model.IModel;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.agent.ISerialisedAgent;
import gama.core.metamodel.agent.MinimalAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.species.ISpecies;
import proxyPopulation.ProxyPopulation;
import synchronizationMode.BaseDistantSyncMode;
import synchronizationMode.DistantSynchronizationMode;
import synchronizationMode.LocalSynchronizationMode;
import synchronizationMode.SynchronizationModeAbstract;

/**
 * ProxyAgent class, it is used to control access to an agent's attributes
 * 
 * @author Lucas Grosjean
 *
 */
public class ProxyAgent implements IAgent
{
	static
	{
		//DEBUG.ON();
	}

	protected final ProxyPopulation population;
	private IScope scope;
	public SynchronizationModeAbstract synchroMode;
	public UUID uniqueID;
	public boolean copy; // is the proxy a copy?
    public int originalProcessRank;
    
	public ProxyAgent(IAgent proxiedAgent, final ProxyPopulation s, IScope scope, boolean copy, int originalProcessRank)
    {
		DEBUG.OUT("create new proxy : " + proxiedAgent.getName());
		
    	this.synchroMode = new LocalSynchronizationMode(proxiedAgent); // todo custom synchro mode
    	this.population = s;
		this.scope = scope;
		this.uniqueID = proxiedAgent.getUUID();
		this.copy = copy;
		this.originalProcessRank = originalProcessRank;
		DEBUG.OUT("this.copy: " + this.copy);
		DEBUG.OUT("this.originalProcessRank: " + this.originalProcessRank);
		DEBUG.OUT("this.hashcodethisuniquedIF: " + this.uniqueID);
    }
	
	public void fixTopology()
	{
		this.getSynchroMode().getTopology().updateAgent(this.getSynchroMode().getGeometry().getEnvelope(),this);
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		DEBUG.OUT("custom custo m equals : " + this + " :: " + obj);
		return obj == null ? false : this.uniqueID == ((IAgent)obj).getUUID();
	}

	public SynchronizationModeAbstract getSynchroMode() {
		//DEBUG.OUT("getSynchroMode : " + this.synchroMode);
		return this.synchroMode;
	}

	public void setSynchroMode(SynchronizationModeAbstract synchroMode) {
		DEBUG.OUT("setSynchroMode : " + synchroMode);
		this.synchroMode = synchroMode;
	}
	
	public void setSynchronizationMode(LocalSynchronizationMode synchroMode)
	{
		DEBUG.OUT("set synchroMode " + synchroMode.getClass());
		this.synchroMode = synchroMode;
	}
	public void setSynchronizationMode(BaseDistantSyncMode synchroMode)
	{
		DEBUG.OUT("set setDistantSynchronizationMode " + synchroMode.getClass());
		this.synchroMode = synchroMode;
	}	
	
	@Override
	public IAgent getAgent() {
		DEBUG.OUT("getAgent() " + synchroMode.getAgent());
		return this.getSynchroMode().getAgent();
	}

	@Override
	public void setAgent(IAgent agent) {
		DEBUG.OUT("setAgent() " + agent);
		this.getSynchroMode().setAgent(agent);
	}
	
	public IPopulation<?> getProxyPopulation() {
		DEBUG.OUT("getProxyPopulation() " + this.population);
		return this.population;
	}
	
	@Override
	public IMap<String, Object> getOrCreateAttributes() {
		DEBUG.OUT("getOrCreateAttributes " + this.getSynchroMode().getOrCreateAttributes());
		return this.getSynchroMode().getOrCreateAttributes();
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return this.getSynchroMode().stringValue(scope);
	}

	@Override
	public Object getAttribute(String key) {
		DEBUG.OUT("getAttribute ProxyAgent " + key);
		return this.getSynchroMode().getAttribute(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		DEBUG.OUT("setAttribute ProxyAgent " + key + " :: " + value);
		this.getSynchroMode().setAttribute(key, value);
	}

	@Override
	public boolean hasAttribute(String key) {
		DEBUG.OUT("hasAttribute " + this.getSynchroMode().hasAttribute(key));
		return this.getSynchroMode().hasAttribute(key);
	}

	@Override
	public GamaPoint getLocation() {
		DEBUG.OUT("getLocation " + this.getSynchroMode().getLocation());
		return this.getSynchroMode().getLocation();
	}

	@Override
	public GamaPoint setLocation(GamaPoint l) {
		DEBUG.OUT("setLocationsetLocationsetLocationsetLocationsetLocationsetLocation");
		GamaPoint agentPoint = this.getSynchroMode().setLocation(l);
		fixTopology();
				
		return agentPoint;
	}

	@Override
	public boolean dead() {
		return this.getSynchroMode().dead();
	}

	@Override
	public IShape copy(IScope scope) {
		return this.getSynchroMode().copy(scope);
	}

	@Override
	public void dispose() {
		DEBUG.OUT("DISPOSING proxyAgent " + this.getSynchroMode());
		this.getSynchroMode().dispose();
	}
	
	@Override
	public boolean init(IScope scope) throws GamaRuntimeException {
		return this.getSynchroMode().init(scope);
	}

	@Override
	public boolean step(IScope scope) throws GamaRuntimeException 
	{
		DEBUG.OUT("proxy step : ");
		return this.getSynchroMode().step(scope);
	}

	@Override
	public Object get(IScope scope, String index) throws GamaRuntimeException {
		return this.getSynchroMode().get(scope, index);
	}

	@Override
	public String getName() {
		return this.getSynchroMode().getName();
	}

	@Override
	public void setName(String name) {
		this.getSynchroMode().setName(name);
	}

	@Override
	public GamaPoint getLocation(IScope scope) {
		return this.getSynchroMode().getLocation(scope);
	}

	@Override
	public GamaPoint setLocation(IScope scope, GamaPoint l) {
		DEBUG.OUT("setLocationsetLocationsetLocationsetLocationsetLocationsetLocation");
		GamaPoint agentPoint = this.getSynchroMode().setLocation(scope, l);
		fixTopology();
				
		return agentPoint;
	}

	@Override
	public IShape getGeometry(IScope scope) {
		return this.getSynchroMode().getGeometry(scope);
	}
	
	@Override
	public IShape getGeometry() {
		return this.getSynchroMode().getGeometry();
	}

	@Override
	public void setGeometry(IScope scope, IShape newGeometry) {
		this.getSynchroMode().setGeometry(scope, newGeometry);
		fixTopology();
	}

	@Override
	public void schedule(IScope scope) {
		if (!dead()) { 
			scope.init(this); 
		}
	}

	@Override
	public int getIndex() {
		return this.getSynchroMode().getIndex();
	}

	@Override
	public String getSpeciesName() {
		return this.getSynchroMode().getSpeciesName();
	}

	@Override
	public ISpecies getSpecies() {
		return this.getSynchroMode().getSpecies();
	}

	@Override
	public IPopulation<? extends IAgent> getPopulation() {
		return this.getSynchroMode().getPopulation();
	}

	@Override
	public boolean isInstanceOf(ISpecies s, boolean direct) {
		return this.getSynchroMode().isInstanceOf(s, direct);
	}

	@Override
	public Object getDirectVarValue(IScope scope, String s) throws GamaRuntimeException {
		DEBUG.OUT("getDirectVarValue : " + s);
		return this.getSynchroMode().getDirectVarValue(scope, s);
	}

	@Override
	public void setDirectVarValue(IScope scope, String s, Object v) throws GamaRuntimeException {

		DEBUG.OUT("setDirectVarValue : " + s + " :: " + v);
		
		this.getSynchroMode().setDirectVarValue(scope, s, v);
		if(s.equals("location") || s.equals("shape"))
		{
			fixTopology();
		}
	}

	@Override
	public IModel getModel() {
		return this.getSynchroMode().getModel();
	}

	@Override
	public Object primDie(IScope scope) throws GamaRuntimeException {
		DEBUG.OUT("do primDie");
		DEBUG.OUT("do primDie name??? " + this.getSynchroMode().getAgent().getName());
		population.fireAgentRemoved(scope, this.getSynchroMode().getAgent());
		return this.getSynchroMode().primDie(scope);
	}

	@Override
	public Type getGeometricalType() {
		return this.getSynchroMode().getGeometricalType();
	}

	@Override
	public int compareTo(IAgent o) {
		return (this.getUUID() == ((MinimalAgent) o).getUUID()) ? 1 : 0;
	}

	@Override
	public Object getFromIndicesList(IScope scope, IList<String> indices) throws GamaRuntimeException {
		return this.getSynchroMode().getFromIndicesList(scope, indices);
	}

	@Override
	public ITopology getTopology() {
		return this.getSynchroMode().getTopology();
	}

	@Override
	public void setPeers(IList<IAgent> peers) {
		this.getSynchroMode().setPeers(peers);
	}

	@Override
	public IList<IAgent> getPeers() throws GamaRuntimeException {
		return this.getSynchroMode().getPeers();
	}

	@Override
	public IMacroAgent getHost() {
		return this.getSynchroMode().getHost();
	}

	@Override
	public void setHost(IMacroAgent macroAgent) {
		this.getSynchroMode().setHost(macroAgent);
	}

	@Override
	public List<IAgent> getMacroAgents() {
		return this.getSynchroMode().getMacroAgents();
	}

	@Override
	public boolean isInstanceOf(String skill, boolean direct) {
		return this.getSpecies().implementsSkill(skill);
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(ISpecies microSpecies) {
		DEBUG.OUT("???");
		return this.getScope().getSimulation().getPopulationFor(microSpecies);
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(String speciesName) {
		return this.getScope().getSimulation().getMicroPopulation(speciesName);
	}
	
	public void proxyDispose()
	{
		this.getProxyPopulation().remove(this);
	}

	@Override
	public IScope getScope() {
		return this.scope;
	}
	
	@Override
	public boolean covers(IShape g)
	{
		return this.getSynchroMode().covers(g);
	}
	
	@Override
	public boolean intersects(IShape g)
	{
		return this.getSynchroMode().intersects(g);
	}
	
	@Override
	public boolean crosses(IShape g)
	{
		return this.getSynchroMode().crosses(g);
	}
	
	@Override
	public void setInnerGeometry(final Geometry geom) {
		this.getGeometry().setInnerGeometry(geom);
	}

	@Override
	public IList<GamaPoint> getPoints() {
		if (this.getGeometry() == null) return GamaListFactory.EMPTY_LIST;
		return this.getGeometry().getPoints();
	}

	@Override
	public void setDepth(final double depth) 
	{
		if (this.getGeometry() == null) return;
		this.getGeometry().setDepth(depth);
	}
	
	@Override
	public void setGeometricalType(final IShape.Type t) 
	{
		this.getSynchroMode().setGeometricalType(t);
	}

	
	@Override
	public int intValue(final IScope scope) 
	{
		return this.getIndex();
	}
	
	@Override
	public Double getArea() { return this.getGeometry().getArea(); }

	@Override
	public Double getVolume() { return this.getGeometry().getVolume(); }

	@Override
	public double getPerimeter() { return this.getGeometry().getPerimeter(); }

	@Override
	public IList<GamaShape> getHoles() { return this.getGeometry().getHoles(); }

	@Override
	public GamaPoint getCentroid() { return this.getGeometry().getCentroid(); }

	@Override
	public GamaShape getExteriorRing(final IScope scope) {
		return this.getGeometry().getExteriorRing(scope);
	}

	@Override
	public Double getWidth() { return this.getGeometry().getWidth(); }

	@Override
	public Double getHeight() { return this.getGeometry().getHeight(); }

	@Override
	public Double getDepth() { return this.getGeometry().getDepth(); }

	@Override
	public GamaShape getGeometricEnvelope() { return this.getGeometry().getGeometricEnvelope(); }

	@Override
	public IList<? extends IShape> getGeometries() { return this.getGeometry().getGeometries(); }

	@Override
	public boolean isMultiple() { return this.getGeometry().isMultiple(); }

	@Override
	public boolean isPoint() { return this.getGeometry().isPoint(); }

	@Override
	public boolean isLine() { return this.getGeometry().isLine(); }

	@Override
	public Geometry getInnerGeometry() { return this.getGeometry().getInnerGeometry(); }

	@Override
	public Envelope3D getEnvelope() {
		final IShape g = this.getGeometry();
		return g == null ? null : g.getEnvelope();
	}
	@Override
	public double euclidianDistanceTo(final IShape g) {
		return this.getGeometry().euclidianDistanceTo(g);
	}
	@Override
	public double euclidianDistanceTo(final GamaPoint g) {
		return this.getGeometry().euclidianDistanceTo(g);
	}
	
	@Override
	public boolean partiallyOverlaps(final IShape g) {
		return this.getGeometry().partiallyOverlaps(g);
	}
	
	@Override
	public boolean touches(final IShape g) {
		return this.getGeometry().touches(g);
	}
	
	@Override
	public Map<String, Object> getAttributes(boolean createIfNeeded) {
		return this.getSynchroMode().getAttributes(createIfNeeded);
	}

	@Override
	public void updateWith(IScope s, ISerialisedAgent sa) {
		this.getSynchroMode().updateWith(s, sa);
	}

	@Override
	public void setUUID(String uuid) 
	{
		DEBUG.OUT("proxy setUUID " + uuid);
		this.uniqueID = UUID.fromString(uuid);
	}

	@action (name = "getUUID")
	public UUID getUUID(IScope scope) 
	{
		return this.uniqueID;
	}

	@Override
	public UUID getUUID() 
	{
		return this.uniqueID;
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
