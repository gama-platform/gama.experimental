package spll.localizer.constraint;

import java.util.Map;
import java.util.stream.Collectors;

import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Maths;
import gama.gaml.types.Types;

public class SpatialConstraintMaxNumber extends ASpatialConstraint {

	protected Map<IShape,Integer> nestCapacities;
	
	
	/**
	 * Constraint on maximum number of entities in every nest
	 * 
	 * @param nests
	 * @param maxVal : global value for the max number of entities per nest
	 */
	public SpatialConstraintMaxNumber(IList<IShape> nests, Double maxVal) {
		super();
		nestCapacities = computeMaxPerNest(nests, maxVal);
	}
	
	 /**
	  * Constraint on maximum number of entities for each a nest
	  * @param nests
	  * @param keyAttMax : name of the attribute that contains the max number of entities in the nest file
	  */
	public SpatialConstraintMaxNumber(IList<IShape> nests, String keyAttMax) {
		super();
		nestCapacities = computeMaxPerNest(nests, keyAttMax);
	}
	
	@Override
	public void relaxConstraintOp(IList<IShape> nests) {
		for (IShape nest : nests)  
			nestCapacities.put(nest, Maths.round(nestCapacities.get(nest) + increaseStep));
	}


	@Override
	public IList<IShape> getCandidates(IScope scope, IContainer<?, ? extends IShape> nests) {
		return (IList<IShape>) GamaListFactory.createWithoutCasting(Types.GEOMETRY, nests.listValue(scope, Types.GEOMETRY, false).stream().filter(a -> nestCapacities.get(a) > 0).toList());
	}
	
	@Override
	public boolean updateConstraint(IShape nest) {
		int capacity = nestCapacities.get(nest);
		nestCapacities.put(nest, capacity - 1);
		if (capacity <= 1) return true;
		return false;
			
	}

	
	public Map<IShape, Integer> getNestCapacities() {
		return nestCapacities;
	}

	protected Map<IShape, Integer> computeMaxPerNest(IList<IShape> nests, String keyAttMax){
		return nests.stream().collect(Collectors.toMap(a -> a, a-> Cast.asInt(GAMA.getRuntimeScope(), a.getAttribute(keyAttMax))));
	}
	
	protected Map<IShape, Integer> computeMaxPerNest(IList<IShape> nests, Double maxVal){
		return nests.stream().collect(Collectors.toMap(a ->a, a-> (int)(Math.round(maxVal))));
	}
	
}
