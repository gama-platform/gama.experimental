package spll.localizer.distribution;

import java.util.HashMap;
import java.util.Map;

import core.util.random.roulette.ARouletteWheelSelection;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import spll.localizer.distribution.function.ISpatialComplexFunction;

/**
 * Spatial distribution that relies on both attribute of spatial and population entity. 
 * For example, probability attached to the distance between the entity to bind and the spatial entity to be bound with.
 * 
 * @author kevinchapuis
 *
 * @param <N>
 */
public class ComplexSpatialDistribution<N extends Number> implements ISpatialDistribution<IShape> {

	private ISpatialComplexFunction<N> function;
	
	private IList<? extends IShape> candidates;
	private Map<IShape, ARouletteWheelSelection<N, ? extends IShape>> roulettes;
	
	public ComplexSpatialDistribution(ISpatialComplexFunction<N> function) {
		this.function = function;
	}
	
	@Override
	public IShape getCandidate(IScope scope, IAgent entity, IList<? extends IShape> candidates){
		return RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(candidate -> function.apply(entity,candidate)).toList(), candidates)
			.drawObject();
	} 
		
	@Override
	public IShape getCandidate(IScope scope, IAgent entity) {
		if(this.candidates == null || this.candidates.isEmpty())
			throw new NullPointerException("No candidates have been setup, must use "
					+ "ISpatialDistribution.setCandidates(List) first");
		if(this.roulettes == null)
			this.roulettes = new HashMap<>();
		if(this.roulettes.isEmpty()
				|| !this.roulettes.containsKey(entity))
			this.roulettes.put(entity, RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(candidate -> function.apply(entity,candidate)).toList(), candidates));
		return roulettes.get(entity).drawObject(); 
	}

	@Override
	public void setCandidate(IList<? extends IShape> candidates) {
		this.candidates = candidates;
	}

	@Override
	public  IList<IShape> getCandidates(IScope scope) {
		return (IList<IShape>) candidates.copy(scope);
	}

	@Override
	public void removeNest(IShape n) {
		for (ARouletteWheelSelection roulette : roulettes.values())
			roulette.remove(n);
	}


}
