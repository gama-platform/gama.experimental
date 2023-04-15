package spll.localizer.distribution.function;

import java.util.Collection;

import core.metamodel.value.IValue;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;

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
