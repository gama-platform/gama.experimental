package spll.localizer.distribution;

import core.util.random.GenstarRandom;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;

/**
 * Uniform Spatial Distribution: each candidate has the same probability to be chosen 
 * 
 * @author patricktaillandier
 *
 * @param <N>
 */
public class UniformSpatialDistribution<N extends Number, E extends IShape> implements ISpatialDistribution<IShape> {
	
	private IList<IShape> candidates;

	@Override
	public IShape getCandidate(IScope scope, IAgent entity, IList<IShape> candidates) {
		return candidates.get(GenstarRandom.getInstance().nextInt(candidates.size()));
	}

	@Override
	public IShape getCandidate(IScope scope, IAgent entity) {
		if(this.candidates == null || this.candidates.isEmpty())
			throw new NullPointerException("No candidates have been setp - use ISpatialDistribution.setCandidates(List) first");
		return this.getCandidate(scope, entity, candidates);
	}

	@Override
	public void setCandidate(IList<IShape> candidates) {
		this.candidates = candidates;
	}

	@Override
	public IList<IShape> getCandidates(IScope scope) {
		return candidates.copy(scope);
	}
	
}
