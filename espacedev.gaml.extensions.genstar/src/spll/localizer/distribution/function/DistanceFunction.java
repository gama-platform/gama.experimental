package spll.localizer.distribution.function;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.IList;

public class DistanceFunction implements ISpatialComplexFunction<Double> {

	@Override
	public Double apply(IAgent entity, IShape geom) {
		return geom.euclidianDistanceTo(entity.getLocation());
	}

	@Override
	public void updateFunctionState(IScope scope, IList<IAgent> entities,
			IList<IShape> candidates) {
		
		
	}

	@Override
	public void clear() {
		
		
	}

}
