package synchronizationMode;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.locationtech.jts.geom.Geometry;

import HardSyncModeComm.HardSyncRequestRunnableClient;
import MPISkill.IMPISkill;
import gama.core.common.geometry.Envelope3D;
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
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.species.ISpecies;
import mpi.MPI;
import mpi.MPIException;

public class DistantHardSyncMode implements SynchronizationModeAbstract {
	
	int myRank;
	int mpiRankOfLocalAgent;
	int uniqueID;

	
	public DistantHardSyncMode(IScope scope, int myRank, int mpiRankOfLocalAgent, int uniqueID) 
	{
		this.myRank = myRank;
		this.mpiRankOfLocalAgent = mpiRankOfLocalAgent;
		this.uniqueID = uniqueID;
	}	
	
	HardSyncRequestRunnableClient setupReadRequest(String attribute)
	{
		return new HardSyncRequestRunnableClient(HardSyncModeComm.RequestType.READ, this.uniqueID, this.myRank, this.mpiRankOfLocalAgent, attribute);
	}

	HardSyncRequestRunnableClient setupWriteRequest(String attribute, Object value)
	{
		return new HardSyncRequestRunnableClient(HardSyncModeComm.RequestType.WRITE, this.uniqueID, this.myRank, this.mpiRankOfLocalAgent, attribute, value);
	}
	
	Object sendRequest(HardSyncRequestRunnableClient request)
	{
		byte[] requestSerialized = request.serializeObject();
		try {
			MPI.COMM_WORLD.send(requestSerialized, requestSerialized.length, MPI.BYTE, this.mpiRankOfLocalAgent, IMPISkill.REQUEST_TYPE);
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // send request
		return null;
	}
	
		
	@Override
	public IAgent getAgent()
	{
		
		return null; // TODO throw exception?
	}
	
	@Override
	public void stepProxy()
	{
	}
	
	@Override
	public boolean step(IScope scope) throws GamaRuntimeException {
		return true;
	}
	
	@Override
	public IMap<String, Object> getOrCreateAttributes()
	{
		return null;
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return serializeToGaml(true);
	}
	
	@Override
	public Object getAttribute(String key)
	{
		return sendRequest(setupReadRequest(key));
	}

	@Override
	public void setAttribute(String key, Object value) 
	{
		// get lock 
		//attributes.put(key, value);
		// releaselock
	}
	
	@Override
	public boolean hasAttribute(String key)
	{
		//return attributes.containsKey(key);
		return false;
	}
	
	@Override
	public GamaPoint getLocation() 
	{
		//return (GamaPoint) attributes.get(IKeyword.LOCATION);
		return null;
	}
	
	@Override
	public GamaPoint setLocation(GamaPoint l)
	{
		//attributes.put(IKeyword.LOCATION, l);
		//return l;
		return null;
	}
	
	@Override
	public boolean dead() 
	{
		return false;
	}
	
	@Override
	public Object getDirectVarValue(IScope scope, String s) throws GamaRuntimeException {
		setupReadRequest(s);
		return null;
	}

	
	@Override
	public void setDirectVarValue(IScope scope, String s, Object v) throws GamaRuntimeException {
		//this.attributes.put(s, v);
	}
	
	@Override
	public void updateWith(IScope scope, ISerialisedAgent sa)
	{
		final Map<String, Object> mapAttr = sa.attributes();
		for (final Entry<String, Object> attr : mapAttr.entrySet()) {
			this.setDirectVarValue(scope, attr.getKey(), attr.getValue());
		}
	}
	
	@Override
	public IShape copy(IScope scope) {
		//IShape shape = (IShape) this.attributes.get(IKeyword.SHAPE);
		//return shape.copy(scope);
		return null;
	}

	@Override
	public void dispose() 
	{
		// TODO
	}

	@Override
	public boolean init(IScope scope) throws GamaRuntimeException 
	{
		if (!getPopulation().isInitOverriden()) 
		{
			_init_(scope);
		} else {
			scope.execute(getSpecies().getAction(ISpecies.initActionName), this, null);
		}
		return !scope.interrupted();
	}
	
	@Override
	public Object _init_(final IScope scope) {
		return getSpecies().getArchitecture().init(scope) ? initSubPopulations(scope) : false;
	}
	
	@Override
	public boolean initSubPopulations(final IScope scope) {
		return true;
	}

	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException 
	{
		if (getPopulation().hasVar(index)) return scope.getAgentVarValue(this, index);
		return getAttribute(index);
	}
	
	@Override
	public String getName() {
		//return this.attributes.get(IKeyword.NAME).toString();
		return null;
	}
	
	@Override
	public void setName(String name) {
		//this.attributes.put(IKeyword.NAME, name);
	}
	
	@Override
	public GamaPoint getLocation(IScope scope) {
		//return (GamaPoint) this.attributes.get(IKeyword.LOCATION);
		return null;
	}

	
	@Override
	public GamaPoint setLocation(IScope scope, GamaPoint l) {
		//this.attributes.put(IKeyword.LOCATION, l);	
		//return l;
		return null;
	}

	
	@Override
	public IShape getGeometry(IScope scope) {
		//return (IShape) this.attributes.get(IKeyword.SHAPE);
		return null;
	}
	
	@Override
	public IShape getGeometry() {
		//return (IShape) this.attributes.get(IKeyword.SHAPE);
		return null;
	}

	
	@Override
	public void setGeometry(IScope scope, IShape newGeometry) {
		//this.attributes.put(IKeyword.SHAPE, newGeometry);	
	}
	
	@Override
	public void schedule(IScope scope) {
		if (!dead()) { scope.init(this); }
	}

	
	@Override
	public int getIndex() {
		return 0; // TODO check if correct
	}

	
	@Override
	public String getSpeciesName() {
		//return (String) this.attributes.get(IKeyword.TYPE);
		return null;
	}

	
	@Override
	public ISpecies getSpecies() {
		return getPopulation().getSpecies();
	}
	
	@Override
	public IScope getScope() {
		final IMacroAgent a = getHost();
		if (a == null) return null;
		return a.getScope();
	}
	
	@Override
	public IMacroAgent getHost() {
		return getPopulation().getHost();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPopulation<? extends IAgent> getPopulation() {
		//return (IPopulation<? extends IAgent>) this.attributes.get(IKeyword.POPULATION);
		return null;
	}

	@Override
	public boolean isInstanceOf(ISpecies s, boolean direct) {
		return this.getSpecies().getName().equals(s.getName());
	}

	@Override
	public IModel getModel() {
		return null;	// TODO check if correct
	}
	 
	@Override
	public Object primDie(IScope scope) throws GamaRuntimeException{
		return null;	// TODO check if correct
	}

	@Override
	public int getHashcode() {
		//return (int) this.attributes.get(IKeyword.HASHCODE);
		return 0;
	}
	
	@Override
	public boolean covers(IShape g)
	{
		IShape shape = this.getGeometry();
		return shape.covers(g);
	}
	
	@Override
	public boolean intersects(IShape g)
	{
		IShape shape = this.getGeometry();
		return shape.intersects(shape);
	}
	
	@Override
	public boolean crosses(IShape g)
	{
		IShape shape = this.getGeometry();
		return shape.crosses(shape);
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
		this.getGeometry().setGeometricalType(t);
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
	public ITopology getTopology() {
		return null; // OK
	}

	@Override
	public void setPeers(IList<IAgent> peers) { // OK
	}

	@Override
	public IList<IAgent> getPeers() throws GamaRuntimeException {
		return null; // OK
	}

	@Override
	public void setHost(IMacroAgent macroAgent) { // OK
	}

	@Override
	public List<IAgent> getMacroAgents() {
		return null; // OK
	}

	@Override
	public boolean isInstanceOf(String skill, boolean direct) {
		return false; // OK
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(ISpecies microSpecies) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(String speciesName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getGeometricalType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(IAgent o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getFromIndicesList(IScope scope, IList<String> indices) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getAttributes(boolean createIfNeeded) {
		//if (attributes == null && createIfNeeded) { attributes = GamaMapFactory.create(Types.STRING, Types.NO_TYPE); }
		//return attributes;
		return null;
	}

	@Override
	public void setAgent(IAgent agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAttributes(IAgent agent) {
		// TODO Auto-generated method stub
		
	}

}
