package spll.localizer.distribution;

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
	
	private IList<? extends IShape> candidates;

	@Override
	public IShape getCandidate(IScope scope, IAgent entity, IList<? extends IShape>  candidates) {
		return candidates.anyValue(scope);
	}

	@Override
	public IShape getCandidate(IScope scope, IAgent entity) {
		if(this.candidates == null || this.candidates.isEmpty())
			throw new NullPointerException("No candidates have been setp - use ISpatialDistribution.setCandidates(List) first");
		return candidates.anyValue(scope);
	}

	@Override
	public void setCandidate(IList<? extends IShape>  candidates) {
		this.candidates = candidates;
	}

	@Override
	public IList<IShape> getCandidates(IScope scope) {
		return (IList<IShape>) candidates.copy(scope);
	}

	@Override
	public void removeNest(IShape n) {
		candidates.remove(n);
	}
	
}
