package spll.localizer.constraint;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.types.Types;

public class SpatialConstraintMaxDistance extends ASpatialConstraint {

	private Map<IShape, Double> distanceToEntities;

	public SpatialConstraintMaxDistance(IList<IShape> distanceToEntities,
			Double distance) {
		this.distanceToEntities = distanceToEntities.stream().collect(Collectors
				.toMap(Function.identity(), entity -> distance));
	}
	
	public SpatialConstraintMaxDistance(Map<IShape, Double> distanceToEntities) {
		this.distanceToEntities = distanceToEntities;
	}
	
	@Override
	public IList<IShape> getCandidates(IScope scope,IContainer<?, ? extends IShape> nests) {
		return (IList<IShape>) GamaListFactory.createWithoutCasting(Types.GEOMETRY,nests.listValue(scope, Types.GEOMETRY, false).stream().filter(nest -> distanceToEntities.keySet()
				.stream().anyMatch(entity -> entity.euclidianDistanceTo(nest) <= distanceToEntities.get(entity))).toList());
		
		/*		.sorted((c1, c2) -> Double.compare(
						distanceToEntities.keySet().stream().mapToDouble(entity -> c1.getGeometry().getCentroid()
								.distance(entity.getGeometry())).min().getAsDouble(),
						distanceToEntities.keySet().stream().mapToDouble(entity -> c2.getGeometry().getCentroid()
								.distance(entity.getGeometry())).min().getAsDouble()))*/
				
	}

	@Override
	public boolean updateConstraint(IShape nest) {
		return false;
	}

	@Override
	public void relaxConstraintOp(IList<IShape> distanceToEntities) {
		distanceToEntities.stream().forEach(entity -> 
			this.distanceToEntities.put(entity, 
					this.distanceToEntities.get(entity)+this.increaseStep));

	}

}
