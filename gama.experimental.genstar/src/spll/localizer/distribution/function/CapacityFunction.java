package spll.localizer.distribution.function;

import gama.core.metamodel.shape.IShape;
import spll.localizer.constraint.SpatialConstraintMaxNumber;

public class CapacityFunction implements ISpatialEntityFunction<Integer> {

	private SpatialConstraintMaxNumber scNumber;

	public CapacityFunction(SpatialConstraintMaxNumber scNumber) {
		this.scNumber = scNumber;
	}
	
	@Override
	public Integer apply(IShape t) {
		return scNumber.getNestCapacities().get(t);
	}

	@Override
	public void updateFunctionState(IShape entity) {
		int capacity = scNumber.getNestCapacities().get(entity);
		scNumber.getNestCapacities().put(entity, capacity == 0 ? 0 : capacity - 1);
	}

}
