package spll.localizer.distribution.function;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.operators.Spatial.Queries;
import gama.gaml.operators.Spatial.Transformations;
import gama.gaml.types.Types;

/**
 * Function that computes probability based on the gravity model
 * 
 * @author kevinchapuis
 *
 */
public class GravityFunction implements ISpatialComplexFunction<Double> {

	private Map<IShape, Double> mass; 
	private double buffer = -1;
	private double frictionCoeff = 1.0;
	
	private BiFunction<Double, Double, Double> function;

	private GravityFunction() {
		this.function = new BiFunction<Double, Double, Double>() {
			@Override public Double apply(Double mass, Double distance) { 
				return mass / Math.pow(distance, frictionCoeff); }
		};
	}
	
	/**
	 * Mass of spatial entity is defined as the sum of distance between the spatial entity and all entities
	 * 
	 * @param candidates
	 * @param frictionCoeff
	 * @param entities
	 */
	public GravityFunction( IList<? extends IShape> candidates, double frictionCoeff, IList<IAgent> entities) {
		this();
		this.frictionCoeff = frictionCoeff;
		this.mass = candidates.stream().collect(Collectors.toMap(Function.identity(), se -> entities.stream()
				.mapToDouble(e -> se.euclidianDistanceTo(e.getLocation())).sum()));
	}

	/**
	 * Mass of spatial entity is defined as the number of entity within a given buffer around the spatial entity
	 * 
	 * @param candidates
	 * @param frictionCoeff
	 * @param buffer
	 * @param entities
	 */
	public GravityFunction(IScope scope, IList<? extends IShape> candidates, 
			double frictionCoeff, double buffer, IList<IAgent> entities) {
		this();
		IList<IAgent> agents = GamaListFactory.createWithoutCasting(Types.AGENT,entities);
		
		this.mass = candidates.stream().collect(
				Collectors.toMap(Function.identity(), 
						spacEntity -> (double) Queries.overlapping(scope,agents, Transformations.enlarged_by(scope, spacEntity, buffer)).length(scope)));
		this.buffer = buffer;
		this.frictionCoeff = frictionCoeff;
	}
	
	// ------------------------------------------ //
	
	/**
	 * Set the function that compute probability from mass of space entity and distance
	 * from population entity
	 * @param function
	 */
	public void setMassDistanceFunction(BiFunction<Double, Double, Double> function) {
		this.function = function;
	}
	
	/**
	 * Add / Replace the recorded mass of spatial entity
	 * @param mass
	 */
	public void setSpatialEntityMass(Map<? extends IShape, Double> mass) {
		this.mass.putAll(mass); 
	}
	
	// ------------------------------------------ //

	@Override
	public Double apply(IAgent entity , IShape spatialEntity) {
		return function.apply(mass.get(spatialEntity), spatialEntity.euclidianDistanceTo(entity.getLocation()));
	}

	@Override
	public void updateFunctionState(IScope scope, IList<IAgent> entities, IList<IShape> candidates) {
		if(buffer <= 0)
			for(IShape se : candidates)
				mass.put(se, entities.stream().mapToDouble(e -> se.euclidianDistanceTo(e.getLocation())).sum());
		else
			for(IShape se : candidates)
				mass.put(se, (double) Queries.overlapping(scope,entities, Transformations.enlarged_by(scope, se.getLocation(), buffer)).length(scope));
	}

	@Override
	public void clear() {
		mass.clear();
	}

	

}
