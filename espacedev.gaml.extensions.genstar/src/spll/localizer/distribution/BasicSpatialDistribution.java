package spll.localizer.distribution;

import java.util.stream.Collectors;

import core.util.random.roulette.ARouletteWheelSelection;
import core.util.random.roulette.RouletteWheelSelectionFactory;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.types.Types;
import spll.localizer.distribution.function.ISpatialEntityFunction;

/**
 * Spatial Distribution that relies on spatial entity attribute to asses probability. For exemple,
 * probability could be computed based on the area of spatial entity.
 * 
 * @author kevinchapuis
 *
 * @param <N>
 */
public class BasicSpatialDistribution<N extends Number,E extends IShape> implements ISpatialDistribution<IShape> {
	
	private ISpatialEntityFunction<N> function;
	private ARouletteWheelSelection<N,IShape> roulette;
	
	public BasicSpatialDistribution(ISpatialEntityFunction<N> function) {
		this.function = function;
	} 
	
	@Override 
	public IShape getCandidate(IScope scope, IAgent entity, IList<? extends IShape> candidates) {
		if (roulette != null && roulette.getKeys().equals(candidates)) {
			return roulette.drawObject();
		}
		return RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
				.map(a -> function.apply(a)).collect(Collectors.toList()), candidates)
			.drawObject();
		
	}

	@Override
	public IShape getCandidate(IScope scope, IAgent entity) {
		if(this.roulette == null || this.roulette.getKeys().isEmpty())
			throw new NullPointerException("No candidate geographic entity to draw from");
		return roulette.drawObject();
	}

	@Override
	public void setCandidate(IList<? extends IShape> candidates) {
		this.roulette = (ARouletteWheelSelection<N, IShape>) RouletteWheelSelectionFactory.getRouletteWheel(candidates.stream()
			.map(a -> function.apply(a)).toList(), candidates);
	}

	@Override
	public IList<IShape> getCandidates(IScope scope) {
		return (IList<IShape>) GamaListFactory.createWithoutCasting(Types.GEOMETRY,roulette.getKeys());
	}
	
	@Override
	public void removeNest(IShape n) {
		roulette.remove(n);
	}


	
}
