package spll.localizer.constraint;

import java.util.Map;
import java.util.stream.Collectors;

import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.operators.Cast;
import msi.gaml.operators.Maths;
import msi.gaml.types.Types;

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
	public IList<IShape> getCandidates(IScope scope, IList<IShape> nests) {
		return GamaListFactory.createWithoutCasting(Types.GEOMETRY, nests.stream().filter(a -> nestCapacities.get(a) > 0).toList());
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
