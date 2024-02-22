package synchronizationMode;

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
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.species.ISpecies;

/**
 * Control the access of agent from a distant processor / simulation
 * 
 * @author Lucas Grosjean
 *
 */
public abstract interface SynchronizationModeAbstract extends IAgent
{	
	@Override
	public ITopology getTopology();
		
	@Override
	public abstract IAgent getAgent();
	
	public abstract void stepProxy();
	
	@Override
	public abstract boolean step(IScope scope);
	
	@Override
	public abstract IMap<String, Object> getOrCreateAttributes();

	@Override
	public abstract String stringValue(IScope scope);
	
	@Override
	public abstract Object getAttribute(String key);

	@Override
	public abstract void setAttribute(String key, Object value) ;
		 
	@Override
	public abstract boolean hasAttribute(String key);
		 
	@Override
	public abstract GamaPoint getLocation();
		 
	@Override
	public abstract GamaPoint setLocation(GamaPoint l);
		 
	@Override
	public abstract boolean dead();
		 
	@Override
	public abstract Object getDirectVarValue(IScope scope, String s);
	 
	@Override
	public abstract void setDirectVarValue(IScope scope, String s, Object v);
		 
	@Override
	public abstract void updateWith(IScope scope, ISerialisedAgent sa);
		 
	@Override
	public abstract IShape copy(IScope scope);
	 
	@Override
	public abstract void dispose();
 
	@Override
	public abstract boolean init(IScope scope);
	
	public abstract Object _init_(final IScope scope);
	
	public abstract boolean initSubPopulations(final IScope scope);

	@Override
	public abstract Object get(final IScope scope, final String index);

	@Override
	public abstract String getName();
	
	@Override
	public abstract void setName(String name);
	
	@Override
	public abstract GamaPoint getLocation(IScope scope);

	@Override
	public abstract GamaPoint setLocation(IScope scope, GamaPoint l);

	@Override
	public abstract IShape getGeometry(IScope scope);	
	 
	@Override
	public abstract IShape getGeometry();
	
	@Override
	public abstract void setGeometry(IScope scope, IShape newGeometry);
	 
	@Override
	public abstract void schedule(IScope scope);

	@Override
	public abstract int getIndex();

	@Override
	public abstract String getSpeciesName();
	 
	@Override
	public abstract ISpecies getSpecies();
	 
	@Override
	public abstract IScope getScope();
	 
	@Override
	public abstract IMacroAgent getHost();
	 
	@Override
	public abstract IPopulation<? extends IAgent> getPopulation();
	 
	@Override
	public abstract boolean isInstanceOf(ISpecies s, boolean direct);
	 
	@Override
	public abstract IModel getModel();
	
	@Override
	public abstract Object primDie(IScope scope);
	 
	public abstract int getHashcode();
	 
	@Override
	public abstract boolean covers(IShape g);
	
	@Override
	public abstract boolean intersects(IShape g);
	
	@Override
	public abstract boolean crosses(IShape g);
	 
	@Override
	public abstract void setInnerGeometry(final Geometry geom);
	 
	@Override
	public abstract IList<GamaPoint> getPoints();
	 
	@Override
	public abstract void setDepth(final double depth);
	 
	@Override
	public abstract void setGeometricalType(final IShape.Type t);
	 
	@Override
	public abstract int intValue(final IScope scope);
	 
	@Override
	public abstract Double getArea();
	 
	@Override
	public abstract Double getVolume();
	 
	@Override
	public abstract double getPerimeter();
	 
	@Override
	public abstract IList<GamaShape> getHoles();
	 
	@Override
	public abstract GamaPoint getCentroid();
	 
	@Override
	public abstract GamaShape getExteriorRing(final IScope scope);

	@Override
	public abstract Double getWidth();

	@Override
	public abstract Double getHeight();
	 
	@Override
	public abstract Double getDepth();
	 
	@Override
	public abstract GamaShape getGeometricEnvelope();
	 
	@Override
	public abstract IList<? extends IShape> getGeometries();
	 
	@Override
	public abstract boolean isMultiple();
	
	@Override
	public abstract boolean isPoint();
	
	@Override
	public abstract boolean isLine();
	
	@Override
	public abstract Geometry getInnerGeometry();
	
	@Override
	public abstract Envelope3D getEnvelope();
	
	@Override
	public abstract double euclidianDistanceTo(final IShape g);
	 
	@Override
	public abstract double euclidianDistanceTo(final GamaPoint g);
	 
	@Override
	public abstract boolean partiallyOverlaps(final IShape g);
	 
	@Override
	public abstract boolean touches(final IShape g);

	public abstract void updateAttributes(IAgent agent);
}
