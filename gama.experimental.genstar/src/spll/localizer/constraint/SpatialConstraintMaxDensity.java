package spll.localizer.constraint;

import java.util.Map;
import java.util.stream.Collectors;

import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.util.IList;
import gama.gaml.operators.Cast;

public class SpatialConstraintMaxDensity extends SpatialConstraintMaxNumber {

	protected Map<IShape, Double> nestInitDensity;

	// maxVal: global value for the max density of entities per nest
	public SpatialConstraintMaxDensity(final IList<IShape> nests,
			final Double maxVal) {
		super(nests, maxVal);

	}

	// keyAttMax: name of the attribute that contains the max density of entities in the nest file
	public SpatialConstraintMaxDensity(final IList<IShape> nests,
			final String keyAttMax) {
		super(nests, keyAttMax);
	}

	@Override
	public void relaxConstraintOp(final IList<IShape> nests) {
		for (IShape n : nests) {
			nestCapacities.put(n, Math
					.round(nestCapacities.get(n)
							- (int) Math.round(nestInitDensity.get(n) * n.getArea()))
					+ (int) Math.round((nestInitDensity.get(n) + increaseStep * (1 + nbIncrements))
							* n.getArea()));
		}
	}

	@Override
	protected Map<IShape, Integer> computeMaxPerNest(final IList<IShape> nests,
			final String keyAttMax) {
		nestInitDensity = nests.stream().collect(Collectors.toMap(a -> a,
				a -> Cast.asFloat(GAMA.getRuntimeScope(), a.getAttribute(keyAttMax))));
		
		return nests.stream().collect(Collectors.toMap(a->a,
				a -> (int) Math.round(Cast.asFloat(GAMA.getRuntimeScope(), a.getAttribute(keyAttMax)) * a.getArea())));
	}

	@Override
	protected Map<IShape, Integer> computeMaxPerNest(final IList<IShape> nests,
			final Double maxVal) {
		nestInitDensity = nests.stream().collect(Collectors.toMap(a -> a, a -> maxVal));
		return nests.stream()
				.collect(Collectors.toMap(a -> a, a -> (int) Math.round(maxVal * a.getArea())));
		
	}

}
