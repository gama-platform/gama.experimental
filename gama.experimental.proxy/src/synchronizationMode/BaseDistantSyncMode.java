package synchronizationMode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

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
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.species.ISpecies;

public class BaseDistantSyncMode implements SynchronizationModeAbstract {
	
	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return serializeToGaml(true);
	}
	
	@Override
	public Object getAttribute(String key)
	{
		return null;
	}

	@Override
	public void setAttribute(String key, Object value) 
	{
	}
	
	@Override
	public boolean hasAttribute(String key)
	{
		return false;
	}
	
	@Override
	public GamaPoint getLocation() 
	{
		return null;
	}
	
	@Override
	public GamaPoint setLocation(GamaPoint l)
	{
		return null;
	}
	
	@Override
	public boolean dead() 
	{
		return false;
	}
	
	@Override
	public Object getDirectVarValue(IScope scope, String s) throws GamaRuntimeException {
		return null;
	}

	
	@Override
	public void setDirectVarValue(IScope scope, String s, Object v) throws GamaRuntimeException {
	}
	
	@Override
	public void updateWith(IScope scope, ISerialisedAgent sa)
	{
	}
	
	@Override
	public IShape copy(IScope scope) {
		return null;
	}

	@Override
	public void dispose() 
	{
	}

	@Override
	public boolean init(IScope scope) throws GamaRuntimeException 
	{
		return false;
	}
	
	@Override
	public Object _init_(final IScope scope) {
		return null;
	}
	
	@Override
	public boolean initSubPopulations(final IScope scope) {
		return false;
	}

	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException 
	{
		return null;
	}
	
	@Override
	public String getName() {
		return "";
	}
	
	@Override
	public void setName(String name) {
	}
	
	@Override
	public GamaPoint getLocation(IScope scope) {
		return null;
	}

	
	@Override
	public GamaPoint setLocation(IScope scope, GamaPoint l) {
		return null;
	}

	
	@Override
	public IShape getGeometry(IScope scope) {
		return null;
	}
	
	@Override
	public IShape getGeometry() {
		return null;
	}

	
	@Override
	public void setGeometry(IScope scope, IShape newGeometry) {
	}
	
	@Override
	public void schedule(IScope scope) {
		if (!dead()) { scope.init(this); }
	}

	
	@Override
	public int getIndex() {
		return 0;
	}

	
	@Override
	public String getSpeciesName() {
		return null;
	}

	
	@Override
	public ISpecies getSpecies() {
		return null;
	}
	
	@Override
	public IScope getScope() {
		return null;
	}
	
	@Override
	public IMacroAgent getHost() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IPopulation<? extends IAgent> getPopulation() {
		return null;
	}

	@Override
	public boolean isInstanceOf(ISpecies s, boolean direct) {
		return false;
	}

	@Override
	public IModel getModel() {
		return null;
	}
	 
	@Override
	public Object primDie(IScope scope) throws GamaRuntimeException{
		return null;
	}
	
	@Override
	public boolean covers(IShape g)
	{
		return false;
	}
	
	@Override
	public boolean intersects(IShape g)
	{
		return false;
	}
	
	@Override
	public boolean crosses(IShape g)
	{
		return false;
	}
	
	@Override
	public void setInnerGeometry(final Geometry geom) {
	}

	@Override
	public IList<GamaPoint> getPoints() {
		return null;
	}

	@Override
	public void setDepth(final double depth) 
	{
	}
	
	@Override
	public void setGeometricalType(final IShape.Type t) 
	{
	}

	@Override
	public int intValue(final IScope scope) 
	{
		return 0;
	}
	
	@Override
	public Double getArea() {
		return null;
	}

	@Override
	public Double getVolume() {
		return null;}

	@Override
	public double getPerimeter() {
		return 0.0;
	}

	@Override
	public IList<GamaShape> getHoles() {
		return null;
	}

	@Override
	public GamaPoint getCentroid() {
		return null;
		}

	@Override
	public GamaShape getExteriorRing(final IScope scope) {
		return null;
	}

	@Override
	public Double getWidth() {
		return 0.0;
	}

	@Override
	public Double getHeight() {
		return 0.0;
	}

	@Override
	public Double getDepth() {
		return 0.0;
	}

	@Override
	public GamaShape getGeometricEnvelope() {
		return null;
	}

	@Override
	public IList<? extends IShape> getGeometries() { 
		return null;
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

	@Override
	public boolean isPoint() {
		return false;
	}

	@Override
	public boolean isLine() {
		return false; 
	}

	@Override
	public Geometry getInnerGeometry() {
		return null; }

	@Override
	public Envelope3D getEnvelope() {
		return null;
	}
	
	@Override
	public double euclidianDistanceTo(final IShape g) {
		return 0.0;
	}
	
	@Override
	public double euclidianDistanceTo(final GamaPoint g) {
		return 0.0;
	}
	
	@Override
	public boolean partiallyOverlaps(final IShape g) {
		return false;
	}
	
	@Override
	public boolean touches(final IShape g) {
		return false;
	}

	@Override
	public ITopology getTopology() {
		return null;
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
		return null;
	}

	@Override
	public void setAgent(IAgent agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IAgent getAgent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean step(IScope scope) {
		// TODO Auto-generated method stub
		DEBUG.OUT("base distantsycnhromode step");
		return false;
	}

	@Override
	public IMap<String, Object> getOrCreateAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateAttributes(IAgent agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stepProxy() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setUUID(String uuid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UUID getUUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOriginalSimulationID(int originalSimulationID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getOriginalSimulationID() {
		// TODO Auto-generated method stub
		return 0;
	}

}
